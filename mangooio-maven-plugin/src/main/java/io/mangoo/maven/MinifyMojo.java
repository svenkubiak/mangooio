package io.mangoo.maven;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.build.Minification;
import io.mangoo.enums.Suffix;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * 
 * @author svenkubiak
 *
 */
@Mojo(name = "minify",
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
defaultPhase = LifecyclePhase.NONE,
threadSafe = true)
public class MinifyMojo extends AbstractMojo {
    private static final String FILES_PATH = "src/main/resources/files/";

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String baseDir = project.getBasedir().getAbsolutePath();
        Minification.setBasePath(baseDir);
        minifyFiles(baseDir);
    }
    
    @SuppressFBWarnings(value = "ISB_TOSTRING_APPENDING", justification = "toString is called on an enum")
    public void minifyFiles(String directoryName){
        var buffer = new StringBuilder();
        buffer.append(directoryName).append('/').append(FILES_PATH);
        
        File directory = new File(buffer.toString()); //NOSONAR
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files){
                if (file.isFile()){
                    String fileName = file.getName();
                    if (fileName.endsWith(Suffix.CSS.toString()) || (fileName.endsWith(Suffix.JS.toString()) && !fileName.contains("min"))) {
                        Minification.minify(file.getAbsolutePath());
                    }
                } else if (file.isDirectory()){
                    minifyFiles(file.getAbsolutePath());
                } else {
                    // Ignore anything else
                }
            }    
        }
    }
}