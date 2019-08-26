package io.mangoo.filters.cors;

/**
 * Defines the interface for policy classes.
 * 
 * <p>A Policy class should implement this interface and have a constructor that accepts a String argument.</p>
 */
public interface CorsPolicy {
    
    /**
     * Indicates whether the given {@code origin} should be allowed access.
     * 
     * @param origin The origin String, may be {@code null}.
     * 
     * @return {@code true} if the origin is allowed (and CORS headers should be added), or {@code false} otherwise.
     */
    boolean isAllowed(String origin);
}