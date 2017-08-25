package controllers;

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
		this.dataService = dataService;
	}
	
	@FilterWith(AuthenticityFilter.class)
	public Response authenticate(Form form, Flash flash, Authentication authentication) {
		form.expectValue("username");
		form.expectValue("password");
		
		if (form.isValid()) {
			User user = this.dataService.getUser("foo");
			if (user != null && authentication.validLogin(form.get("username"), form.get("password"), user.getPassword())) {
				flash.setSuccess("authentication");
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