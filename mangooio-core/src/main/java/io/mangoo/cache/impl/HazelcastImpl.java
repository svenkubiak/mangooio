package io.mangoo.cache.impl;

import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.interfaces.MangooCache;

/**
 *
 * @author svenkubiak
 *
 */
public class HazelcastImpl implements MangooCache {

    @Inject
    public HazelcastImpl(Config cpnfig) {
    }

    @Override
    public void add(String key, Object value) {
        // TODO Auto-generated method stub
    }

    @Override
    public void add(String key, Object value, int expiration) {
        // TODO Auto-generated method stub
    }

    @Override
    public Object get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }
}