package io.mangoo.constants;

import java.util.regex.Pattern;

public final class Const {
    public static final Pattern NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(.*?)\\}");
    public static final String INVOKE_CONTROLLER = "Invoke controller";
    public static final String FRAMEWORK = "io.mangoo";
    public static final String CSRF_TOKEN = "x-csrf-token";
    public static final String CONFIG_FILE = "config.yaml";
    public static final String[] SCAN_REJECTED_PACKAGES = {
            "com.bastiaanjansen", "com.cronutils", "com.fasterxml", "com.github.benmanes", "com.google", "com.icegreen",
            "com.launchdarkly", "com.mongodb", "com.nimbusds", "com.sun", "de.flapdoodle",
            "de.svenkubiak.embeddedmongodb", "edu.umd", "freemarker", "io.github.classgraph", "io.micrometer",
            "io.netty", "io.opentelemetry", "io.smallrye", "io.undertow", "jakarta", "javassist", "javax",
            "jersey.repackaged", "kotlin", "net.bytebuddy", "net.glxn", "net.jcip",
            "okhttp3", "okio", "org.aopalliance", "org.apache", "org.apiguardian",
            "org.awaitility", "org.bouncycastle", "org.bson", "org.cactoos", "org.codehaus", "org.commonmark",
            "org.eclipse", "org.exparity", "org.glassfish", "org.hamcrest", "org.hibernate", "org.jboss", "org.jgrapht",
            "org.jheaps", "org.jspecify", "org.junit", "org.jvnet", "org.llorllale", "org.mockito", "org.objenesis",
            "org.ocpsoft", "org.opentest4j", "org.reactivestreams", "org.reflections", "org.slf4j", "org.wildfly",
            "org.xnio", "org.yaml"
    };

    private Const() {}
}
