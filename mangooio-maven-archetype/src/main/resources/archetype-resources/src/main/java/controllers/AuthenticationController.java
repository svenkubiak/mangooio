package controllers;

import java.util.Objects;

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
    private DataService dataService;
    
    @Inject
    public AuthenticationController(DataService dataService) {
        this.dataService = Objects.requireNonNull(dataService, "dataService can not be null");
    }
    
    @FilterWith(AuthenticityFilter.class)
    public Response authenticate(Form form, Flash flash, Authentication authentication) {
        form.validation().required("username");
        form.validation().required("password");
        
        if (!form.validation().hasErrors()) {
            User user = this.dataService.getUser(form.get("username"));
            if (user != null) {
                //You could also wrap the login around an if and redirect back to the login page if loing fails
                authentication.login(user.getUsername(), form.get("password"), user.getPassword());
            }
        }
        
        return Response.withRedirect("/");
    }
    
    @FilterWith(AuthenticityFilter.class)
    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.withRedirect("/");
    }
}