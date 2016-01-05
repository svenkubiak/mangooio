package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum AdminRoute {
    ROUTES("/@routes"),
    CONFIG("/@config"),
    HEALTH("/@health"),
    CACHE("/@cache"),
    METRICS("/@metrics"),
    SCHEDULER("/@scheduler"),
    SYSTEM("/@system"),
    MEMORY("/@memory");

    private final String value;

    AdminRoute (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
    
    public static AdminRoute fromString(String value) {
        for (AdminRoute route : AdminRoute.values()) {
            if (route.toString().equalsIgnoreCase(value)) {
                return route;
            }
        }

        return null;
    }
}