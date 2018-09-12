package io.mangoo;

import io.mangoo.enums.Key;
import io.mangoo.test.TestRunner;

public class TestExtension extends TestRunner {
    public static final int THREADS = 100;

    @Override
    public void init() {
        System.setProperty(Key.APPLICATION_PRIVATEKEY.toString(), "./key/privatekey.txt");
        System.setProperty(Key.APPLICATION_NAME.toString(), "namefromarg");
        System.setProperty("application.test", "valuefromarg");
        System.setProperty("application.encrypted", "cryptex{IrlH4cKz1glSAob7nMP7SXYyBNnrhh8eWIHnhh5qPXfp7W/k7+gN2IrYMg2kCqXyJlqsziKD6z3ABr3OtOCz8/RSdspw1iUG68WtInRqzgGUFbl4t6/DqbQ+YedNIAKPffFh5MtM+HKnv5z8nBJbJJLl/EZHz48kzX1tVye5UdznpRmbO7uQGHEtzkEsm76BUUrbnbt1BlqDe6JNEvnDkr+9dMAsi/eg+17yY4bzMluMVOo99UG+ltQigryKm+By7LS17h4ggrdkfwztcyspi83p1AFIyOjGnMBjflW0P5oX3N7AHNcvCCye/kkY3CVgCmjWenr+DZlWWlOwA6XbZQ==}");
        System.setProperty("application.profil", "cryptex{IrlH4cKz1glSAob7nMP7SXYyBNnrhh8eWIHnhh5qPXfp7W/k7+gN2IrYMg2kCqXyJlqsziKD6z3ABr3OtOCz8/RSdspw1iUG68WtInRqzgGUFbl4t6/DqbQ+YedNIAKPffFh5MtM+HKnv5z8nBJbJJLl/EZHz48kzX1tVye5UdznpRmbO7uQGHEtzkEsm76BUUrbnbt1BlqDe6JNEvnDkr+9dMAsi/eg+17yY4bzMluMVOo99UG+ltQigryKm+By7LS17h4ggrdkfwztcyspi83p1AFIyOjGnMBjflW0P5oX3N7AHNcvCCye/kkY3CVgCmjWenr+DZlWWlOwA6XbZQ==}");
    }
}