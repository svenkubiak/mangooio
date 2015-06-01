package mangoo.io.interfaces;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooAuthenticator {
    public boolean validCredentials (String username, String password);
}