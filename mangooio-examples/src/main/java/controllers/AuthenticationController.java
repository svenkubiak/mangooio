package controllers;

import java.util.UUID;

import com.google.inject.Inject;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticityFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import models.User;
import services.DataService;

public class AuthenticationController {
    private static final String PASSWORD = "password"; //NOSONAR
    private static final String USERNAME = "username";
    private DataService dataService;

    @Inject
    public AuthenticationController(DataService dataService) {
        this.dataService = dataService;
    }
    
    public Response login() {
        return Response.withOk();
    }
    
    @FilterWith(AuthenticityFilter.class)
    public Response authenticate(Form form, Flash flash, Authentication authentication) {
        form.expectValue(USERNAME);
        form.expectValue(PASSWORD);
        
        if (form.isValid()) {
            User user = this.dataService.getUser();
            if (user != null && authentication.validLogin(form.get(USERNAME), form.get(PASSWORD), user.getPassword())) {
                authentication
                        .login(UUID.randomUUID().toString())
                        .rememberMe(form.getBoolean("remember").orElse(Boolean.FALSE));

                return Response.withRedirect("/");
            } 
        }
        
        return Response.withRedirect("/login");
    }
    
    @FilterWith(AuthenticityFilter.class)
    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.withRedirect("/");
    }
}