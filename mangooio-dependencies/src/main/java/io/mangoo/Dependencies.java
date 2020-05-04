package io.mangoo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        try (InputStream inputStream = Files.newInputStream(Paths.get(DEPENDENCIES_FILE))) { //NOSONAR
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace(); //NOSONAR
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
            
            Path file = Paths.get(LIB_FOLDER + hash + "-" + version + "-" + jar); //NOSONAR
            if (!Files.exists(file)) {
                deletePreviousVersion(hash, version);
                
                try (InputStream inputstream = new URL(url).openStream()) { //NOSONAR
                    Files.copy(inputstream, Paths.get(LIB_FOLDER + hash + "-" + version + "-" + jar), StandardCopyOption.REPLACE_EXISTING); //NOSONAR    
                } catch (IOException e) {
                    e.printStackTrace(); //NOSONAR
                }
            }
        }
    }

    private static void deletePreviousVersion(String hash, String version) {
        try (Stream<Path> stream = Files.list(Paths.get(LIB_FOLDER))) { //NOSONAR
            stream.filter(path -> path.toFile().getName().startsWith(hash) && !path.toFile().getName().startsWith(hash + "-" + version))
                  .forEach(c -> {
                  try {
                      Files.delete(c);
                  } catch (IOException e) {
                      e.printStackTrace(); //NOSONAR
                  }
            });
        } catch (IOException e) {
            e.printStackTrace(); //NOSONAR
        }
    }
    
    private static String hash256(String input) {
        String hash = "";
        try { 
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8)); 
            BigInteger no = new BigInteger(1, messageDigest); 
            hash = no.toString(16); 
            while (hash.length() < 32) { 
                hash = "0" + hash; 
            } 
        } catch (NoSuchAlgorithmException e) { 
            e.printStackTrace(); //NOSONAR
        } 
        
        return hash;
    }
}