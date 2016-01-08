package services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.svenkubiak.mangooio.mongodb.MongoDB;
import models.Fortune;
import models.World;
import utils.RandomUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class DataService {
    private static final int MAX_QUERIES = 500;
    private final MongoDB mongoDB;

    @Inject
    public DataService(MongoDB mongoDB) {
    	this.mongoDB = mongoDB;
        this.mongoDB.ensureIndexes(false);
    }

    public World findById(int id) {
        return this.mongoDB.getDatastore().find(World.class).field("id").equal(id).retrievedFields(false, "_id").get();
    }

	public void save(Object object) {
		this.mongoDB.getDatastore().save(object);
	}
	
	public List<World> getWorlds(String queries) {
		int query = 1;
		if (StringUtils.isNotBlank(queries) && StringUtils.isNumeric(queries)) {
			query = Integer.valueOf(queries);
		}
		
		if (query <= 1) {
			query = 1;
		} else if (query > MAX_QUERIES) {
			query = MAX_QUERIES;
		}
		
		List<World> worlds = new ArrayList<>();
		for (int i=0; i < query; i++) {
			worlds.add(findById(RandomUtils.getRandomId()));
		}
		return worlds;
	}

	public List<Fortune> findAllFortunes() {
		return this.mongoDB.getDatastore().find(Fortune.class).retrievedFields(false, "_id").asList();
	}
}