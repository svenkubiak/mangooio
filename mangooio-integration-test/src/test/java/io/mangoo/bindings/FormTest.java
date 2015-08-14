package io.mangoo.bindings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.mangoo.routing.bindings.Form;
import io.mangoo.test.MangooTestInstance;

public class FormTest {

    private Form getNewForm() {
        Form form = MangooTestInstance.IO.getInjector().getInstance(Form.class);
        form.setSubmitted(true);

        return form;
    }

    @Test
    public void exactMatchTest() {
        Form form = getNewForm();
        form.addValue("foo", "BlA");
        form.addValue("bar", "BlA");
        form.validation().exactMatch("foo", "bar");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "BlA");
        form.addValue("bar", "Bla");
        form.validation().exactMatch("foo", "bar");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void MatchTest() {
        Form form = getNewForm();
        form.addValue("foo", "BLA");
        form.addValue("bar", "bla");
        form.validation().match("foo", "bar");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "BLA");
        form.addValue("bar", "bla2");
        form.validation().match("foo", "bar");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void requiredTest() {
        Form form = getNewForm();
        form.addValue("foo", "bar");
        form.validation().required("foo");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "");
        form.validation().required("foo");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void minTest() {
        Form form = getNewForm();
        form.addValue("foo", "bar");
        form.validation().min("foo", 3);

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "ba");
        form.validation().min("foo", 4);

        assertTrue(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "5");
        form.validation().min("foo", 1);

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "3");
        form.validation().min("foo", 5);

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void maxTest() {
        Form form = getNewForm();
        form.addValue("foo", "bar");
        form.validation().max("foo", 3);

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "bars");
        form.validation().max("foo", 3);

        assertTrue(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "3");
        form.validation().max("foo", 5);

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "7");
        form.validation().max("foo", 6);

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void emailTest() {
        Form form = getNewForm();
        form.addValue("foo", "foo@bar.com");
        form.validation().email("foo");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "foobar");
        form.validation().email("foo");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void urlTest() {
        Form form = getNewForm();
        form.addValue("foo", "https://mangoo.io");
        form.validation().url("foo");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "htps://mangoo.io");
        form.validation().url("foo");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void ipv4Test() {
        Form form = getNewForm();
        form.addValue("foo", "192.168.2.1");
        form.validation().ipv4("foo");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "501.15.1.2.1");
        form.validation().ipv4("foo");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void ipv6Test() {
        Form form = getNewForm();
        form.addValue("foo", "001:db8:85a3:8d3:1319:8a2e:370:7348");
        form.validation().ipv6("foo");

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "001:db8:85a3:8d3:1319:8a2e:7348");
        form.validation().ipv6("foo");

        assertTrue(form.validation().hasErrors());
    }

    @Test
    public void rangeTest() {
        Form form = getNewForm();
        form.addValue("foo", "bar");
        form.validation().range("foo", 1, 3);

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "barddddd");
        form.validation().range("foo", 1, 4);

        assertTrue(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "10");
        form.validation().range("foo", 1, 11);

        assertFalse(form.validation().hasErrors());

        form = getNewForm();
        form.addValue("foo", "23");
        form.validation().range("foo", 10, 20);

        assertTrue(form.validation().hasErrors());
    }
}