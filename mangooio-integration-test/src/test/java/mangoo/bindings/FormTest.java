package mangoo.bindings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mangoo.io.routing.bindings.Form;
import mangoo.io.test.MangooTest;

public class FormTest {

    private Form getNewForm() {
        Form form = MangooTest.INSTANCE.getInjector().getInstance(Form.class);
        form.setSubmitted(true);

        return form;
    }

    @Test
    public void exactMatchTest() {
        Form form = getNewForm();
        form.add("foo", "BlA");
        form.add("bar", "BlA");
        form.exactMatch("foo", "bar");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "BlA");
        form.add("bar", "Bla");
        form.exactMatch("foo", "bar");

        assertTrue(form.hasErrors());
    }

    @Test
    public void MatchTest() {
        Form form = getNewForm();
        form.add("foo", "BLA");
        form.add("bar", "bla");
        form.match("foo", "bar");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "BLA");
        form.add("bar", "bla2");
        form.match("foo", "bar");

        assertTrue(form.hasErrors());
    }

    @Test
    public void requiredTest() {
        Form form = getNewForm();
        form.add("foo", "bar");
        form.required("foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "");
        form.required("foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void minTest() {
        Form form = getNewForm();
        form.add("foo", "bar");
        form.min(3, "foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "ba");
        form.min(4, "foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void maxTest() {
        Form form = getNewForm();
        form.add("foo", "bar");
        form.max(3, "foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "bars");
        form.max(3, "foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void emailTest() {
        Form form = getNewForm();
        form.add("foo", "foo@bar.com");
        form.email("foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "foo@bar");
        form.email("foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void urlTest() {
        Form form = getNewForm();
        form.add("foo", "https://mangoo.io");
        form.url("foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "htps://mangoo.io");
        form.url("foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void ipv4Test() {
        Form form = getNewForm();
        form.add("foo", "192.168.2.1");
        form.ipv4("foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "501.15.1.2.1");
        form.ipv4("foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void ipv6Test() {
        Form form = getNewForm();
        form.add("foo", "001:db8:85a3:8d3:1319:8a2e:370:7348");
        form.ipv6("foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "001:db8:85a3:8d3:1319:8a2e:7348");
        form.ipv6("foo");

        assertTrue(form.hasErrors());
    }

    @Test
    public void rangeTest() {
        Form form = getNewForm();
        form.add("foo", "bar");
        form.range(1, 3, "foo");

        assertFalse(form.hasErrors());

        form = getNewForm();
        form.add("foo", "barddddd");
        form.range(1, 4, "foo");

        assertTrue(form.hasErrors());
    }
}