package main;

import io.mangoo.core.Application;
import io.mangoo.enums.Mode;

public final class Main {

    private Main(){
    }

    static void main() {
        Application.start(Mode.DEV);
    }
}