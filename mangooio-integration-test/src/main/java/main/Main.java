package main;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

/**
 *
 * @author svenkubiak
 *
 */
public final class Main {

    private Main(){
    }

    public static void main(String... args) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException {
        //Vault vault = new Vault();
        Application.start(Mode.DEV);


    }
}