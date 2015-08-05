package io.mangoo.cache.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Key;
import io.mangoo.interfaces.MangooCache;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class MemcachedImpl implements MangooCache {
    private static final Logger LOG = LoggerFactory.getLogger(MemcachedImpl.class);
    private MemcachedClient client;

    @Inject
    public MemcachedImpl(Config config) {
        try {
            List<InetSocketAddress> hosts = AddrUtil.getAddresses(config.getString(Key.CACHE_MEMCACHED_HOSTS));

            String username = config.getString(Key.CACHE_MEMCACHED_USER);
            String password = config.getString(Key.CACHE_MEMCACHED_PASSWORD);
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                AuthDescriptor authDescriptor = new AuthDescriptor(new String[]{"PLAIN"}, new PlainCallbackHandler(username, password));
                ConnectionFactory connectionFactory = new ConnectionFactoryBuilder()
                        .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
                        .setAuthDescriptor(authDescriptor)
                        .build();

                this.client = new MemcachedClient(connectionFactory, hosts);

            } else {
                this.client = new MemcachedClient(hosts);
            }
        } catch (IOException e) {
            LOG.error("Failed to create MemcachedClient", e);
        }
    }

    @Override
    public void add(String key, Object value) {
        this.client.add(key, 0, value);
    }

    @Override
    public void add(String key, Object value, int expiration) {
        this.client.add(key, expiration, value);
    }

    @Override
    public Object get(String key) {
        return this.client.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) this.client.get(key);
    }

    @Override
    public void clear() {
        this.client.flush();
    }
}