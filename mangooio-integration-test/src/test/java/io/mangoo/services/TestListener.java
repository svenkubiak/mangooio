package io.mangoo.services;

import com.google.common.eventbus.Subscribe;

/**
 * 
 * @author sven.kubiak
 *
 */
public class TestListener {
    private int count;
    
    @Subscribe
    public void task(String s) {
        this.count++;
    }
    
    public int getCount() {
        return count;
    }
}