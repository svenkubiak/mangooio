package controllers;

import com.google.inject.Inject;

import mangoo.io.i18n.Messages;
import mangoo.io.routing.Response;

public class I18nController {

	@Inject
	private Messages messages;
	
    public Response translation() {
        messages.get("my.translation");
        messages.get("my.othertranslation", "foo");

        return Response.withOk();
    }
}
