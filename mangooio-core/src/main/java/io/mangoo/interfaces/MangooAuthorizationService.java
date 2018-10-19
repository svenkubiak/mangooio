package io.mangoo.interfaces;

/**
 * 
 * @author svenkubiak
 *
 */
public interface MangooAuthorizationService {
    /**
     * Validates the authorization for a give subject on a given resource with a given operation
     * 
     * @param subject The subject, e.g. username, to check
     * @param resource The resource, e.g. ApplicationController:write, to check
     * @param operation The operation, e.g. read, to check
     * 
     * @return true if authorization is valid, false otherwise
     */
    boolean validAuthorization(String subject, String resource, String operation);
}