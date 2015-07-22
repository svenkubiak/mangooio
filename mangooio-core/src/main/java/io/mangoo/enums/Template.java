package io.mangoo.enums;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Default HTML templates for standard HTTP responses
 *
 * @author svenkbiak
 *
 */
public enum Template {
    DEFAULT;
    private transient Logger LOG = LoggerFactory.getLogger(Template.class); //NOSONAR
    private String notFoundContent;
    private String badRequestContent;
    private String unauthorizedContent;
    private String forbiddenContent;
    private String serverErrorContent;

    Template () {
        try {
            this.badRequestContent = Resources.toString(Resources.getResource("defaults/400.html"), Charsets.UTF_8);
            this.unauthorizedContent = Resources.toString(Resources.getResource("defaults/401.html"), Charsets.UTF_8);
            this.forbiddenContent = Resources.toString(Resources.getResource("defaults/403.html"), Charsets.UTF_8);
            this.notFoundContent = Resources.toString(Resources.getResource("defaults/404.html"), Charsets.UTF_8);
            this.serverErrorContent = Resources.toString(Resources.getResource("defaults/500.html"), Charsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to load default templates", e);
        }
    }

    public String forbidden() {
        return this.forbiddenContent;
    }

    public String notFound() {
        return this.notFoundContent;
    }

    public String badRequest() {
        return this.badRequestContent;
    }

    public String unauthorized() {
        return this.unauthorizedContent;
    }

    public String internalServerError() {
        return this.serverErrorContent;
    }
}