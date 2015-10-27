package io.mangoo.test;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooBrowser extends MangooResponse {
    public static MangooBrowser open() {
        return new MangooBrowser();
    }
}