package controllers;

import mangoo.io.routing.Response;
import mangoo.io.routing.bindings.Form;

public class FormController {
    public Response form(Form form) {
        return Response.withOk();
    }
    
    public Response validateform(Form form) {
    	form.required("name");
    	form.email("email");
    	form.exactMatch("password", "passwordconfirm");
    	form.match("email2", "email2confirm");
    	form.ipv4("ipv4");
    	form.ipv6("ipv6");
    	form.max(12, "phone");
    	form.min(11, "fax");
    	
    	if (!form.hasErrors()) {
    		return Response.withOk().andTextBody("Fancy that!");
    	}
    	
    	return Response.withOk(); 
    }
}