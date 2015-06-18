package mangoo.io.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

/**
 *
 * @author svenkubiak
 *
 */
public final class ThrowableUtils {

    private ThrowableUtils() {
    }
    
    public static String getSourceCodePath(StackTraceElement stackTraceElement) {
        String packageName = stackTraceElement.getClassName();
        int position = packageName.lastIndexOf('.');
        if (position > 0) {
            packageName = packageName.substring(0, position);
            return packageName.replace(".", File.separator) + File.separator + stackTraceElement.getFileName();
        } else {
            return stackTraceElement.getFileName();
        }
    }

    @SuppressWarnings("all")
    public static List<Source> getSources(int errorLine, String sourcePath) throws Exception {
        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir"))
            .append(File.separator)
            .append("src")
            .append(File.separator)
            .append("main")
            .append(File.separator)
            .append("java");
        
        File templateFile = new File(buffer.toString()).toPath().resolve(sourcePath).toFile();
        List<String> lines = IOUtils.readLines(new FileInputStream(templateFile), Charsets.UTF_8);

        int index = 0;
        List<Source> sources = new ArrayList<Source>();
        for (String line : lines) {
            if ( (index + 5 > errorLine) && (index - 3 < errorLine) ) {
                sources.add(new Source((index + 1) == errorLine, index + 1, line));
            }
            index++;
        }

        return sources;
    }
}