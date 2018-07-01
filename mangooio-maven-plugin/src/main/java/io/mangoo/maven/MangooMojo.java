/**
 * Copyright (C) 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mangoo.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.mangoo.build.Minification;
import io.mangoo.build.Runner;
import io.mangoo.build.Trigger;
import io.mangoo.build.Watcher;
import io.mangoo.core.Application;
import io.mangoo.utils.IOUtils;

/**
 * This is a refactored version of
 * NinjaRunMojo.java from the Ninja Web Framework
 *
 * Original source code can be found here:
 * https://github.com/ninjaframework/ninja/blob/develop/ninja-maven-plugin/src/main/java/ninja/maven/NinjaRunMojo.java
 *
 * @author svenkubiak
 *
 */
@Mojo(name = "run",
requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
defaultPhase = LifecyclePhase.NONE,
threadSafe = true)
public class MangooMojo extends AbstractMojo {
    private static final String [] DEFAULT_EXCLUDE_PATTERNS = {
            "(.*)" + Pattern.quote(File.separator) + "templates" + Pattern.quote(File.separator) + "(.*)ftl",
            "(.*)less",
            "(.*)sass",
            "(.*)" + Pattern.quote(File.separator) + "assets" + Pattern.quote(File.separator) + "(.*)"
    };

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(property = "mangoo.skip", defaultValue="false", required = true)
    private boolean skip;

    @Parameter(property = "mangoo.jpdaPort", defaultValue="8000", required = true)
    private int jpdaPort;

    @Parameter(property = "mangoo.jvmArgs", required = false)
    private String jvmArgs;

    @Parameter(property = "mangoo.outputDirectory", defaultValue = "${project.build.outputDirectory}", required = true)
    private String buildOutputDirectory;

    @Parameter(property = "mangoo.watchDirs", required = false)
    private File[] watchDirs;

    @Parameter(property = "mangoo.watchAllClassPathDirs", defaultValue = "false", required = true)
    private boolean watchAllClassPathDirs;

    @Parameter(property = "mangoo.watchAllClassPathJars", defaultValue = "false", required = true)
    private boolean watchAllClassPathJars;

    @Parameter(property = "mangoo.includes", required = false)
    protected List<String> includes;

    @Parameter(property = "mangoo.excludes", required = false)
    private List<String> excludes;

    @Parameter(property = "mangoo.useDefaultExcludes", defaultValue = "true", required = true)
    protected boolean useDefaultExcludes;

    @Parameter(property = "mangoo.settleDownMillis", defaultValue="500", required = false)
    private Long settleDownMillis;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skip flag is on. Will not execute.");
            return;
        }

        initMojo();
        checkClasses(buildOutputDirectory);

        Minification.setBasePath(project.getBasedir().getAbsolutePath());

        List<String> classpathItems = new ArrayList<>();
        classpathItems.add(buildOutputDirectory);

        for (Artifact artifact: project.getArtifacts()) {
            classpathItems.add(artifact.getFile().toString()); //NOSONAR
        }

        Set<String> includesSet = new LinkedHashSet<>(includes);
        Set<String> excludesSet = new LinkedHashSet<>(excludes);

        Set<Path> watchDirectories = new LinkedHashSet<>();
        FileSystem fileSystem = FileSystems.getDefault();
        watchDirectories.add(fileSystem.getPath(buildOutputDirectory).toAbsolutePath());

        if (this.watchDirs != null) {
            for (File watchDir: this.watchDirs) {
                watchDirectories.add(watchDir.toPath().toAbsolutePath());
            }
        }

        getArtifacts(includesSet, excludesSet, watchDirectories);
        startRunner(classpathItems, includesSet, excludesSet, watchDirectories);
        IOUtils.closeQuietly(fileSystem);
    }

    private void startRunner(List<String> classpathItems, Set<String> includesSet, Set<String> excludesSet, Set<Path> watchDirectories) {
        try {
            Runner machine = new Runner(
                    Application.class.getName(),
                    StringUtils.join(classpathItems, File.pathSeparator),
                    project.getBasedir(),
                    jpdaPort,
                    jvmArgs);

            Trigger restartTrigger = new Trigger(machine);
            restartTrigger.setSettleDownMillis(settleDownMillis);
            restartTrigger.start();

            Watcher watcher = new Watcher(
                    watchDirectories,
                    includesSet,
                    excludesSet,
                    restartTrigger);

            machine.restart();
            watcher.run(); //NOSONAR
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private void getArtifacts(Set<String> includesSet, Set<String> excludesSet, Set<Path> watchDirectories) {
        for (Artifact artifact : project.getArtifacts()) {
            File file = artifact.getFile();

            if (this.watchAllClassPathDirs && file.isDirectory()) {
                watchDirectories.add(file.toPath().toAbsolutePath());
            } else if (file.getName().endsWith(".jar") && this.watchAllClassPathJars) {
                File parentDir = file.getParentFile();
                Path parentPath = parentDir.toPath().toAbsolutePath();

                String rulePrefix = parentDir.getAbsolutePath() + File.separator;
                rulePrefix = rulePrefix.replace("\\", "\\\\");

                if (!watchDirectories.contains(parentPath)) {
                    excludesSet.add(rulePrefix + "(.*)$");
                }
                
                includesSet.add(rulePrefix + file.getName() + "$");
                watchDirectories.add(parentPath);
            } else {
                // Ignore anything else
            }
        }
    }

    private void initMojo() {
        if (useDefaultExcludes) {
            excludes.addAll(Arrays.asList(DEFAULT_EXCLUDE_PATTERNS));
        }
    }

    @SuppressWarnings("all")
    public void checkClasses(String classesDirectory) {
        if (!new File(classesDirectory).exists()) {
            getLog().error("Directory with classes does not exist: " + classesDirectory);
            System.exit(1); //NOSONAR
        }
    }
}