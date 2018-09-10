package mangooio;

import de.svenkubiak.embeddedmongodb.EmbeddedMongo;
import io.mangoo.test.TestRunner;

public class TestExtension extends TestRunner {
    public static final int THREADS = 100;

    @Override
    public void init() {
        EmbeddedMongo.DB.port(29019).start();
    }
}