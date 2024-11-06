package main;

import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.PasetoLocal;

import java.util.Base64;

/**
 *
 * @author svenkubiak
 *
 */
public final class Main {

    private Main(){
    }

    public static void main(String... args) {
        //Application.start(Mode.DEV);


        String payload = "{"
                + "\"sub\":\"user123\","
                + "\"name\":\"John Doe\","
                + "\"exp\":\"2025-01-01T00:00:00+00:00\""
                + "}";

        byte[] symmetricKey = Base64.getDecoder().decode("Jb3MenqcohfVQNi3mYBBoYGk8JQ94u5cvEWz9rZMR1EED9t3l8eWwuF1jyXLDzvvZpMznuWeOlnfcNss0qlimeK8MA5McCCPeo2fhC");
        SecretKey secretKey = new SecretKey(symmetricKey, Version.V4);

        String encryptedToken = PasetoLocal.encrypt(secretKey, payload, "footer", "");

        System.out.println("Encrypted Token: " + encryptedToken);

    }
}