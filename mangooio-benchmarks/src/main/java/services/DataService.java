package services;

import java.util.ArrayList;
import java.util.List;

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

    public World findById(int worldId) {
        return this.mongoDB.getDatastore().find(World.class).field("worldId").equal(worldId).retrievedFields(false, "_id").get();
    }

    public void save(Object object) {
        this.mongoDB.getDatastore().save(object);
    }

    public List<World> getWorlds(int queries) {
        if (queries < 1) {
            queries = 1;
        } else if (queries > MAX_QUERIES) {
            queries = MAX_QUERIES;
        }

        final List<World> worlds = new ArrayList<>();
        for (int i=0; i < queries; i++) {
            worlds.add(findById(RandomUtils.getRandomId()));
        }
        
        return worlds;
    }

    public List<Fortune> findAllFortunes() {
        return this.mongoDB.getDatastore().find(Fortune.class).retrievedFields(false, "_id").asList();
    }

    public void dropDatabase() {
        this.mongoDB.dropDatabase();
        this.mongoDB.ensureIndexes(false);
        this.mongoDB.getDatastore().ensureIndexes(World.class);
        this.mongoDB.getDatastore().ensureIndexes(Fortune.class);
    }
}