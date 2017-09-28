package conf;

import java.util.UUID;

import com.google.inject.Singleton;

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

    @Override
    public void applicationInitialized() {
        final DataService dataService = Application.getInstance(DataService.class);
        dataService.dropDatabase();
        for (int i = 1; i <= Constants.WORLDS; i++) {
            final World world = new World(i, RandomUtils.getRandomId());
            dataService.save(world);
        }

        for (int i = 1; i <= Constants.FORTUNES; i++) {
            final Fortune fortune = new Fortune(i, UUID.randomUUID().toString());
            dataService.save(fortune);
        }
    }

    @Override
    public void applicationStarted() {
        // do nothing for now
    }

    @Override
    public void applicationStopped() {
        // do nothing for now
    }
}