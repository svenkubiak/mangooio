package io.mangoo.crypto;

import io.mangoo.TestExtension;
import io.mangoo.constants.Key;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Mode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
class VaultTest {
    private static final List<String> KEYS = List.of(
            Key.AUTHENTICATION_COOKIE_SECRET,
            Key.AUTHENTICATION_COOKIE_KEY,
            Key.SESSION_COOKIE_SECRET,
            Key.SESSION_COOKIE_KEY,
            Key.FLASH_COOKIE_SECRET,
            Key.FLASH_COOKIE_KEY);

    @Test
    void testPutWithEmptyValueKeepsVaultFile() throws IOException {
        //given
        Vault vault = Application.getInstance(Vault.class);
        byte[] before = Files.readAllBytes(vault.getPath());

        //when
        assertThrows(IllegalArgumentException.class, () -> vault.put("vaulttest.empty", ""));

        //then
        assertThat(Files.readAllBytes(vault.getPath()), equalTo(before));
        assertThat(new Vault().get(Key.SESSION_COOKIE_SECRET), not(emptyOrNullString()));
    }

    @Test
    void testPutIsPersisted() throws IOException {
        //given
        Vault vault = Application.getInstance(Vault.class);

        //when
        vault.put("vaulttest.value", "westeros");

        //then
        assertThat(new Vault().get("vaulttest.value"), equalTo("westeros"));
        assertThat(tempFiles(vault.getPath()), empty());
    }

    @Test
    void testStartupDoesNotRewriteVaultFile() throws Exception {
        //given a vault without the secrets of another mode, as a vault that has only been used in dev mode
        Path path = Application.getInstance(Vault.class).getPath();
        KeyStore keyStore = load(path);
        keyStore.deleteEntry("prod." + Key.SESSION_COOKIE_SECRET);
        try (var outputStream = Files.newOutputStream(path)) {
            keyStore.store(outputStream, vaultSecret());
        }

        //when
        new Vault();
        byte[] afterFirstStart = Files.readAllBytes(path);
        new Vault();

        //then the missing secret is created once and the next start writes nothing
        assertThat(load(path).containsAlias("prod." + Key.SESSION_COOKIE_SECRET), equalTo(true));
        assertThat(Files.readAllBytes(path), equalTo(afterFirstStart));
    }

    @Test
    void testSecretsExistForEveryModeWithoutDoubledPrefix() throws Exception {
        //given
        Vault vault = Application.getInstance(Vault.class);
        KeyStore keyStore = load(vault.getPath());
        List<String> aliases = Collections.list(keyStore.aliases());

        //then
        for (Mode mode : Mode.values()) {
            String prefix = mode.toString().toLowerCase(Locale.ENGLISH) + ".";
            for (String key : KEYS) {
                assertThat(aliases, hasItem(prefix + key));
            }
            for (Mode inner : Mode.values()) {
                String doubled = prefix + inner.toString().toLowerCase(Locale.ENGLISH) + ".";
                assertThat(aliases.stream().noneMatch(alias -> alias.startsWith(doubled)), equalTo(true));
            }
        }
    }

    @Test
    void testEmptyVaultFileIsCreatedAnew() throws Exception {
        Path path = Application.getInstance(Vault.class).getPath();
        byte[] original = Files.readAllBytes(path);
        try {
            //given an empty vault file as left behind by a failed creation
            Files.write(path, new byte[0]);

            //when
            Vault vault = new Vault();

            //then
            assertThat(Files.size(path), greaterThan(0L));
            assertThat(vault.get(Key.SESSION_COOKIE_SECRET), not(emptyOrNullString()));
            assertThat(load(path).containsAlias("prod." + Key.SESSION_COOKIE_SECRET), equalTo(true));
        } finally {
            Files.write(path, original);
        }
    }

    @Test
    void testMissingVaultFileIsCreatedOwnerOnly() throws Exception {
        Path path = Application.getInstance(Vault.class).getPath();
        byte[] original = Files.readAllBytes(path);
        try {
            //given
            Files.delete(path);

            //when
            new Vault();

            //then
            assertThat(Files.exists(path), equalTo(true));
            assertThat(tempFiles(path), empty());
            if (Files.getFileStore(path.toAbsolutePath().getParent()).supportsFileAttributeView(PosixFileAttributeView.class)) {
                assertThat(Files.getPosixFilePermissions(path), equalTo(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
            }
        } finally {
            Files.write(path, original);
        }
    }

    private static KeyStore load(Path path) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (var inputStream = Files.newInputStream(path)) {
            keyStore.load(inputStream, vaultSecret());
        }

        return keyStore;
    }

    private static char[] vaultSecret() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_VAULT_SECRET).toCharArray();
    }

    private static List<Path> tempFiles(Path vaultPath) throws IOException {
        try (Stream<Path> files = Files.list(vaultPath.toAbsolutePath().getParent())) {
            return files
                    .filter(file -> file.getFileName().toString().startsWith(".vault"))
                    .filter(file -> file.getFileName().toString().endsWith(".tmp"))
                    .toList();
        }
    }
}
