package io.mangoo.test;

import org.junit.Before;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooUnit {
    
    @Before
    public final void mangooStartup() {
        beforeMangooStartup();
        MangooInstance.TEST.start();
    }

    public void beforeMangooStartup() {
        //Intentionally left blank for overwriting
    }
}