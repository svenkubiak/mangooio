package mangoo.io.interfaces;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooAuthenticator {
    /**
     * Validates given credentials
     *
     * @param username The user name to validate
     * @param password The clear text password to validate
     *
     * @return True if authentication was successful, false otherwise
     */
    public boolean validCredentials (String username, String password);
}