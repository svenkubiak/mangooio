package io.mangoo.providers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class CacheProvider implements Provider<Cache> {
    private static final Logger LOG = LogManager.getLogger(CacheProvider.class);
	private static final Config CONFIG = Application.getConfig();
	private Cache cache;

	@Inject
	public CacheProvider(Injector injector) {
		Class<? extends Cache> cacheClass = null; 
		try {
			cacheClass = Class.forName(CONFIG.getCacheClass()).asSubclass(Cache.class);
		} catch (ClassNotFoundException e) {
			LOG.error("Can not find cache class: " + CONFIG.getCacheClass());
		}
		
		if (cacheClass != null) {
			this.cache = injector.getInstance(cacheClass);
			LOG.info("Using {} as implementation for Cache",  cacheClass);
		}
	}

	@Override
	public Cache get() {
		return this.cache;
	}
}