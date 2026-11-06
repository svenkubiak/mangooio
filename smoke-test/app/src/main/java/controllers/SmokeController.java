package controllers;

import io.mangoo.core.Application;
import io.mangoo.crypto.Argon2Hash;
import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Form;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Endpoints the smoke test drives; every user has the password "admin". */
public class SmokeController {
    public static final String TOTP_SECRET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final String HASH = "$argon2id$v=19$m=32768,t=3,p=1$OWE0Y2VjMTAtYzMzMi00NjdhLTkzZjEtMzY1NTk2ZTZkNTU0$iXrYbrubsyGZukmWOmR6Luu+rcRD/QxFWJzGio04XhU";
    private final ServerSentEventManager serverSentEventManager;

    @Inject
    public SmokeController(ServerSentEventManager serverSentEventManager) {
        this.serverSentEventManager = Objects.requireNonNull(serverSentEventManager);
    }

    public Response instance() {
        return Response.ok().bodyText(Application.getInstanceId());
    }

    public Response login(Form form, Authentication authentication) {
        String salt = new String(Argon2Hash.parse(HASH).salt(), StandardCharsets.UTF_8);
        if (authentication.isValidLogin(form.get("username"), form.get("password"), salt, HASH)) {
            authentication.login(form.get("username")).twoFactorAuthentication("true".equals(form.get("twofactor")));
            return Response.ok().bodyText("ok");
        }

        return Response.ok().bodyText("fail");
    }

    public Response totp(Form form, Authentication authentication) {
        if (authentication.hasSubject() && authentication.isValidSecondFactor(authentication.getSubject(), TOTP_SECRET, form.get("code"))) {
            authentication.twoFactorAuthentication(false);
            authentication.update();
            return Response.ok().bodyText("ok");
        }

        return Response.ok().bodyText("fail");
    }

    public Response me(Authentication authentication) {
        return Response.ok().bodyText(authentication.hasSubject() + ":" + authentication.isValid() + ":" + authentication.getSubject());
    }

    public Response locked(String user, Authentication authentication) {
        return Response.ok().bodyText(String.valueOf(authentication.userHasLock(user)));
    }

    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.ok().bodyText("ok");
    }

    public Response send(Form form) {
        serverSentEventManager.send("/sse", form.get("data"));
        return Response.ok().bodyText("ok");
    }

    public Response slow() throws InterruptedException {
        Thread.sleep(3000);
        return Response.ok().bodyText("slow");
    }
}
