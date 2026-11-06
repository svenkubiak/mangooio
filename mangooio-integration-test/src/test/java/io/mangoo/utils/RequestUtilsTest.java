package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.constants.Header;
import io.mangoo.routing.bindings.Request;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import io.undertow.util.PathTemplateMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({TestExtension.class})
class RequestUtilsTest {

    @Test
    void testGetAuthorizationHeader() {
        // given
        Request request = Mockito.mock(Request.class);
        String authorization = UUID.randomUUID().toString();
        String header = "Bearer " + authorization + "       ";

        // when
        when(request.getHeader(Header.AUTHORIZATION)).thenReturn(header);

        // then
        assertThat(RequestUtils.getAuthorizationHeader(request), not(nullValue()));
        assertThat(RequestUtils.getAuthorizationHeader(request).isPresent(), equalTo(true));
        assertThat(RequestUtils.getAuthorizationHeader(request).get(), equalTo(authorization));
    }

    @Test
    void testGetRequestParametersWithQueryParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("param1", "value1");
        exchange.addQueryParam("param2", "value2");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.get("param1"), equalTo("value1"));
        assertThat(parameters.get("param2"), equalTo("value2"));
        assertThat(parameters.size(), equalTo(2));
    }

    @Test
    void testGetRequestParametersWithPathParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addPathParam("pathParam1", "pathValue1");
        exchange.addPathParam("pathParam2", "pathValue2");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.get("pathParam1"), equalTo("pathValue1"));
        assertThat(parameters.get("pathParam2"), equalTo("pathValue2"));
        assertThat(parameters.size(), equalTo(2));
    }

    @Test
    void testGetRequestParametersWithBothQueryAndPathParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("queryParam", "queryValue");
        exchange.addPathParam("pathParam", "pathValue");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.get("queryParam"), equalTo("queryValue"));
        assertThat(parameters.get("pathParam"), equalTo("pathValue"));
        assertThat(parameters.size(), equalTo(2));
    }

    @Test
    void testPathParameterWinsOverQueryParameterOfSameName() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("id", "spoofed");
        exchange.putAttachment(PathTemplateMatch.ATTACHMENT_KEY,
                new PathTemplateMatch("/foo/{id}", Map.of("id", "routevalue")));

        //then
        assertThat(RequestUtils.getRequestParameters(exchange).get("id"), equalTo("routevalue"));
        assertThat(RequestUtils.getPathParameters(exchange).get("id"), equalTo("routevalue"));
        assertThat(RequestUtils.getQueryParameters(exchange).get("id"), equalTo("spoofed"));
    }

    @Test
    void testPathTemplateMatchWinsOverAddPathParam() {
        //given the router resolved value is authoritative
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addPathParam("id", "fromHandler");
        exchange.putAttachment(PathTemplateMatch.ATTACHMENT_KEY,
                new PathTemplateMatch("/foo/{id}", Map.of("id", "fromRouter")));

        //then
        assertThat(RequestUtils.getPathParameters(exchange).get("id"), equalTo("fromRouter"));
    }

    @Test
    void testGetQueryParametersHoldsNoRouteValues() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("limit", "25");
        exchange.putAttachment(PathTemplateMatch.ATTACHMENT_KEY,
                new PathTemplateMatch("/foo/{id}", Map.of("id", "routevalue")));

        //when
        Map<String, String> queryParameters = RequestUtils.getQueryParameters(exchange);

        //then
        assertThat(queryParameters.get("limit"), equalTo("25"));
        assertThat(queryParameters.containsKey("id"), equalTo(false));
        assertThat(queryParameters.size(), equalTo(1));
    }

    @Test
    void testHasAmbiguousParameters() {
        //given
        HttpServerExchange colliding = new HttpServerExchange(null);
        colliding.addQueryParam("id", "spoofed");
        colliding.putAttachment(PathTemplateMatch.ATTACHMENT_KEY,
                new PathTemplateMatch("/foo/{id}", Map.of("id", "routevalue")));

        HttpServerExchange distinct = new HttpServerExchange(null);
        distinct.addQueryParam("limit", "25");
        distinct.putAttachment(PathTemplateMatch.ATTACHMENT_KEY,
                new PathTemplateMatch("/foo/{id}", Map.of("id", "routevalue")));

        HttpServerExchange withoutTemplate = new HttpServerExchange(null);
        withoutTemplate.addQueryParam("id", "anything");

        //then
        assertThat(RequestUtils.hasAmbiguousParameters(colliding), equalTo(true));
        assertThat(RequestUtils.hasAmbiguousParameters(distinct), equalTo(false));
        assertThat(RequestUtils.hasAmbiguousParameters(withoutTemplate), equalTo(false));
    }

    @Test
    void testGetRequestParametersWithEmptyParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    void testGetRequestParametersWithNullExchange() {
        //given

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.getRequestParameters(null));
    }

    @Test
    void testIsPostPutPatchWithPostRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.POST);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(true));
    }

    @Test
    void testIsPostPutPatchWithPutRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.PUT);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(true));
    }

    @Test
    void testIsPostPutPatchWithPatchRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.PATCH);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(true));
    }

    @Test
    void testIsPostPutPatchWithGetRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.GET);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(false));
    }

    @Test
    void testIsPostPutPatchWithDeleteRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.DELETE);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(false));
    }

    @Test
    void testIsPostPutPatchWithNullExchange() {
        //given

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.isPostPutPatch(null));
    }

    @Test
    void testIsJsonRequestWithJsonContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "application/json");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(true));
    }

    @Test
    void testIsJsonRequestWithJsonContentTypeAndCharset() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "application/json; charset=utf-8");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(true));
    }

    @Test
    void testIsJsonRequestWithXmlContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "application/xml");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(false));
    }

    @Test
    void testIsJsonRequestWithTextContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "text/plain");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(false));
    }

    @Test
    void testIsJsonRequestWithNoContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(false));
    }

    @Test
    void testIsJsonRequestWithNullExchange() {
        //given

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.isJsonRequest(null));
    }

    @Test
    void testGetAuthorizationHeaderWithNoAuthorizationHeader() {
        //given
        Request request = new Request();

        //when
        Optional<String> authorization = RequestUtils.getAuthorizationHeader(request);

        //then
        assertThat(authorization.isPresent(), is(false));
    }

    @Test
    void testGetAuthorizationHeaderWithNullRequest() {
        //given

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.getAuthorizationHeader(null));
    }

    @Test
    void testGetRequestParametersPathParameterWinsOverQueryParameter() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("id", "queryValue");
        exchange.addPathParam("id", "pathValue");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters.get("id"), equalTo("pathValue"));
        assertThat(parameters.size(), equalTo(1));
    }

    @Test
    void testGetRequestParametersDoesNotMutateExchange() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("queryParam", "queryValue");
        exchange.addPathParam("pathParam", "pathValue");

        //when
        RequestUtils.getRequestParameters(exchange);

        //then the live query parameter map must not be polluted with path parameters
        assertThat(exchange.getQueryParameters().containsKey("pathParam"), is(false));
        assertThat(exchange.getQueryParameters().size(), equalTo(1));
    }

    @Test
    void testHasMultipleParameterValuesWithSingleValues() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setQueryString("first=foo&second=bar");

        //when
        boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

        //then
        assertThat(hasMultiple, is(false));
    }

    @Test
    void testHasMultipleParameterValuesWithEmptyParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);

        //when
        boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

        //then
        assertThat(hasMultiple, is(false));
    }

    @Test
    void testHasMultipleParameterValuesWithValuelessParameter() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setQueryString("first&second");

        //when
        boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

        //then
        assertThat(hasMultiple, is(false));
    }

    @Test
    void testHasMultipleParameterValuesWithDuplicateQueryParameter() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setQueryString("id=1&id=2");

        //when
        boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

        //then
        assertThat(hasMultiple, is(true));
    }

    @Test
    void testHasMultipleParameterValuesWithPercentEncodedDuplicate() {
        for (String queryString : new String[] {"id=1&%69d=2", "id=1&i%64=2", "id=1&%69%64=2", "lang=1&%6cang=2", "lang=1&%6Cang=2", "a+b=1&a%20b=2"}) {
            //given
            HttpServerExchange exchange = new HttpServerExchange(null);
            exchange.setQueryString(queryString);

            //when
            boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

            //then
            assertThat(queryString, hasMultiple, is(true));
        }
    }

    @Test
    void testHasMultipleParameterValuesWithMalformedEscape() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setQueryString("%zz=1");

        //when
        boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

        //then
        assertThat(hasMultiple, is(true));
    }

    @Test
    void testHasMultipleParameterValuesIgnoresPathTemplateValues() {
        //given a route like /foo/{id} appends the path template value to the parsed query
        //parameter deque, but the raw query string only contains the actual query parameter
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setQueryString("id=1");
        exchange.addQueryParam("id", "1");
        exchange.addQueryParam("id", "templateValue");

        //when
        boolean hasMultiple = RequestUtils.hasMultipleParameterValues(exchange);

        //then
        assertThat(hasMultiple, is(false));
    }

    @Test
    void testHasMultipleParameterValuesWithNullExchange() {
        //given

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.hasMultipleParameterValues(null));
    }
}
