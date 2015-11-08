package io.mangoo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import io.mangoo.models.Source;

/**
 *
 * @author svenkubiak
 *
 */
public final class ThrowableUtils {

    private ThrowableUtils() {
    }

    /**
     * Retrieves the source code file name from an StrackTraceElement
     *
     * @param stackTraceElement The StrackTraceElement to check
     * @return Source code filename
     */
    public static String getSourceCodePath(StackTraceElement stackTraceElement) {
        Preconditions.checkNotNull(stackTraceElement, "stackTraceElement can not be null");

        String packageName = stackTraceElement.getClassName();
        int position = packageName.lastIndexOf('.');
        if (position > 0) {
            packageName = packageName.substring(0, position);
            return packageName.replace(".", File.separator) + File.separator + stackTraceElement.getFileName();
        }

        return stackTraceElement.getFileName();
    }

    /**
     * Retrieves the lines of code where an exception occurred
     *
     * @param errorLine The line number of the exception
     * @param sourcePath The path to the source code file
     * @return A list of source code with the exception and surrounding lines
     *
     * @throws FileNotFoundException If the file is not found
     * @throws IOException If an IO exception occurs
     */
    @SuppressWarnings("all")
    public static List<Source> getSources(int errorLine, String sourcePath) throws FileNotFoundException, IOException {
        Preconditions.checkNotNull("sourcePath", "sourcePath can not be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir"))
        .append(File.separator)
        .append("src")
        .append(File.separator)
        .append("main")
        .append(File.separator)
        .append("java");

        List<Source> sources = new ArrayList<Source>();
        File templateFile = new File(buffer.toString()).toPath().resolve(sourcePath).toFile();
        if (templateFile.exists()) {
            List<String> lines = IOUtils.readLines(new FileInputStream(templateFile), Charsets.UTF_8);

            int index = 0;
            for (String line : lines) {
                if ( (index + 8 > errorLine) && (index - 6 < errorLine) ) {
                    sources.add(new Source((index + 1) == errorLine, index + 1, line));
                }
                index++;
            }
        }

        return sources;
    }
}