package io.mangoo.maven;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@SuppressWarnings("all")
@Mojo(name = "dependencies", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.NONE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependenciesMojo extends AbstractMojo {
    private static final String BOMS = "boms.";
    private static final List BLACKLIST = List.of("rhino", "junit", "hamcrest");
    
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "dependencies.snapshotStyle", defaultValue = "TIMESTAMP")
    private SnapshotStyle snapshotStyle;
    
    private Log LOG = getLog();
    
    private enum SnapshotStyle {
        SNAPSHOT, TIMESTAMP
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (this.project.getPackaging().equals("pom")) {
            LOG.error("Depenciess goal could not be applied to pom project.");
            return;
        }
        
        Properties props = new Properties();
        try {
            Path target = Paths.get("target/dependencies.properties");
            if (Files.exists(target)) {
                Properties properties = new Properties();
                try (InputStream inputStream = Files.newInputStream(target)){
                    properties.load(inputStream);
                } catch (IOException e) {
                    LOG.error("Failed to load dependies.properties file", e);
                }
                
                for (Enumeration<?> keys = props.propertyNames(); keys.hasMoreElements();) {
                    String key = (String) keys.nextElement();
                    if (key.startsWith("dependencies.")) {
                        props.remove(key);
                    }
                    
                    if (key.startsWith(BOMS)) {
                        props.remove(key);
                    }
                }

                boms(project, props);
                Set<Artifact> artifacts = project.getArtifacts();
                for (Artifact artifact : artifacts) {
                    if ("runtime".equals(artifact.getScope()) || "compile".equals(artifact.getScope())) {
                        props.setProperty(key(artifact, props), coordinates(artifact));
                    }
                }
                
                for (Enumeration<?> keys = props.propertyNames(); keys.hasMoreElements();) {
                    String key = (String) keys.nextElement();
                    if (BLACKLIST.contains(key)) {
                        props.remove(key);
                    }
                }
                
                try (OutputStream outputStream = Files.newOutputStream(target)){
                    props.store(outputStream, "");
                    LOG.info("dependencies.properties succesfully created");
                } catch (IOException e) {
                    LOG.error("Failed to create dependies.properties file", e);
                }
            }
        }
        catch (Exception e) {
            throw new MojoExecutionException("Can not calculate dependencies for: " + this.project.getArtifact(),e);
        }
    }

    private String key(Artifact dependency, Properties props) {
        String key = dependency.getArtifactId();
        if (!StringUtils.isEmpty(dependency.getClassifier())) {
            key = key + "." + dependency.getClassifier();
        }

        int counter = 1;
        while (props.get(key) != null) {
            key = key + "." + counter++;
        }
        
        return key;
    }

    private void boms(MavenProject project, Properties props) {
        while (project != null) {
            String artifactId = project.getArtifactId();
            if (isBom(artifactId)) {
                props.setProperty(BOMS + artifactId, coordinates(project.getArtifact()));
            }
            if (project.getDependencyManagement() != null) {
                for (Dependency dependency : project.getDependencyManagement().getDependencies()) {
                    if ("import".equals(dependency.getScope())) {
                        props.setProperty(BOMS + dependency.getArtifactId(), coordinates(dependency));
                    }
                }
            }
            project = project.getParent();
        }
    }

    private boolean isBom(String artifactId) {
        return artifactId.endsWith("-dependencies") || artifactId.endsWith("-bom");
    }

    private String coordinates(Dependency dependency) {
        return dependency.getGroupId() + "|" + dependency.getArtifactId() + "|" + dependency.getVersion();
    }

    private String coordinates(Artifact artifact) {
        String classifier = artifact.getClassifier();
        String extension = artifact.getType();
        String version = snapshotStyle == SnapshotStyle.SNAPSHOT ? artifact.getBaseVersion() : artifact.getVersion();
        
        return artifact.getGroupId() + ":" + artifact.getArtifactId()
                + (hasText(extension)
                        && (!"jar".equals(extension) || hasText(classifier))
                                ? ":" + extension
                                : "")
                + (hasText(classifier) ? ":" + classifier : "")
                + ":" + version;
    }
    
    public boolean hasText (String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}