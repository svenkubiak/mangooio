package mangooio;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

import de.svenkubiak.embeddedmongodb.EmbeddedMongo;
import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 * 
 * @author svenkubiak
 *
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
@SuppressWarnings("all")
public class TestSuite {
    @BeforeClass
    public static final void start() {
        before();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        Application.main(null);
    }

    private static void before() {
        EmbeddedMongo.DB.port(29019).start();
    }
}