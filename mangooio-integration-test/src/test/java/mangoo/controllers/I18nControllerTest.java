package mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import mangoo.io.testing.MangooRequest;
import mangoo.io.testing.MangooResponse;
import mangoo.io.testing.MangooUnit;

import org.junit.Test;
/**
 * 
 * @author svenkubiak
 *
 */
public class I18nControllerTest extends MangooUnit {

    @Test
    public void templateTest() {
        MangooResponse response = MangooRequest.get("/translation").header("Accept-Language", "de-DE").execute();
        
        assertNotNull(response.getContent());
        assertEquals("willkommen", response.getContent());
    }
    
    @Test
    public void title_of_bing_should_contain_search_query_name() {
        goTo("http://www.bing.com");
        fill("#sb_form_q").with("FluentLenium");
        submit("#sb_form_go");
        assertTrue(title().contains("FluentLenium"));
    }
}