package controllers;

import io.mangoo.routing.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ParameterController {
    private static final String FOO = "foo";
    private static final String PARAM_TEMPLATE = "/ParameterController/param.ftl";
    private static final String MULTIPARAM_TEMPLATE = "/ParameterController/multiparam.ftl";

    public Response stringParam(String foo) {
        if (foo == null) {
            foo = "isNull"; //NOSONAR
        }

        return Response.ok().template(PARAM_TEMPLATE).render(FOO, foo);
    }
    
    public Response doubleParam(Double foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response doublePrimitiveParam(double foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response booleanParam(Boolean foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response booleanPrimitiveParam(boolean foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response intParam(int foo) {
        return Response.ok().template(PARAM_TEMPLATE).render(FOO, foo);
    }

    public Response integerParam(Integer foo) {
        return Response.ok().template(PARAM_TEMPLATE).render(FOO, foo);
    }

    public Response floatParam(Float foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response floatPrimitiveParam(float foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response longParam(Long foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response longPrimitiveParam(long foo) {
        return Response.ok().bodyText(String.valueOf(foo));
    }

    public Response multipleParam(String foo, int bar) {
        return Response.ok().template(MULTIPARAM_TEMPLATE).render(FOO, foo).render("bar", bar);
    }

    public Response pathParam(String foo) {
        return Response.ok().template(PARAM_TEMPLATE).render(FOO, foo);
    }
    
    public Response optionalParam(Optional<String> foo) {
        return Response.ok().template(PARAM_TEMPLATE).render(FOO, foo);
    }

    public Response localdate(LocalDate localDate) {
        return Response
                .ok()
                .bodyText(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public Response localdatetime(LocalDateTime localDateTime) {
        return Response
                .ok()
                .bodyText(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}