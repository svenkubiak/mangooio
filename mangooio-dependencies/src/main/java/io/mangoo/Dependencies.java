package io.mangoo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class Dependencies {
    private static final String REPO = "https://repo.maven.apache.org/maven2/";
    private static final String WORKDIR = System.getProperty("user.dir");
    private static final String DEPENDENCIES_FILE = WORKDIR + "/dependencies.properties";
    private static final String LIB_FOLDER = WORKDIR + "/lib/";
    
    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(DEPENDENCIES_FILE))){
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Set<Entry<Object, Object>> entries = properties.entrySet();
        for (Entry<Object, Object> entry : entries) {
            String dependency = (String) entry.getValue();
            String[] parts = dependency.split(":");

            String groupId = parts[0].replace(".", "/");
            String artifact = parts[1];
            String version = parts [2];
            String jar = artifact + "-" + version + ".jar";
            
            String url = REPO + groupId + "/" + artifact + "/" + version + "/" + jar;
            
            Path file = Paths.get(LIB_FOLDER + jar);
            if (!Files.exists(file)) {
                deletePreviousVersion(artifact);
                
                try (InputStream inputstream = new URL(url).openStream();){
                    Files.copy(inputstream, Paths.get(LIB_FOLDER + jar), StandardCopyOption.REPLACE_EXISTING);   
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deletePreviousVersion(String artifact) {
        try (Stream<Path> stream = Files.list(Paths.get(LIB_FOLDER))) {
            stream.filter(path -> path.toFile().getName().contains(artifact))
                  .forEach(c -> {
                  try {
                      Files.delete(c);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}