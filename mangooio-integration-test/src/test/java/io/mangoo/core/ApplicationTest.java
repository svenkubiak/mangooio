package io.mangoo.core;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import io.mangoo.TestExtension;
import io.mangoo.enums.Mode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
public class ApplicationTest {
    @Test
    void testIsStarted() {
        //given
        boolean started = Application.isStarted();

        //then
        assertThat(started, equalTo(true));
    }

    @Test
    void testGetUptime() {
        //given
        Duration uptime = Application.getUptime();

        //then
        assertThat(uptime, not(nullValue()));
    }

    @Test
    void testRootIsForbiddenInProdMode() {
        assertThat(Application.isRootForbidden("0", Mode.PROD), equalTo(true));
        assertThat(Application.isRootForbidden("0\n", Mode.PROD), equalTo(true));
        assertThat(Application.isRootForbidden(" 0 ", Mode.PROD), equalTo(true));
    }

    @Test
    void testRootIsAllowedOutsideOfProdMode() {
        assertThat(Application.isRootForbidden("0", Mode.DEV), equalTo(false));
        assertThat(Application.isRootForbidden("0", Mode.TEST), equalTo(false));
        assertThat(Application.isRootForbidden("0", null), equalTo(false));
    }

    @Test
    void testUnprivilegedUserIsAllowedInProdMode() {
        assertThat(Application.isRootForbidden("1000", Mode.PROD), equalTo(false));
        assertThat(Application.isRootForbidden("", Mode.PROD), equalTo(false));
        assertThat(Application.isRootForbidden(null, Mode.PROD), equalTo(false));
    }

    public static class SchedulableJob {
        public void execute() {
            // Nothing to do here
        }

        public void withParameter(String value) {
            // Nothing to do here
        }

        @SuppressWarnings("unused")
        private void notPublic() {
            // Nothing to do here
        }
    }

    @Test
    void testIsSchedulable() {
        assertThat(Application.isSchedulable(SchedulableJob.class, "execute"), equalTo(true));
        assertThat(Application.isSchedulable(SchedulableJob.class, "withParameter"), equalTo(false));
        assertThat(Application.isSchedulable(SchedulableJob.class, "notPublic"), equalTo(false));
    }

    @Test
    void testClasspathScanFindsApplicationClasses() {
        //given
        try (var scanResult = Application.scanClasspath()) {

            //then
            assertThat(scanResult.getClassInfo("jobs.InfoJob"), not(nullValue()));
            assertThat(scanResult.getClassInfo("models.Person"), not(nullValue()));
            assertThat(scanResult.getClassInfo("subscribers.MySubscriber"), not(nullValue()));
            assertThat(scanResult.getClassInfo("io.mangoo.core.Application"), not(nullValue()));
            assertThat(scanResult.getClassesWithMethodAnnotation("io.mangoo.annotations.Run").getNames(), hasItem("jobs.InfoJob"));
            assertThat(scanResult.getClassesWithAnnotation("io.mangoo.annotations.Collection").getNames(), hasItem("models.Person"));
            assertThat(scanResult.getAllClassesImplementing("io.mangoo.async.Subscriber").getNames(), hasItem("subscribers.MySubscriber"));
        }
    }

    @Test
    void testClasspathScanSkipsLibraryClasses() {
        //given
        try (var scanResult = Application.scanClasspath()) {

            //when
            var scanned = scanResult.getAllClasses().getNames();

            //then
            assertThat(scanned, hasItem("io.mangoo.core.Application"));
            assertThat(scanned, not(hasItem("com.google.common.base.Preconditions")));
            assertThat(scanned, not(hasItem("io.undertow.Undertow")));
            assertThat(scanned, not(hasItem("org.bouncycastle.jce.provider.BouncyCastleProvider")));
            assertThat(scanned.size(), lessThan(5000));
        }
    }

    @Test
    void testClasspathScanDoesNotRejectApplicationPackages() {
        //given
        var rejected = Arrays.asList(Application.SCAN_REJECTED_PACKAGES);

        //then
        for (String broad : new String[] {"io", "io.mangoo", "com", "com.github", "org", "de", "de.svenkubiak", "net", "app", "controllers"}) {
            assertThat(rejected, not(hasItem(broad)));
        }
        rejected.forEach(pkg -> assertThat("io.mangoo".startsWith(pkg), equalTo(false)));
    }

    interface Greeting {
        String greet();
    }

