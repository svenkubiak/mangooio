package mangooio.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import interfaces.Constants;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;
import utils.RandomUtils;

public class ApplicationControllerTest {
	private static final String HELLO_WORLD_JSON = "{\"text\":\"Hello, World!\"}";

	@Test
	public void testIndex() {
		WebResponse mangooResponse = WebRequest.get("/").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
	}
	
	@Test
	public void testJson() {
		WebResponse mangooResponse = WebRequest.get("/json").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertEquals(HELLO_WORLD_JSON, mangooResponse.getContent());
	}
	
	@Test
	public void testDb() {
		WebResponse mangooResponse = WebRequest.get("/db").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains("randomNumber"));
	}
	
	@Test
	public void testQueries() {
		int queries = RandomUtils.getRandomId();
		WebResponse mangooResponse = WebRequest.get("/db?queries=" + queries).execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains("randomNumber"));
	}
	
	@Test
	public void testPlaintext() {
		WebResponse mangooResponse = WebRequest.get("/plaintext").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertEquals(Constants.HELLO_WORLD, mangooResponse.getContent());
	}
	
	@Test
	public void testFortunes() {
		WebResponse mangooResponse = WebRequest.get("/fortunes").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains(Constants.FORTUNE_MESSAGE));
	}
	
	@Test
	public void testUpdates() {
		int queries = RandomUtils.getRandomId();
		WebResponse mangooResponse = WebRequest.get("/updates?queries=" + queries).execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
	}
}