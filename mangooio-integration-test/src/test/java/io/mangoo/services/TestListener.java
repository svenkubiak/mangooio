package io.mangoo.services;

import com.google.common.eventbus.Subscribe;

/**
 * 
 * @author svenkubiak
 *
 */
public class TestListener {
    private int count;
    
    @Subscribe
    void task(String s) {
        this.count++;
    }
    
    public int getCount() {
        return count;
    }
}