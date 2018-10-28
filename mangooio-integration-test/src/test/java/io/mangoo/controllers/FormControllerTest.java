package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class FormControllerTest {

    @Test
	public void testFormPost() {
		// given
		Multimap<String, String> parameter = ArrayListMultimap.create();
		parameter.put("username", "vip");
		parameter.put("password", "secret");

		// when
		TestResponse response = TestRequest.post("/form")
				.withForm(parameter)
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("vip;secret"));
	}
	
    @Test
    public void testFormPostConcurrent() {
        MatcherAssert.assertThat(t -> {
            // given
            String username = UUID.randomUUID().toString();
            String password = UUID.randomUUID().toString();
            Multimap<String, String> parameter = ArrayListMultimap.create();
            parameter.put("username", username);
            parameter.put("password", password);

            // when
            TestResponse response = TestRequest.post("/form")
                    .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                    .withForm(parameter)
                    .execute();
            
            // then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals(username + ";" + password);
        }, new org.llorllale.cactoos.matchers.RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
	
    @Test
    public void testMultiValue() {
        // given
        Multimap<String, String> parameter = ArrayListMultimap.create();
        parameter.put("foo[]", "1");
        parameter.put("foo[]", "2");
        parameter.put("foo[]", "3");
        
        // when
        TestResponse response = TestRequest.post("/multivalued")
                .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                .withForm(parameter)
                .execute();

        // then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1\n2\n3\n"));
    }
    
    @Test
    public void testMultiValueConcurrent() {
        MatcherAssert.assertThat(t -> {
            // given
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();
            Multimap<String, String> parameter = ArrayListMultimap.create();
            parameter.put("foo[]", uuid1);
            parameter.put("foo[]", uuid2);
            parameter.put("foo[]", uuid3);
            
            // when
            TestResponse response = TestRequest.post("/multivalued")
                    .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                    .withForm(parameter)
                    .execute();
            
            // then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals(uuid1 + "\n" + uuid2 + "\n" + uuid3 + "\n");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }

	@Test
	public void testSingleFileUpload() throws IOException {
	    // given
	    String host = Application.getInstance(Config.class).getConnectorHttpHost();
	    int port = Application.getInstance(Config.class).getConnectorHttpPort();
	    MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
	    multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

	    // when
        File file = new File(UUID.randomUUID().toString());
        InputStream attachment = Resources.getResource("attachment.txt").openStream();
        FileUtils.copyInputStreamToFile(attachment, file);
        multipartEntityBuilder.addPart("attachment", new FileBody(file));
        HttpPost httpPost = new HttpPost("http://" + host + ":" + port + "/singlefile");
        httpPost.setEntity(multipartEntityBuilder.build());

        String response = null;
        HttpResponse httpResponse = httpClientBuilder.build().execute(httpPost);

        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
            response = EntityUtils.toString(httpEntity);
        }

		// then
		assertThat(httpResponse, not(nullValue()));
		assertThat(response, not(nullValue()));
		assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response, equalTo("This is an attachment"));
		file.delete();
	}
	
	@Test
	public void testMultiFileUpload() throws IOException {
	    // given
	    String host = Application.getInstance(Config.class).getConnectorHttpHost();
	    int port = Application.getInstance(Config.class).getConnectorHttpPort();
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // when
        File file1 = new File(UUID.randomUUID().toString());
        File file2 = new File(UUID.randomUUID().toString());
        InputStream attachment1 = Resources.getResource("attachment.txt").openStream();
        InputStream attachment2 = Resources.getResource("attachment.txt").openStream();
        FileUtils.copyInputStreamToFile(attachment1, file1);
        FileUtils.copyInputStreamToFile(attachment2, file2);
        multipartEntityBuilder.addPart("attachment1", new FileBody(file1));
        multipartEntityBuilder.addPart("attachment2", new FileBody(file2));
        HttpPost httpPost = new HttpPost("http://" + host + ":" + port + "/multifile");
        httpPost.setEntity(multipartEntityBuilder.build());

        String response = null;
        HttpResponse httpResponse = httpClientBuilder.build().execute(httpPost);

        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
            response = EntityUtils.toString(httpEntity);
        }

        // then
        assertThat(httpResponse, not(nullValue()));
        assertThat(response, not(nullValue()));
        assertThat(httpResponse.getStatusLine().getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response, equalTo("This is an attachmentThis is an attachment2"));
        file1.delete();
        file2.delete();
	}

	@Test
	public void testFormEncoding() {
		// given
		Multimap<String, String> parameter = ArrayListMultimap.create();
        parameter.put("username", "süpöä");
        parameter.put("password", "#+ß§");
		
		// when
		TestResponse response = TestRequest.post("/form")
		        .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withForm(parameter)
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("süpöä;#+ß§"));
	}

	@Test
	public void testInvalidFormValues() {
		// given
		Multimap<String, String> parameter = ArrayListMultimap.create();
        parameter.put("phone", "1234567890123");
        parameter.put("regex", "ABC");
		
		// when
		TestResponse response = TestRequest.post("/validateform")
				.withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withForm(parameter)
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

		String[] lines = response.getContent().split(System.getProperty("line.separator"));
		assertThat(lines[0], equalTo("name is required"));
		assertThat(lines[1], equalTo("email must be a valid eMail address"));
		assertThat(lines[2], equalTo("email2 must match email2confirm"));
		assertThat(lines[3], equalTo("password must exactly match passwordconfirm"));
		assertThat(lines[4], equalTo("ipv4 must be a valid IPv4 address"));
		assertThat(lines[5], equalTo("ipv6 must be a valid IPv6 address"));
		assertThat(lines[6], equalTo("phone must have a size of max 12"));
		assertThat(lines[7], equalTo("fax must have a least a size of 11"));
		assertThat(lines[8], equalTo("regex is invalid"));
	}

	@Test
	public void testValidFormValues() {
		// given
        Multimap<String, String> parameter = ArrayListMultimap.create();
        parameter.put("name", "this is my name");
        parameter.put("email", "foo@bar.com");
        parameter.put("email2", "game@thrones.com");
        parameter.put("email2confirm", "game@thrones.com");
        parameter.put("password", "Secret");
        parameter.put("passwordconfirm", "Secret");
        parameter.put("ipv4", "11.12.23.42");
        parameter.put("ipv6", "2001:db8:85a3:8d3:1319:8a2e:370:7348");
        parameter.put("phone", "abcdef");
        parameter.put("fax", "abchdjskcjsa");
        parameter.put("regex", "a");

		// when
		TestResponse response = TestRequest.post("/validateform")
		        .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withForm(parameter)
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("Fancy that!"));
	}

    @Test
    public void testFlashify() {
        // given
        String data = "this is my namefoo@bar.com";
        Multimap<String, String> parameter = ArrayListMultimap.create();
        parameter.put("name", "this is my name");
        parameter.put("email", "foo@bar.com");
        
        // when
        TestResponse response = TestRequest.post("/submit")
                .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                .withForm(parameter)
                .execute();

        // then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(data));
    }
}