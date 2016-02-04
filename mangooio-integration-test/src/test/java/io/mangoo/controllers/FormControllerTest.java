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

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.google.common.io.Resources;

import io.mangoo.enums.ContentType;
import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class FormControllerTest {

	@Test
	public void testFormPost() {
		// given
		List<NameValuePair> parameter = new ArrayList<NameValuePair>();
		parameter.add(new BasicNameValuePair("username", "vip"));
		parameter.add(new BasicNameValuePair("password", "secret"));

		// when
		Response response = Request.post("/form")
				.withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
				.withPostParameters(parameter)
				.execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("vip;secret"));
	}

	@Test
	public void testSingleFileUpload() throws IOException {
		// given
		File file = new File(UUID.randomUUID().toString());
		InputStream attachment = Resources.getResource("attachment.txt").openStream();		
		FileUtils.copyInputStreamToFile(attachment, file);
		
		// when
		Response response = Request.post("/singlefile")
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
		Response response = Request.post("/multifile")
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
		Response response = Request.post("/form")
				.withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
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
		Response response = Request.post("/validateform")
				.withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
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
		Response response = Request.post("/validateform").withContentType(ContentType.APPLICATION_X_WWW_FORM_URLENCODED)
				.withPostParameters(parameter).execute();

		// then
		assertThat(response, not(nullValue()));
		assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
		assertThat(response.getContent(), equalTo("Fancy that!"));
	}
}