package controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import io.mangoo.routing.Response;

public class ParameterController {
    private static final String FOO = "foo";
    private static final String PARAM_TEMPLATE = "/ParameterController/param.ftl";
    private static final String MULTIPARAM_TEMPLATE = "/ParameterController/multiparam.ftl";

    public Response stringParam(String foo) {
        if (foo == null) {
            foo = "isNull"; //NOSONAR
        }

        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }
    
    public Response doubleParam(Double foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }

    public Response doublePrimitiveParam(double foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }

    public Response intParam(int foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }

    public Response integerParam(Integer foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }

    public Response floatParam(Float foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }

    public Response floatPrimitiveParam(float foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }

    public Response longParam(Long foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }

    public Response longPrimitiveParam(long foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }

    public Response multipleParam(String foo, int bar) {
        return Response.withOk().andTemplate(MULTIPARAM_TEMPLATE).andContent(FOO, foo).andContent("bar", bar);
    }

    public Response pathParam(String foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }
    
    public Response optionalParam(Optional<String> foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }

    public Response localdate(LocalDate localDate) {
        return Response
                .withOk()
                .andTextBody(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public Response localdatetime(LocalDateTime localDateTime) {
        return Response
                .withOk()
                .andTextBody(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}