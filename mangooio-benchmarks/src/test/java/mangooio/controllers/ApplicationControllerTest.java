package mangooio.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import interfaces.Constants;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;
import utils.RandomUtils;

public class ApplicationControllerTest {
	private static final String HELLO_WORLD_JSON = "{\"text\":\"Hello, World!\"}";
	
	@Test
	public void testJson() {
		WebResponse mangooResponse = WebRequest.get("/json").execute();
		
        assertThat(mangooResponse, not(nullValue()));
        assertThat(StatusCodes.OK, equalTo(mangooResponse.getStatusCode()));
        assertThat(HELLO_WORLD_JSON, equalTo(mangooResponse.getContent()));
	}
	
	@Test
	public void testDb() {
		WebResponse mangooResponse = WebRequest.get("/db").execute();
		
		assertThat(mangooResponse, not(nullValue()));
		assertThat(StatusCodes.OK, equalTo(mangooResponse.getStatusCode()));
		assertThat(mangooResponse.getContent(), containsString("worldId"));
		assertThat(mangooResponse.getContent(), containsString("randomNumber"));
	}
	
	@Test
	public void testQueries() {
		int queries = RandomUtils.getRandomId();
		WebResponse mangooResponse = WebRequest.get("/queries?queries=" + queries).execute();
		
		assertThat(mangooResponse, not(nullValue()));
		assertThat(StatusCodes.OK, equalTo(mangooResponse.getStatusCode()));
        assertThat(mangooResponse.getContent(), containsString("worldId"));
        assertThat(mangooResponse.getContent(), containsString("randomNumber"));
	}
	
	@Test
	public void testPlaintext() {
		WebResponse mangooResponse = WebRequest.get("/plaintext").execute();
		
		assertThat(mangooResponse, not(nullValue()));
		assertThat(StatusCodes.OK, equalTo(mangooResponse.getStatusCode()));
		assertThat(mangooResponse.getContent(), containsString(Constants.HELLO_WORLD));
	}
	
	@Test
	public void testFortunes() {
		WebResponse mangooResponse = WebRequest.get("/fortunes").execute();
		
		assertThat(mangooResponse, not(nullValue()));
		assertThat(StatusCodes.OK, equalTo(mangooResponse.getStatusCode()));
		assertThat(mangooResponse.getContent(), containsString("id"));
		assertThat(mangooResponse.getContent(), containsString(Constants.FORTUNE_MESSAGE));
	}
	
	@Test
	public void testUpdates() {
		int queries = RandomUtils.getRandomId();
		WebResponse mangooResponse = WebRequest.get("/updates?queries=" + queries).execute();
		
		assertThat(mangooResponse, not(nullValue()));
		assertThat(StatusCodes.OK, equalTo(mangooResponse.getStatusCode()));
		assertThat(mangooResponse.getContent(), containsString("worldId"));
	}
}