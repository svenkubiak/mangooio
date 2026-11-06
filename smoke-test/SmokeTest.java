import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ServerDescription;
import io.mangoo.utils.TotpUtils;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Drives the scenarios against two running instances (A on 9101, B on 9102), nginx (9100) and a MongoDB replica set
 * (27117-27119). Usage: java -cp <classpath> SmokeTest.java init | run <work directory>
 */
public class SmokeTest {
    static final String A = "http://localhost:9101";
    static final String B = "http://localhost:9102";
    static final String LB = "http://localhost:9100";
    static final String USER_SECRET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    static final String ADMIN_SECRET = "ZYXWVUTSRQPONMLKJIHGFEDCBA765432ZYXWVUTSRQPONMLKJIHGFEDCBA765432";
    static final String REPLICA_SET = "mongodb://localhost:27117,localhost:27118,localhost:27119/?replicaSet=rs0";
    static final Pattern CSRF = Pattern.compile("value=\"([^\"]+)\" name=\"x-csrf-token\"");
    static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5)).build();
    static final boolean TTY = Boolean.getBoolean("smoke.tty");
    static final boolean COLOR = Boolean.getBoolean("smoke.color");
    static final int COLUMNS = Integer.getInteger("smoke.columns", 120);
    static final String[] FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    static final List<String> NOTES = new CopyOnWriteArrayList<>();
    static volatile String running;
    static volatile String status;
    static volatile Instant runningSince;
    static Path work;

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "init" -> init();
            case "run" -> {
                work = Path.of(args[1]);
                run();
            }
            default -> throw new IllegalArgumentException(args[0]);
        }
    }

    static void init() throws Exception {
        try (MongoClient client = MongoClients.create("mongodb://localhost:27117/?directConnection=true")) {
            var members = List.of(member(0, 27117), member(1, 27118), member(2, 27119));
            client.getDatabase("admin").runCommand(new Document("replSetInitiate", new Document("_id", "rs0").append("members", members)));
        }
        try (MongoClient client = MongoClients.create(REPLICA_SET)) {
            await("replica set has a primary", Duration.ofSeconds(60), () -> primaryPort(client) > 0);
            System.out.println("primary on " + primaryPort(client));
        }
    }

    static Document member(int id, int port) {
        return new Document("_id", id).append("host", "localhost:" + port);
    }

    static void run() throws Exception {
        var scenarios = List.of(
                new Named("round robin reaches both instances", SmokeTest::roundRobin),
                new Named("session cookie works on the other instance", SmokeTest::sessionAcrossInstances),
                new Named("failed login budget is shared", SmokeTest::sharedLock),
                new Named("logout revocation applies on the other instance", SmokeTest::revocationAcrossInstances),
                new Named("TOTP code is accepted once across instances", SmokeTest::totpReplayAcrossInstances),
                new Named("admin second factor works across instances", SmokeTest::adminTwoFactorAcrossInstances),
                new Named("server sent event reaches a client of the other instance", SmokeTest::sseAcrossInstances),
                new Named("replica set failover", SmokeTest::failover),
                new Named("graceful shutdown of instance A", SmokeTest::gracefulShutdown));

        var labels = new ArrayList<String>();
        for (var i = 0; i < scenarios.size(); i++) {
            labels.add("[%d/%d] %s".formatted(i + 1, scenarios.size(), scenarios.get(i).name()));
        }
        int width = labels.stream().mapToInt(String::length).max().orElse(0);
        if (TTY) {
            Thread.ofPlatform().daemon().start(SmokeTest::spin);
        }

        var failed = 0;
        for (var i = 0; i < scenarios.size(); i++) {
            if (!scenario(scenarios.get(i), labels.get(i), width)) {
                failed++;
            }
        }
        System.exit(failed == 0 ? 0 : 1);
    }

    interface Scenario {
        void run() throws Exception;
    }

    record Named(String name, Scenario scenario) {}

    // Prints the result line with the notes of the scenario and appends the result to scenarios.tsv for the summary
    static boolean scenario(Named scenario, String label, int width) throws IOException {
        NOTES.clear();
        status = null;
        runningSince = Instant.now();
        running = label;

        String error = null;
        try {
            scenario.scenario().run();
        } catch (Throwable e) {
            error = Objects.requireNonNullElse(e.getMessage(), e.toString()).replaceAll("\\s+", " ");
        }
        long millis = Duration.between(runningSince, Instant.now()).toMillis();

        synchronized (SmokeTest.class) {
            running = null;
            var out = new StringBuilder(TTY ? "\r\033[K" : "");
            out.append("  ").append(error == null ? color("32", "✓") : color("31", "✗")).append(' ')
                    .append(("%-" + width + "s").formatted(label)).append(' ')
                    .append(color("2", "%7s".formatted(elapsed(millis)))).append('\n');
            NOTES.forEach(note -> out.append("      ").append(color("2", note)).append('\n'));
            if (error != null) {
                out.append("      ").append(color("31", error)).append('\n');
            }
            System.out.print(out);
            System.out.flush();
        }

        String line = String.join("\t", error == null ? "PASS" : "FAIL", scenario.name(), String.valueOf(millis), Objects.requireNonNullElse(error, ""));
        Files.writeString(work.resolve("scenarios.tsv"), line + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return error == null;
    }

    // Redraws the line of the running scenario with its current status, only on a terminal
    static void spin() {
        for (var frame = 0; ; frame++) {
            synchronized (SmokeTest.class) {
                String label = running;
                if (label != null) {
                    var notes = List.copyOf(NOTES);
                    String current = status != null ? status : notes.isEmpty() ? null : notes.getLast();
                    var line = "  " + FRAMES[frame % FRAMES.length] + " " + label + (current != null ? " · " + current : "")
                            + " " + Duration.between(runningSince, Instant.now()).toSeconds() + " s";
                    System.out.print("\r\033[K" + (line.length() < COLUMNS ? line : line.substring(0, COLUMNS - 1)));
                    System.out.flush();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    // Shown below the result of the scenario
    static void note(String text) {
        NOTES.add(text);
    }

    // Shown while the scenario runs only
    static void status(String text) {
        status = text;
    }

    static String elapsed(long millis) {
        return millis < 1000 ? millis + " ms" : String.format(Locale.ROOT, "%.1f s", millis / 1000.0);
    }

    static String color(String code, String text) {
        return COLOR ? "\033[" + code + "m" + text + "\033[0m" : text;
    }

    // --- scenarios ---

    static void roundRobin() throws Exception {
        Set<String> instances = new HashSet<>();
        for (var i = 0; i < 6; i++) {
            instances.add(new Browser().get(LB, "/instance").body());
        }
        check(instances.size() == 2, "expected two instances behind nginx, got " + instances);
    }

    static void sessionAcrossInstances() throws Exception {
        var browser = new Browser();
        check("ok".equals(browser.post(A, "/login", Map.of("username", "alice", "password", "admin")).body()), "login on A failed");
        String me = browser.get(B, "/me").body();
        check("true:true:alice".equals(me), "B does not see the login of A: " + me);
    }

    static void sharedLock() throws Exception {
        String user = "eve-" + UUID.randomUUID();
        for (var i = 0; i < 10; i++) {
            new Browser().post(i % 2 == 0 ? A : B, "/login", Map.of("username", user, "password", "wrong"));
        }
        check("true".equals(new Browser().get(A, "/locked/" + user).body()), "A does not see the lock");
        check("true".equals(new Browser().get(B, "/locked/" + user).body()), "B does not see the lock");
        String correct = new Browser().post(B, "/login", Map.of("username", user, "password", "admin")).body();
        check("fail".equals(correct), "correct password was accepted during the lock: " + correct);
    }

    static void revocationAcrossInstances() throws Exception {
        var browser = new Browser();
        browser.post(A, "/login", Map.of("username", "carol", "password", "admin"));
        var stolen = browser.copy();
        check("true:true:carol".equals(stolen.get(B, "/me").body()), "copied cookie not valid on B before logout");

        browser.get(A, "/logout");
        String me = stolen.get(B, "/me").body();
        check(me.startsWith("false:false"), "revoked cookie still accepted on B: " + me);
    }

    static void totpReplayAcrossInstances() throws Exception {
        String user = "bob-" + UUID.randomUUID();
        var first = new Browser();
        var second = new Browser();
        first.post(A, "/login", Map.of("username", user, "password", "admin", "twofactor", "true"));
        second.post(B, "/login", Map.of("username", user, "password", "admin", "twofactor", "true"));
        check(first.get(A, "/me").body().startsWith("true:false"), "second factor not pending");

        String code = freshCode(USER_SECRET);
        check("ok".equals(first.post(A, "/totp", Map.of("code", code)).body()), "valid code refused on A");
        check("fail".equals(second.post(B, "/totp", Map.of("code", code)).body()), "replayed code accepted on B");
        check(first.get(B, "/me").body().startsWith("true:true"), "verified session not valid on B");
    }

    static void adminTwoFactorAcrossInstances() throws Exception {
        var browser = new Browser();
        String login = browser.get(A, "/@admin/login").body();
        var matcher = CSRF.matcher(login);
        check(matcher.find(), "no CSRF token on the admin login page");
        String csrf = matcher.group(1);

        var authenticate = browser.post(A, "/@admin/authenticate", Map.of("username", "admin", "password", "admin", "x-csrf-token", csrf));
        check(authenticate.statusCode() == 302 && "/@admin".equals(header(authenticate, "Location")), "password step failed: " + authenticate.statusCode() + " " + header(authenticate, "Location"));

        var verify = browser.post(B, "/@admin/verify", Map.of("code", freshCode(ADMIN_SECRET), "x-csrf-token", csrf));
        check(verify.statusCode() == 302 && "/@admin".equals(header(verify, "Location")), "second factor on B failed: " + verify.statusCode() + " " + header(verify, "Location"));

        var dashboard = browser.get(A, "/@admin");
        check(dashboard.statusCode() == 200 && dashboard.body().contains("Dashboard"), "dashboard on A not reachable: " + dashboard.statusCode());
    }

    static void sseAcrossInstances() throws Exception {
        try (var stream = new EventStream(B)) {
            String data = "hello-" + UUID.randomUUID();
            new Browser().post(A, "/send", Map.of("data", data));
            check(stream.await(data, Duration.ofSeconds(10)), "event sent on A did not reach the client on B");
        }
    }

    static void failover() throws Exception {
        try (MongoClient client = MongoClients.create(REPLICA_SET)) {
            status("discovering the primary");
            await("primary discovered", Duration.ofSeconds(30), () -> primaryPort(client) > 0);
            int primary = primaryPort(client);
            long pid = Long.parseLong(Files.readString(work.resolve("mongod-" + primary + ".pid")).trim());
            ProcessHandle.of(pid).orElseThrow().destroyForcibly();
            note("killed primary on " + primary + " (pid " + pid + ")");

            var start = Instant.now();
            status("waiting for a new primary");
            await("new primary", Duration.ofSeconds(60), () -> {
                int current = primaryPort(client);
                return current > 0 && current != primary;
            });
            note("new primary on " + primaryPort(client) + " after " + Duration.between(start, Instant.now()).toMillis() + " ms");
        }

        // Logins write with majority, which two of three nodes still provide
        status("waiting for logins on A and B");
        await("login on A and B after failover", Duration.ofSeconds(60), () -> loginWorks(A) && loginWorks(B));
        status("checking the shared lock");
        String user = "dave-" + UUID.randomUUID();
        for (var i = 0; i < 10; i++) {
            new Browser().post(i % 2 == 0 ? A : B, "/login", Map.of("username", user, "password", "wrong"));
        }
        check("true".equals(new Browser().get(B, "/locked/" + user).body()), "lock not shared after failover");

        // The broker thread reconnects with backoff; events in between are lost by design
        status("waiting for the message broker to reconnect");
        try (var stream = new EventStream(B)) {
            String data = "after-failover-" + UUID.randomUUID();
            await("event across instances after failover", Duration.ofSeconds(60), () -> {
                silently(() -> new Browser().post(A, "/send", Map.of("data", data)));
                return stream.await(data, Duration.ofSeconds(2));
            });
        }
    }

    static void gracefulShutdown() throws Exception {
        long pid = Long.parseLong(Files.readString(work.resolve("app-9101.pid")).trim());
        var stream = new EventStream(A);
        var slow = HTTP.sendAsync(HttpRequest.newBuilder(URI.create(A + "/slow")).build(), HttpResponse.BodyHandlers.ofString());
        Thread.sleep(500);

        ProcessHandle.of(pid).orElseThrow().destroy();
        var stopping = Instant.now();
        status("draining instance A");

        // The shutdown hook runs asynchronously, so readiness is polled until it reports the shutdown
        int ready = 0;
        int live = 0;
        while (Duration.between(stopping, Instant.now()).toMillis() < 2000 && ready != 503) {
            try {
                ready = new Browser().get(A, "/@health/ready").statusCode();
                live = new Browser().get(A, "/@health/live").statusCode();
            } catch (Exception e) {
                break;
            }
        }
        check(ready == 503, "readiness during the drain was " + ready);
        check(live == 200, "liveness during the drain was " + live);

        var response = slow.get(15, TimeUnit.SECONDS);
        check(response.statusCode() == 200 && "slow".equals(response.body()), "open request did not finish: " + response.statusCode());
        check(stream.closed(Duration.ofSeconds(5)), "SSE connection on A was not closed");
        stream.close();

        status("waiting for instance A to exit");
        check(ProcessHandle.of(pid).map(handle -> handle.onExit().orTimeout(20, TimeUnit.SECONDS).join()).isPresent(), "instance A did not exit");
        note("instance A exited " + Duration.between(stopping, Instant.now()).toMillis() + " ms after SIGTERM");
        status("checking that nginx serves B");

        for (var i = 0; i < 4; i++) {
            var instance = new Browser().get(LB, "/instance");
            check(instance.statusCode() == 200, "nginx did not fail over to B: " + instance.statusCode());
        }

        String log = Files.readString(work.resolve("app-9101.log"));
        List<String> order = List.of("Closing server sent event and websocket connections", "Waiting for open requests",
                "Stopping Undertow", "Stopping MessageBroker", "Closing MongoDB connections", "Stopping Log4j");
        int last = -1;
        for (String step : order) {
            int index = log.indexOf(step);
            check(index > last, "shutdown step out of order or missing: " + step);
            last = index;
        }
    }

    // --- helpers ---

    static boolean loginWorks(String instance) {
        try {
            return "ok".equals(new Browser().post(instance, "/login", Map.of("username", "frank", "password", "admin")).body());
        } catch (Exception e) {
            return false;
        }
    }

    static int primaryPort(MongoClient client) {
        return client.getClusterDescription().getServerDescriptions().stream()
                .filter(ServerDescription::isPrimary)
                .mapToInt(description -> description.getAddress().getPort())
                .findFirst()
                .orElse(-1);
    }

    // A code that stays valid for at least five more seconds, so the request does not cross the 30 second step
    static String freshCode(String secret) throws InterruptedException {
        long remaining = 30 - (Instant.now().getEpochSecond() % 30);
        if (remaining < 5) {
            status("waiting for the next TOTP step");
            Thread.sleep((remaining + 1) * 1000);
        }
        return TotpUtils.getTotp(secret);
    }

    static String header(HttpResponse<?> response, String name) {
        return response.headers().firstValue(name).orElse(null);
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void await(String what, Duration timeout, BooleanSupplier condition) throws InterruptedException {
        var deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (RuntimeException e) {
                // retried until the deadline
            }
            Thread.sleep(500);
        }
        throw new AssertionError("timeout waiting for " + what);
    }

    static void silently(Scenario action) {
        try {
            action.run();
        } catch (Exception e) {
            // ignored
        }
    }

    /** Keeps its own cookies and sends them to whichever instance it calls, as a browser behind a load balancer. */
    static final class Browser {
        private final Map<String, String> cookies = new LinkedHashMap<>();

        Browser copy() {
            var copy = new Browser();
            copy.cookies.putAll(cookies);
            return copy;
        }

        HttpResponse<String> get(String base, String path) throws Exception {
            return send(HttpRequest.newBuilder(URI.create(base + path)).GET());
        }

        HttpResponse<String> post(String base, String path, Map<String, String> form) throws Exception {
            String body = form.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            return send(HttpRequest.newBuilder(URI.create(base + path))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body)));
        }

        private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
            if (!cookies.isEmpty()) {
                request.header("Cookie", cookies.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("; ")));
            }
            var response = HTTP.send(request.timeout(Duration.ofSeconds(15)).build(), HttpResponse.BodyHandlers.ofString());
            for (String cookie : response.headers().allValues("Set-Cookie")) {
                String pair = cookie.split(";", 2)[0];
                int separator = pair.indexOf('=');
                String name = pair.substring(0, separator).trim();
                String value = pair.substring(separator + 1).trim();
                if (value.isEmpty() || cookie.toLowerCase(Locale.ENGLISH).contains("max-age=0")) {
                    cookies.remove(name);
                } else {
                    cookies.put(name, value);
                }
            }
            return response;
        }
    }

    /** An SSE client on /sse that collects the data lines it receives. */
    static final class EventStream implements AutoCloseable {
        private final List<String> lines = new CopyOnWriteArrayList<>();
        private final CompletableFuture<Void> ended = new CompletableFuture<>();
        private final Thread reader;
        private volatile BufferedReader input;

        EventStream(String base) throws Exception {
            var connected = new CountDownLatch(1);
            reader = Thread.ofVirtual().start(() -> {
                try {
                    var response = HTTP.send(HttpRequest.newBuilder(URI.create(base + "/sse")).build(), HttpResponse.BodyHandlers.ofInputStream());
                    input = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                    connected.countDown();
                    String line;
                    while ((line = input.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (Exception e) {
                    // the connection was closed
                } finally {
                    connected.countDown();
                    ended.complete(null);
                }
            });
            connected.await(5, TimeUnit.SECONDS);
            // The connection is registered on a virtual thread after the headers were sent
            Thread.sleep(300);
        }

        boolean await(String data, Duration timeout) {
            var deadline = Instant.now().plus(timeout);
            while (Instant.now().isBefore(deadline)) {
                if (lines.contains("data:" + data)) {
                    return true;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return false;
        }

        boolean closed(Duration timeout) {
            try {
                ended.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void close() {
            reader.interrupt();
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                // ignored
            }
        }
    }
}
