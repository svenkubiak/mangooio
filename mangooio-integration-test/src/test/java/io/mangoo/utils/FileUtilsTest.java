package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void testGetMimeTypeWithByteArray() {
        //given
        byte[] data = "Hello World".getBytes();

        //when
        String mimeType = FileUtils.getMimeType(data);

        //then
        assertThat(mimeType, not(nullValue()));
        assertThat(mimeType, not(emptyString()));
    }

    @Test
    void testGetMimeTypeWithByteArrayNull() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> FileUtils.getMimeType((byte[]) null));
    }

    @Test
    void testGetMimeTypeWithInputStream() {
        //given
        InputStream data = new ByteArrayInputStream("Hello World".getBytes());

        //when
        String mimeType = FileUtils.getMimeType(data);

        //then
        assertThat(mimeType, not(nullValue()));
        assertThat(mimeType, not(emptyString()));
    }

    @Test
    void testGetMimeTypeWithInputStreamNull() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> FileUtils.getMimeType((InputStream) null));
    }

    @Test
    void testGetMimeTypeWithEmptyInputStream() {
        //given
        InputStream data = new ByteArrayInputStream(new byte[0]);

        //when
        String mimeType = FileUtils.getMimeType(data);

        //then
        assertThat(mimeType, not(nullValue()));
    }

    @Test
    void testCloseQuietlyWithValidCloseable() {
        //given
        TestCloseable closeable = new TestCloseable();

        //when
        FileUtils.closeQuietly(closeable);

        //then
        assertThat(closeable.isClosed(), is(true));
    }

    @Test
    void testCloseQuietlyWithNullCloseable() {
        //given
        // No setup needed for null input

        //when
        FileUtils.closeQuietly(null);

        //then
        // Should not throw exception
    }

    @Test
    void testCloseQuietlyWithCloseableThatThrowsException() {
        //given
        TestCloseable closeable = new TestCloseable();
        closeable.setShouldThrowException(true);

        //when
        FileUtils.closeQuietly(closeable);

        //then
        // Should not throw exception, but should log error
        assertThat(closeable.isClosed(), is(true));
    }

    @Test
    void testReadableFileSizeWithZeroSize() {
        //given
        long size = 0;

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("0"));
    }

    @Test
    void testReadableFileSizeWithNegativeSize() {
        //given
        long size = -1;

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("0"));
    }

    @Test
    void testReadableFileSizeWithBytes() {
        //given
        long size = 500;

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("500 B"));
    }

    @Test
    void testReadableFileSizeWithKilobytes() {
        //given
        long size = 1536; // 1.5 KB

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("1,5 kB"));
    }

    @Test
    void testReadableFileSizeWithMegabytes() {
        //given
        long size = 2 * 1024 * 1024; // 2 MB

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("2 MB"));
    }

    @Test
    void testReadableFileSizeWithGigabytes() {
        //given
        long size = 3L * 1024 * 1024 * 1024; // 3 GB

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("3 GB"));
    }

    @Test
    void testReadableFileSizeWithTerabytes() {
        //given
        long size = 4L * 1024 * 1024 * 1024 * 1024; // 4 TB

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("4 TB"));
    }

    @Test
    void testReadableFileSizeWithLargeSize() {
        //given
        long size = 1024 * 1024 * 1024 + 512 * 1024 * 1024; // 1.5 GB

        //when
        String readableSize = FileUtils.readableFileSize(size);

        //then
        assertThat(readableSize, equalTo("1,5 GB"));
    }

    @Test
    void testReadFileToStringWithPath() throws IOException {
        //given
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello World\nThis is a test file";
        Files.write(testFile, content.getBytes());

        //when
        String result = FileUtils.readFileToString(testFile);

        //then
        assertThat(result, equalTo(content));
    }

    @Test
    void testReadFileToStringWithPathEmptyFile() throws IOException {
        //given
        Path testFile = tempDir.resolve("empty.txt");
        Files.createFile(testFile);

        //when
        String result = FileUtils.readFileToString(testFile);

        //then
        assertThat(result, equalTo(""));
    }

    @Test
    void testReadFileToStringWithPathNonExistentFile() {
        //given
        Path nonExistentFile = tempDir.resolve("nonexistent.txt");

        //when
        String result = FileUtils.readFileToString(nonExistentFile);

        //then
        assertThat(result, equalTo(""));
    }

    @Test
    void testReadFileToStringWithPathNull() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> FileUtils.readFileToString((Path) null));
    }

    @Test
    void testReadFileToStringWithString() throws IOException {
        //given
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello World\nThis is a test file";
        Files.write(testFile, content.getBytes());
        String filePath = testFile.toString();

        //when
        String result = FileUtils.readFileToString(filePath);

        //then
        assertThat(result, equalTo(content));
    }

    @Test
    void testReadFileToStringWithStringEmptyFile() throws IOException {
        //given
        Path testFile = tempDir.resolve("empty.txt");
        Files.createFile(testFile);
        String filePath = testFile.toString();

        //when
        String result = FileUtils.readFileToString(filePath);

        //then
        assertThat(result, equalTo(""));
    }

    @Test
    void testReadFileToStringWithStringNonExistentFile() {
        //given
        String filePath = tempDir.resolve("nonexistent.txt").toString();

        //when
        String result = FileUtils.readFileToString(filePath);

        //then
        assertThat(result, equalTo(""));
    }

    @Test
    void testReadFileToStringWithStringNull() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> FileUtils.readFileToString((String) null));
    }

    @Test
    void testReadFileToStringWithStringEmptyPath() {
        //given
        String filePath = "";

        //when
        String result = FileUtils.readFileToString(filePath);

        //then
        assertThat(result, equalTo(""));
    }

    @Test
    void testReadFileToStringWithUnicodeContent() throws IOException {
        //given
        Path testFile = tempDir.resolve("unicode.txt");
        String content = "Hello 世界\nThis is a test file with unicode: 🚀";
        Files.write(testFile, content.getBytes());

        //when
        String result = FileUtils.readFileToString(testFile);

        //then
        assertThat(result, equalTo(content));
    }

    @Test
    void testReadFileToStringWithLargeFile() throws IOException {
        //given
        Path testFile = tempDir.resolve("large.txt");
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            content.append("Line ").append(i).append("\n");
        }
        Files.write(testFile, content.toString().getBytes());

        //when
        String result = FileUtils.readFileToString(testFile);

        //then
        assertThat(result, equalTo(content.toString()));
    }

    // Helper class for testing closeable behavior
    private static class TestCloseable implements Closeable {
        private boolean closed = false;
        private boolean shouldThrowException = false;

        @Override
        public void close() throws IOException {
            closed = true;
            if (shouldThrowException) {
                throw new IOException("Test exception");
            }
        }

        public boolean isClosed() {
            return closed;
        }

        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }
    }
}
