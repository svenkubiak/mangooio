package services;

import java.util.List;

import org.mongodb.morphia.query.FindOptions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.svenkubiak.mangooio.mongodb.MongoDB;
import models.Fortune;
import models.World;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class DataService {
    private final MongoDB mongoDB;

    @Inject
    public DataService(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
        this.mongoDB.ensureIndexes(false);
    }

    public World findById(int worldId) {
        return this.mongoDB.getDatastore().find(World.class).field("worldId").equal(worldId).project("_id", false).get();
    }

    public void save(Object object) {
        this.mongoDB.getDatastore().save(object);
    }

    public List<World> findWorlds(int limit) {
        return this.mongoDB.getDatastore().find(World.class).asList(new FindOptions().limit(limit));
    }

    public List<Fortune> findAllFortunes() {
        return this.mongoDB.getDatastore().find(Fortune.class).project("_id", false).asList();
    }

    public void dropDatabase() {
        this.mongoDB.dropDatabase();
        this.mongoDB.ensureIndexes(false);
        this.mongoDB.getDatastore().ensureIndexes(World.class);
        this.mongoDB.getDatastore().ensureIndexes(Fortune.class);
    }
}