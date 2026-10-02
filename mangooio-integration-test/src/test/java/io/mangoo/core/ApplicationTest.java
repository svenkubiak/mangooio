package io.mangoo.core;

import io.mangoo.TestExtension;
import io.mangoo.enums.Mode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
            assertThat(scanResult.getClassesImplementing("io.mangoo.async.Subscriber").getNames(), hasItem("subscribers.MySubscriber"));
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
}
