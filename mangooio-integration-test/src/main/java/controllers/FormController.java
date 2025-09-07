package controllers;

import com.google.re2j.Pattern;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Form;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormController {
    private static final Logger LOG = LogManager.getLogger(FormController.class);
    private static final Pattern PATTERN = Pattern.compile("[a-z]");
    private static final int MIN_SIZE = 11;
    private static final int MAX_SIZE = 12;

    public Response form() {
        return Response.ok().render();
    }
    
    public Response singlefile(Form form) {
        var content = Strings.EMPTY;
        Optional<InputStream> formFile = form.getFile("attachment");
        if (formFile.isPresent()) {
            InputStream file = formFile.get();
            try {
                content = IOUtils.toString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOG.error("Failed to read single file", e);
            }
        }

        return Response.ok().bodyText(content);
    }

    @SuppressWarnings("all")
    public Response multifile(Form form) {
        String content = Strings.EMPTY;
        List<InputStream> files = new ArrayList<>();
        files.add(form.getFile("attachment1").get());
        files.add(form.getFile("attachment2").get());
        for (InputStream file : files) {
            try {
                content = content + IOUtils.toString(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOG.error("Failed to one of multiple files", e);
            }
        }

        return Response.ok().bodyText(content + files.size());
    }


    public Response validateform(Form form) {
        form.expectValue("name");
        form.expectEmail("email");
        form.expectExactMatch("password", "passwordconfirm"); //NOSONAR
        form.expectMatch("email2", "email2confirm");
        form.expectIpv4("ipv4");
        form.expectIpv6("ipv6");
        form.expectRegex("regex", PATTERN);
        form.expectMaxLength("phone", MAX_SIZE);
        form.expectMinLength("fax", MIN_SIZE);

        if (form.isValid()) {
            return Response.ok().bodyText("Fancy that!");
        }

        return Response.ok().render();
    }
    
    public Response flashify() {
        return Response.ok().render();
    }
    
    public Response submit(Form form) {
        form.keep();
        
        return Response.redirect("/flashify");
    }
}