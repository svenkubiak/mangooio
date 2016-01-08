package conf;
    
import java.util.UUID;

import com.google.inject.Singleton;

import de.svenkubiak.embeddedmongodb.EmbeddedMongo;
import interfaces.Constants;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooLifecycle;
import models.Fortune;
import models.World;
import services.DataService;
import utils.RandomUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class Lifecycle implements MangooLifecycle {
	private static final int MONGODB_PORT = 29019;

    @Override
	public void applicationInitialized() {
        if (!Application.inProdMode()) {
            EmbeddedMongo.DB.port(MONGODB_PORT).start();
            DataService dataService = Application.getInstance(DataService.class);
            for (int i=0; i < Constants.ROWS; i++) {
                World world = new World(i + 1, RandomUtils.getRandomId());
                dataService.save(world);
                    
                Fortune fortune = new Fortune(i + 1, UUID.randomUUID().toString());
                dataService.save(fortune);
            }
        }
	}

	@Override
    public void applicationStarted() {
	    //do nothing for now
	}
}