package controllers;

import mangoo.io.routing.Response;

public class ParameterController {
    private static final String FOO = "foo";
    private static final String PARAM_TEMPLATE = "param.ftl";
    private static final String MULTIPARAM_TEMPLATE = "multiparam.ftl";
    
    public Response stringParam (String foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }
    
    public Response doubleParam (double foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }
    
    public Response intParam (int foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }
    
    public Response floatParam (float foo) {
        return Response.withOk().andTextBody(String.valueOf(foo));
    }
    
    public Response multipleParam (String foo, int bar) {
        return Response.withOk().andTemplate(MULTIPARAM_TEMPLATE).andContent(FOO, foo).andContent("bar", bar);
    }
    
    public Response pathParam (String foo) {
        return Response.withOk().andTemplate(PARAM_TEMPLATE).andContent(FOO, foo);
    }
}