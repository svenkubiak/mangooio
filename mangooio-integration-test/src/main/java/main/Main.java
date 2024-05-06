package main;

import io.mangoo.constants.Mode;
import io.mangoo.core.Application;

/**
 *
 * @author svenkubiak
 *
 */
public final class Main {

    private Main(){
    }

    public static void main(String... args) {
        Application.start(Mode.DEV);
    }
}