    private static AbstractModule binding(String greeting) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Greeting.class).toInstance(() -> greeting);
            }
        };
    }

    @Test
    void testApplicationBindingOverridesCoreBinding() {
        //given
        var core = binding("core");
        var application = binding("application");

        //when
        var injector = Guice.createInjector(Application.override(core, application));

        //then
        assertThat(injector.getInstance(Greeting.class).greet(), equalTo("application"));
    }

    @Test
    void testDuplicateBindingWithoutOverrideFails() {
        //given
        var core = binding("core");
        var application = binding("application");

        //then
        assertThrows(CreationException.class, () -> Guice.createInjector(core, application));
    }

    @Test
    void testCoreBindingIsKeptWithoutApplicationBinding() {
        //given
        var core = binding("core");
        var application = new AbstractModule() {};

        //when
        var injector = Guice.createInjector(Application.override(core, application));

        //then
        assertThat(injector.getInstance(Greeting.class).greet(), equalTo("core"));
    }

    @Test
    void testInstanceId() {
        //given
        String first = Application.instanceId("web-1");
        String second = Application.instanceId("web-1");

        //then
        assertThat(first, matchesPattern("web-1-[0-9a-f]{6}"));
        assertThat(first, not(equalTo(second)));
        assertThat(Application.instanceId(" "), matchesPattern("mangooio-[0-9a-f]{6}"));
        assertThat(Application.instanceId(null), matchesPattern("mangooio-[0-9a-f]{6}"));
    }

    @Test
    void testInstanceIdIsStableForTheProcess() {
        //then
        assertThat(Application.getInstanceId(), not(emptyOrNullString()));
        assertThat(Application.getInstanceId(), equalTo(Application.getInstanceId()));
    }

    @Test
    void testIsReadyAfterStartup() {
        //then
        assertThat(Application.isReady(), equalTo(true));
    }

    @Test
    void testEmbeddedMongoDBIsOnlyWarnedInProdMode() {
        //then
        assertThat(Application.isEmbeddedInProd(Mode.PROD, true, Boolean.TRUE), equalTo(true));
        assertThat(Application.isEmbeddedInProd(Mode.PROD, true, Boolean.FALSE), equalTo(false));
        assertThat(Application.isEmbeddedInProd(Mode.PROD, false, Boolean.TRUE), equalTo(false));
        assertThat(Application.isEmbeddedInProd(Mode.PROD, true, null), equalTo(false));
        assertThat(Application.isEmbeddedInProd(Mode.TEST, true, Boolean.TRUE), equalTo(false));
        assertThat(Application.isEmbeddedInProd(Mode.DEV, true, Boolean.TRUE), equalTo(false));
    }

    private static final char[] KEYSTORE_PASSWORD = "keystore-password".toCharArray();

    private static Path keystore(String... aliases) throws Exception {
        Path directory = Files.createTempDirectory("https-keystore");
        Path keystore = directory.resolve("https.p12");
        var keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();

        for (String alias : aliases) {
            var process = new ProcessBuilder(List.of(keytool, "-genkeypair", "-alias", alias, "-keyalg", "EC",
                    "-dname", "CN=localhost", "-validity", "1", "-storetype", "PKCS12", "-keystore", keystore.toString(),
                    "-storepass", new String(KEYSTORE_PASSWORD), "-keypass", new String(KEYSTORE_PASSWORD)))
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            assertThat(process.waitFor(60, TimeUnit.SECONDS) && process.exitValue() == 0, equalTo(true));
        }

        return keystore;
    }

    @Test
    void testSslContextWithAlias() throws Exception {
        //given
        Path keystore = keystore("certificate", "other");

        //when
        var sslContext = Application.sslContext(keystore, KEYSTORE_PASSWORD, "certificate");

        //then
        assertThat(sslContext.getProtocol(), equalTo("TLSv1.3"));
    }

    @Test
    void testSslContextWithoutAliasUsesTheOnlyKeyEntry() throws Exception {
        //given
        Path keystore = keystore("certificate");

        //then
        assertThat(Application.sslContext(keystore, KEYSTORE_PASSWORD, null), not(nullValue()));
    }

    @Test
    void testSslContextWithoutAliasFailsForSeveralKeyEntries() throws Exception {
        //given
        Path keystore = keystore("certificate", "other");

        //when
        var exception = assertThrows(IllegalStateException.class, () -> Application.sslContext(keystore, KEYSTORE_PASSWORD, ""));

        //then
        assertThat(exception.getMessage(), containsString("holds 2 key entries"));
    }

    @Test
    void testSslContextWithUnknownAlias() throws Exception {
        //given
        Path keystore = keystore("certificate");

        //when
        var exception = assertThrows(IllegalStateException.class, () -> Application.sslContext(keystore, KEYSTORE_PASSWORD, "unknown"));

        //then
        assertThat(exception.getMessage(), containsString("'unknown'"));
    }

    @Test
    void testSslContextWithWrongPassword() throws Exception {
        //given
        Path keystore = keystore("certificate");

        //then
        assertThrows(IllegalStateException.class, () -> Application.sslContext(keystore, "wrong".toCharArray(), "certificate"));
    }

    @Test
    void testSslContextWithMissingFile() {
        //then
        assertThrows(IllegalStateException.class, () -> Application.sslContext(Path.of("does-not-exist.p12"), KEYSTORE_PASSWORD, null));
    }
}
