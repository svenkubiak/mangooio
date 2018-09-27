package io.mangoo.routing.handlers;

import java.util.Objects;

import com.google.inject.Inject;

import biz.gabrys.lesscss.compiler.StringUtils;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.services.AuthorizationService;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthorizationHandler implements HttpHandler {
    private Config config;
    private AuthorizationService authorizationService;
    
    @Inject
    public AuthorizationHandler(Config config, AuthorizationService authorizationService) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        this.authorizationService = Objects.requireNonNull(authorizationService, Required.AUTHORIZATION_SERVICE.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        
        if (attachment.hasAuthorization()) {
            Authentication authentication = attachment.getAuthentication();
            
            if (authentication != null && authentication.isValid()) {
                String subject = authentication.getSubject();
                String resource = attachment.getControllerAndMethod();
                String operation = getOperation(exchange.getRequestMethod());
                
                if (isNotBlank(subject, resource, operation) && this.authorizationService.validAuthorization(subject, resource, operation)) {
                    nextHandler(exchange);
                } else {
                    endRequest(exchange);
                }
            } else {
                endRequest(exchange);    
            }
        } else {
            nextHandler(exchange); 
        }
    }
    
    /**
     * Return if a given HTTP method results in a read or write request to a resource
     * 
     * GET = read
     * POST = write
     * PUT = write
     * DELETE = write
     * PATCH = write
     * OPTIONS = read
     * HEAD = read
     * 
     * @param method The HTTP method
     * @return read or write if HTTP method is found, blank otherwise
     */
    private String getOperation(HttpString method) {
        String operation = "";
        
        if (Methods.POST.equals(method)) {
            operation = "write";
        } else if (Methods.PUT.equals(method)) {
            operation = "write";
        } else if (Methods.DELETE.equals(method)) {
            operation = "write";
        } else if (Methods.GET.equals(method)) {
            operation = "read";
        } else if (Methods.PATCH.equals(method)) {
            operation = "write";
        } else if (Methods.OPTIONS.equals(method)) {
            operation = "read";
        } else if (Methods.HEAD.equals(method)) {
            operation = "read";
        }
        
        return operation;
    }

    /**
     * Checks if any of the given strings is blank
     * 
     * @param subject The subject to validate
     * @param resource The resource to validate
     * @param operation The operation to validate
     * 
     * @return True if all strings are not blank, false otherwise
     */
    private boolean isNotBlank(String subject, String resource, String operation) {
        return StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(resource) && StringUtils.isNotBlank(operation);
    }

    /**
     * Ends the current request by sending a HTTP 401 status code and the default unauthorized template
     * @param exchange The HttpServerExchange
     */
    private void endRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
        exchange.getResponseHeaders().put(Header.SERVER.toHttpString(), this.config.getApplicationHeadersServer());
        exchange.endExchange();
    }
    
    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(FormHandler.class).handleRequest(exchange);
    }
}