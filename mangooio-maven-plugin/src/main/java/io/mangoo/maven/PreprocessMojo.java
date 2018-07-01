package io.mangoo.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.mangoo.build.Minification;
import io.mangoo.enums.Default;
import io.mangoo.enums.Suffix;

/**
 * 
 * @author svenkubiak
 *
 */
@Mojo(name = "preprocess",
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
defaultPhase = LifecyclePhase.NONE,
threadSafe = true)
public class PreprocessMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String baseDir = project.getBasedir().getAbsolutePath();
        Minification.setBasePath(baseDir);
        preprocessFiles(baseDir);
    }
    
    public void preprocessFiles(String directoryName){
        StringBuilder buffer = new StringBuilder();
        buffer.append(directoryName).append('/').append(Default.FILES_PATH.toString());
        
        File directory = new File(buffer.toString()); //NOSONAR
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files){
                if (file.isFile()){
                    String fileName = file.getName();
                    if (fileName.endsWith(Suffix.LESS.toString()) || fileName.endsWith(Suffix.SASS.toString())) {
                        Minification.preprocess(file.getAbsolutePath());
                    }
                } else if (file.isDirectory()){
                    preprocessFiles(file.getAbsolutePath());
                } else {
                    // Ignore anything else
                }
            }   
        }
    }
}