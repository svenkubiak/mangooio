package mangoo.bindings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mangoo.io.routing.bindings.Form;

public class FormTest {

	@Test
	public void exactMatchTest() {
		Form form = new Form();
		form.add("foo", "BlA");
		form.add("bar", "BlA");
		form.setSubmitted(true);
		form.exactMatch("foo", "bar");
		
		assertFalse(form.hasErrors());
	}
}