package mangooio.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;

import de.svenkubiak.embeddedmongodb.EmbeddedMongo;
import interfaces.Constants;
import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
import io.undertow.util.StatusCodes;
import utils.RandomUtils;

public class ApplicationControllerTest {
	private static final String HELLO_WORLD_JSON = "{\"message\":\"Hello, World!\"}";

	@Test
	public void testIndex() {
		Response mangooResponse = Request.get("/").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
	}
	
	@Test
	public void testJson() {
		Response mangooResponse = Request.get("/json").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertEquals(HELLO_WORLD_JSON, mangooResponse.getContent());
	}
	
	@Test
	public void testDb() {
		Response mangooResponse = Request.get("/db").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains("randomNumber"));
	}
	
	@Test
	public void testQueries() {
		int queries = RandomUtils.getRandomId();
		Response mangooResponse = Request.get("/db?queries=" + queries).execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains("randomNumber"));
	}
	
	@Test
	public void testPlaintext() {
		Response mangooResponse = Request.get("/plaintext").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertEquals(Constants.HELLO_WORLD, mangooResponse.getContent());
	}
	
	@Test
	public void testFortunes() {
		Response mangooResponse = Request.get("/fortunes").execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
		assertTrue(mangooResponse.getContent().contains(Constants.FORTUNE_MESSAGE));
	}
	
	@Test
	public void testUpdates() {
		int queries = RandomUtils.getRandomId();
		Response mangooResponse = Request.get("/updates?queries=" + queries).execute();
		
		assertNotNull(mangooResponse);
		assertEquals(StatusCodes.OK, mangooResponse.getStatusCode());
		assertTrue(mangooResponse.getContent().contains("id"));
	}
	
	@AfterClass
	public static void stopMongoDB() {
		EmbeddedMongo.DB.stop();
	}
}