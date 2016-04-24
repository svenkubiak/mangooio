package mangooio.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;

import de.svenkubiak.embeddedmongodb.EmbeddedMongo;
import interfaces.Constants;
import io.mangoo.utils.http.HTTPRequest;
import io.mangoo.utils.http.HTTPResponse;
import io.undertow.util.StatusCodes;
import utils.RandomUtils;

public class ApplicationControllerTest {
	private static final String HELLO_WORLD_JSON = "{\"text\":\"Hello, World!\"}";

	@Test
	public void testIndex() {
		HTTPResponse mangooResponse = HTTPRequest.get("/").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
	}
	
	@Test
	public void testJson() {
		HTTPResponse mangooResponse = HTTPRequest.get("/json").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertEquals(HELLO_WORLD_JSON, mangooResponse.getContent());
	}
	
	@Test
	public void testDb() {
		HTTPResponse mangooResponse = HTTPRequest.get("/db").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains("randomNumber"));
	}
	
	@Test
	public void testQueries() {
		int queries = RandomUtils.getRandomId();
		HTTPResponse mangooResponse = HTTPRequest.get("/db?queries=" + queries).execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains("randomNumber"));
	}
	
	@Test
	public void testPlaintext() {
		HTTPResponse mangooResponse = HTTPRequest.get("/plaintext").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertEquals(Constants.HELLO_WORLD, mangooResponse.getContent());
	}
	
	@Test
	public void testFortunes() {
		HTTPResponse mangooResponse = HTTPRequest.get("/fortunes").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains(Constants.FORTUNE_MESSAGE));
	}
	
	@Test
	public void testUpdates() {
		int queries = RandomUtils.getRandomId();
		HTTPResponse mangooResponse = HTTPRequest.get("/updates?queries=" + queries).execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
	}
	
	@AfterClass
	public static void stopMongoDB() {
		EmbeddedMongo.DB.stop();
	}
}