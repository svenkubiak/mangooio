package io.mangoo.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
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

@Mojo(name = "dependencies", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true, requiresDependencyResolution = ResolutionScope.NONE, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependenciesMojo extends AbstractMojo {
    private static final String BOMS = "boms.";
    
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "target", required = true, property = "dependencies.properties")
    private File outputDirectory;

    @Parameter(property = "dependencies.compute", defaultValue = "true", required = true)
    private boolean compute;

    @Parameter(property = "dependencies.snapshotStyle", defaultValue = "TIMESTAMP")
    private SnapshotStyle snapshotStyle;
    
    private Log LOG = getLog();
    
    private enum SnapshotStyle {
        SNAPSHOT, TIMESTAMP
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (this.project.getPackaging().equals("pom")) {
            LOG.error("Thin properties goal could not be applied to pom project.");
            return;
        }
        
        Properties props = new Properties();
        outputDirectory.mkdirs();
        try {
            File target = new File(outputDirectory, "dependencies.properties");
            if (target.exists()) {
                props.load(new FileInputStream(target));
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
            Set<Artifact> artifacts = this.compute ? project.getArtifacts() : project.getArtifacts();
            for (Artifact artifact : artifacts) {
                if ("runtime".equals(artifact.getScope()) || "compile".equals(artifact.getScope())) {
                    props.setProperty(key(artifact, props), coordinates(artifact));
                }
            }
            
            for (Enumeration<?> keys = props.propertyNames(); keys.hasMoreElements();) {
                String key = (String) keys.nextElement();
                if (key.equals("rhino")) {
                    props.remove(key);
                }
                
                if (key.equals("junit")) {
                    props.remove(key);
                }
            }
            
            props.store(new FileOutputStream(target),"");
            
            
            
            LOG.info("Dependencies file succesfully created");
        }
        catch (Exception e) {
            throw new MojoExecutionException("Cannot calculate dependencies for: " + this.project.getArtifact(),e);
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
                props.setProperty(BOMS + artifactId,
                        coordinates(project.getArtifact(), true));
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

    private String coordinates(Artifact dependency) {
        return coordinates(dependency, this.compute);
    }

    private String coordinates(Artifact artifact, boolean withVersion) {
        String classifier = artifact.getClassifier();
        String extension = artifact.getType();
        String version = snapshotStyle.equals(SnapshotStyle.SNAPSHOT) ? artifact.getBaseVersion() : artifact.getVersion();
        
        return artifact.getGroupId() + ":" + artifact.getArtifactId()
                + (hasText(extension)
                        && (!"jar".equals(extension) || hasText(classifier))
                                ? ":" + extension
                                : "")
                + (hasText(classifier) ? ":" + classifier : "")
                + (withVersion ? ":" + version : "");
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