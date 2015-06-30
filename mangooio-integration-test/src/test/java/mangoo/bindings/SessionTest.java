package mangoo.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mangoo.io.routing.bindings.Session;

public class SessionTest {

    @Test
    public void testContent() {
        Session session = new Session();
        assertFalse(session.hasContent());

        session.add("foo", "bar");
        assertTrue(session.hasContent());
    }

    @Test
    public void testRemove() {
        Session session = new Session();
        session.add("foo", "bar");

        assertEquals("bar", session.get("foo"));

        session.remove("foo");
        assertNull(session.get("foo"));
    }

    @Test
    public void testClear() {
        Session session = new Session();
        session.add("foo", "bar");
        session.add("bla", "foobar");

        session.clear();
        assertNull(session.get("foo"));
        assertNull(session.get("bla"));
    }
}