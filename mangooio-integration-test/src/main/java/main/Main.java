package main;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;

/**
 *
 * @author svenkubiak
 *
 */
public final class Main {

    private Main(){
    }

    static void main(String... args) throws InterruptedException {
        Application.start(Mode.DEV);
    }
}