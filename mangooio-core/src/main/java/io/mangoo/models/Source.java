package io.mangoo.models;

/**
 * Base class for holding exception information
 *
 * @author svenkubiak
 *
 */
public record Source(boolean cause, int line, String content) {}