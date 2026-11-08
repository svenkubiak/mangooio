package io.mangoo.maven;

import io.mangoo.constants.Const;
import io.mangoo.constants.Key;
import io.mangoo.crypto.Argon2Settings;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/**
 * Reads a password from the terminal and prints an Argon2id hash for {@code application.admin.password}.
 * The hash uses {@code authentication.hashing.memory}, {@code authentication.hashing.iterations} and
 * {@code authentication.hashing.parallelism} from {@code src/main/resources/config.yaml}.
 * {@code -Dapplication.mode} selects the environment ({@code dev} when unset, unknown values use {@code prod}).
 * {@code -Dapplication.config} reads another config file.
 */
@Mojo(name = "hash", requiresProject = true, threadSafe = true)
public class HashMojo extends AbstractMojo {
    private static final String INDENT = "  ";

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        var settings = readSettings();
        var console = System.console();
        if (console == null) {
            throw new MojoExecutionException("Run mvn mangooio:hash from a terminal so the password is not echoed");
        }

        console.printf("%n%sAdmin password%n%n", INDENT);
        char[] password = console.readPassword("%s%-18s", INDENT, "Password:");
        char[] repeated = console.readPassword("%s%-18s", INDENT, "Repeat password:");
        try {
            String cleartext = HashingSettings.cleartext(password, repeated);
            String hash = CommonUtils.hashArgon2(cleartext, CommonUtils.uuidV4(), settings);
            console.printf("%n%s%s%n%n", INDENT, hash);
        } catch (IllegalArgumentException | MangooHashingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            wipe(password);
            wipe(repeated);
        }
    }

    private Argon2Settings readSettings() throws MojoExecutionException {
        try (var input = openConfig()) {
            return HashingSettings.read(input, mode());
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("Config file can not be read", e);
        }
    }

    private InputStream openConfig() throws MojoExecutionException {
        String configured = System.getProperty(Key.APPLICATION_CONFIG);
        Path path = StringUtils.isNotBlank(configured)
                ? Path.of(configured)
                : project.getBasedir().toPath().resolve("src").resolve("main").resolve("resources").resolve(Const.CONFIG_FILE);

        if (!Files.isRegularFile(path)) {
            throw new MojoExecutionException("Config file not found: " + path);
        }

        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new MojoExecutionException("Config file can not be read: " + path, e);
        }
    }

    private static String mode() {
        String property = System.getProperty(Key.APPLICATION_MODE);
        if (StringUtils.isBlank(property)) {
            return "dev";
        }

        return switch (property.toLowerCase(Locale.ENGLISH)) {
            case "dev" -> "dev";
            case "test" -> "test";
            default -> "prod";
        };
    }

    private static void wipe(char[] value) {
        if (value != null) {
            Arrays.fill(value, '\0');
        }
    }
}
