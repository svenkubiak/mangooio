package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import com.google.common.io.Resources;
import com.google.common.net.MediaType;

import io.mangoo.TestExtension;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
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
		List<NameValuePair> parameter = new ArrayList<NameValuePair>();
		parameter.add(new BasicNameValuePair("username", "vip"));
		parameter.add(new BasicNameValuePair("password", "secret"));

		// when
		WebResponse response = WebRequest.post("/form")
				.withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withPostParameters(parameter)
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
            List<NameValuePair> parameter = new ArrayList<NameValuePair>();
            parameter.add(new BasicNameValuePair("username", username));
            parameter.add(new BasicNameValuePair("password", password));

            // when
            WebResponse response = WebRequest.post("/form")
                    .withContentType(MediaType.FORM_DATA.withoutParameters().toString()).withPostParameters(parameter)
                    .execute();
            
            // then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals(username + ";" + password);
        }, new org.llorllale.cactoos.matchers.RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
	
    @Test
    public void testMultiValue() {
        // given
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("foo[]", "1"));
        parameter.add(new BasicNameValuePair("foo[]", "2"));
        parameter.add(new BasicNameValuePair("foo[]", "3"));

        // when
        WebResponse response = WebRequest.post("/multivalued")
                .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                .withPostParameters(parameter)
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
            List<NameValuePair> parameter = new ArrayList<NameValuePair>();
            parameter.add(new BasicNameValuePair("foo[]", uuid1));
            parameter.add(new BasicNameValuePair("foo[]", uuid2));
            parameter.add(new BasicNameValuePair("foo[]", uuid3));

            // when
            WebResponse response = WebRequest.post("/multivalued")
                    .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                    .withPostParameters(parameter)
                    .execute();
            
            // then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals(uuid1 + "\n" + uuid2 + "\n" + uuid3 + "\n");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }

	@Test
	public void testSingleFileUpload() throws IOException {
		// given
		File file = new File(UUID.randomUUID().toString());
		InputStream attachment = Resources.getResource("attachment.txt").openStream();		
		FileUtils.copyInputStreamToFile(attachment, file);
		
		// when
		WebResponse response = WebRequest.post("/singlefile")
				.withFileBody("file", new FileBody(file))
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("This is an attachment"));
		file.delete();
	}
	
	@Test
	public void testMultiFileUpload() throws IOException {
		// given
		File file1 = new File(UUID.randomUUID().toString());
		File file2 = new File(UUID.randomUUID().toString());
		InputStream attachment1 = Resources.getResource("attachment.txt").openStream();
		InputStream attachment2 = Resources.getResource("attachment.txt").openStream();
		FileUtils.copyInputStreamToFile(attachment1, file1);
		FileUtils.copyInputStreamToFile(attachment2, file2);
		
		// when
		WebResponse response = WebRequest.post("/multifile")
				.withFileBody("file1", new FileBody(file1))
				.withFileBody("file2", new FileBody(file2))
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("This is an attachmentThis is an attachment2"));
		file1.delete();
		file2.delete();
	}

	@Test
	public void testFormEncoding() {
		// given
		List<NameValuePair> parameter = new ArrayList<NameValuePair>();
		parameter.add(new BasicNameValuePair("username", "süpöä"));
		parameter.add(new BasicNameValuePair("password", "#+ß§"));

		// when
		WebResponse response = WebRequest.post("/form")
				.withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withPostParameters(parameter)
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("süpöä;#+ß§"));
	}

	@Test
	public void testInvalidFormValues() {
		// given
		List<NameValuePair> parameter = new ArrayList<NameValuePair>();
		parameter.add(new BasicNameValuePair("phone", "1234567890123"));
		parameter.add(new BasicNameValuePair("regex", "ABC"));

		// when
		WebResponse response = WebRequest.post("/validateform")
				.withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withPostParameters(parameter)
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
		List<NameValuePair> parameter = new ArrayList<NameValuePair>();
		parameter.add(new BasicNameValuePair("name", "this is my name"));
		parameter.add(new BasicNameValuePair("email", "foo@bar.com"));
		parameter.add(new BasicNameValuePair("email2", "game@thrones.com"));
		parameter.add(new BasicNameValuePair("email2confirm", "game@thrones.com"));
		parameter.add(new BasicNameValuePair("password", "Secret"));
		parameter.add(new BasicNameValuePair("passwordconfirm", "Secret"));
		parameter.add(new BasicNameValuePair("ipv4", "11.12.23.42"));
		parameter.add(new BasicNameValuePair("ipv6", "2001:db8:85a3:8d3:1319:8a2e:370:7348"));
		parameter.add(new BasicNameValuePair("phone", "abcdef"));
		parameter.add(new BasicNameValuePair("fax", "abchdjskcjsa"));
		parameter.add(new BasicNameValuePair("regex", "a"));

		// when
		WebResponse response = WebRequest.post("/validateform")
		        .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
				.withPostParameters(parameter)
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
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("name", "this is my name"));
        parameter.add(new BasicNameValuePair("email", "foo@bar.com"));
        
        // when
        WebResponse response = WebRequest.post("/submit")
                .withLaxRedirectStrategy()
                .withContentType(MediaType.FORM_DATA.withoutParameters().toString())
                .withPostParameters(parameter).execute();

        // then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(data));
    }
}