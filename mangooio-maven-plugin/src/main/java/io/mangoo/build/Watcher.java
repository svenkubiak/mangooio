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

package io.mangoo.build;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.enums.Suffix;

/**
 * This is a refactored version of
 * WatchAndRestartMachine.java from the Ninja Web Framework
 *
 * Original source code can be found here:
 * https://github.com/ninjaframework/ninja/blob/develop/ninja-maven-plugin/src/main/java/ninja/build/WatchAndRestartMachine.java
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings({"unchecked"})
public class Watcher implements Runnable {
    private static final Logger LOG = LogManager.getLogger(Watcher.class);
    private static final String PATTERN = "files";
    private static final String SEPARATOR = System.getProperty("file.separator");
    private final Trigger trigger;
    private final Set<String> includes;
    private final Set<String> excludes;
    private final WatchService watchService;
    private final Map<WatchKey, Path> watchKeys;
    private final AtomicInteger takeCount;
    private boolean shutdown;

    @SuppressWarnings("all")
    public Watcher(Set<Path> watchDirectory, Set<String> includes, Set<String> excludes, Trigger trigger) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.watchKeys = new HashMap<>();
        this.includes = includes; //NOSONAR
        this.excludes = excludes; //NOSONAR
        this.trigger = trigger;
        this.takeCount = new AtomicInteger(0);
        for (Path path: watchDirectory) {
            registerAll(path);
        }
    }

    public void doShutdown() {
        this.shutdown = true;
    }

    @SuppressWarnings("all")
    private void registerAll(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
                register(path);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SuppressWarnings("all")
    private void register(Path path) throws IOException {
        WatchKey watchKey = path.register(
                watchService,
                new WatchEvent.Kind[]{
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE
                });

        watchKeys.put(watchKey, path);
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        for (;;) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
                takeCount.incrementAndGet();
            } catch (InterruptedException e) {
                if (!shutdown) {
                    LOG.error("Unexpectedly interrupted while waiting for take()", e);
                }
                return;
            }

            Path path = watchKeys.get(watchKey);
            if (path == null) {
                LOG.error("WatchKey not recognized!!");
                continue;
            }

            handleEvents(watchKey, path);

            if (!watchKey.reset()) {
                watchKeys.remove(watchKey);
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("all")
    private void handleEvents(WatchKey watchKey, Path path) {
        for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
            WatchEvent.Kind<?> watchEventKind = watchEvent.kind();
            if (OVERFLOW.equals(watchEventKind)) {
                continue;
            }

            WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
            Path name = ev.context();
            Path child = path.resolve(name);

            if (ENTRY_MODIFY.equals(watchEventKind) && !child.toFile().isDirectory()) {
                handleNewOrModifiedFile(child);
            }

            if (ENTRY_CREATE.equals(watchEventKind)) {
                if (!child.toFile().isDirectory()) {
                    handleNewOrModifiedFile(child);
                }
                try {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        registerAll(child);
                    }
                } catch (IOException e) {
                    LOG.error("Something fishy happened. Unable to register new dir for watching", e);
                }
            }
        }
    }

    @SuppressWarnings("all")
    public void handleNewOrModifiedFile(Path path) {
        String absolutePath = path.toFile().getAbsolutePath();
        if (isPreprocess(absolutePath)){
            Minification.preprocess(absolutePath);
            String [] tempPath = absolutePath.split(PATTERN);
            Minification.minify(tempPath[0] + "files" + SEPARATOR + "assets" + SEPARATOR + "stylesheet" + SEPARATOR + StringUtils.substringAfterLast(absolutePath, "/")
                .replace(Suffix.SASS.toString(), Suffix.CSS.toString())
                .replace(Suffix.LESS.toString(), Suffix.CSS.toString()));
        }
        
        if (isAsset(absolutePath)) {
            Minification.minify(absolutePath);
        }
        
        RuleMatch match = matchRule(includes, excludes, absolutePath);
        if (match.proceed) {
            this.trigger.trigger();
        }
    }

    private boolean isAsset(String absolutePath) {
        if (StringUtils.isBlank(absolutePath)) {
            return false;
        }

        return !absolutePath.contains("min") && ( absolutePath.endsWith("css") || absolutePath.endsWith("js") );
    }
    
    private boolean isPreprocess(String absolutePath) {
        if (StringUtils.isBlank(absolutePath)) {
            return false;
        }

        return absolutePath.endsWith("sass") || absolutePath.endsWith("less");
    }

    public enum RuleType {
        NONE,
        INCLUDE,
        EXCLUDE
    }

    public static class RuleMatch {
        private final boolean proceed;

        public RuleMatch(boolean proceed) {
            this.proceed = proceed;
        }
    }

    public static RuleMatch matchRule(Set<String> includes, Set<String> excludes, String string) {
        if (includes != null) {
            for (String regex: includes) {
                if (string.matches(regex)) {
                    return new RuleMatch(true);
                }
            }
        }

        if (excludes != null) {
            for (String exclude : excludes) {
                if (string.matches(exclude)) {
                    return new RuleMatch(false);
                }
            }
        }

        return new RuleMatch(true);
    }

    public static boolean checkIfWouldBeExcluded(Set<String> patterns, String string) {
        return !matchRule(null, patterns, string).proceed;
    }
}