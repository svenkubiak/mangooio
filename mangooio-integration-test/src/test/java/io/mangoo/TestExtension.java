package io.mangoo;

import io.mangoo.enums.Key;
import io.mangoo.test.TestRunner;

public class TestExtension extends TestRunner {
    public static final int THREADS = 100;

    @Override
    public void init() {
        System.setProperty(Key.APPLICATION_PRIVATEKEY.toString(), "./key/privatekey.txt");
    }
}