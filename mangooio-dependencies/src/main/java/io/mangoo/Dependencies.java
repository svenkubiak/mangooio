package io.mangoo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

@SuppressWarnings("all")
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
        
        for (Entry<Object, Object> entry : properties.entrySet()) {
            String dependency = (String) entry.getValue();
            String[] parts = dependency.split(":");

            String groupId = parts[0].replace('.', '/');
            String artifact = parts[1];
            String version = parts [2];
            String jar = artifact + "-" + version + ".jar";
            
            String url = REPO + groupId + "/" + artifact + "/" + version + "/" + jar;
            String hash = hash256(groupId + artifact);
            
            Path file = Paths.get(LIB_FOLDER + hash + "-" + version + "-" + jar);
            if (!Files.exists(file)) {
                deletePreviousVersion(hash, version);
                
                try (InputStream inputstream = new URL(url).openStream()){
                    Files.copy(inputstream, Paths.get(LIB_FOLDER + hash + "-" + version + "-" + jar), StandardCopyOption.REPLACE_EXISTING);   
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deletePreviousVersion(String hash, String version) {
        try (Stream<Path> stream = Files.list(Paths.get(LIB_FOLDER))) {
            stream.filter(path -> path.toFile().getName().startsWith(hash) && !path.toFile().getName().startsWith(hash + "-" + version))
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
    
    private static String hash256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return "";
    }
}