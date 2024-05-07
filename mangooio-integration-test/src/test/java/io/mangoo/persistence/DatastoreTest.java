package io.mangoo.persistence;

import com.mongodb.client.model.Sorts;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.models.TestModel;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.utils.MangooUtils;
import org.bson.Document;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class DatastoreTest {
    private static final int THREADS = 50;
    private static Datastore datastore;

    @BeforeAll
    public static void setup() {
        datastore = Application.getInstance(Datastore.class);
    }
    
    @Test
    void testConcurrentSave() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String name = UUID.randomUUID().toString();
            TestModel model = new TestModel(name);
            
            //when
            datastore.save(model);
            
            // then
            return datastore.query(TestModel.class).find(eq("name", name)).first() != null;
        }, new ConcurrentRunner<>(new AtomicInteger(), THREADS));
    }

    @Test
    void testSaveAndDrop() {
        //given
        datastore.dropDatabase();
        TestModel model = new TestModel("foo");
        
        //when
        datastore.save(model);
        
        //then
        assertThat(datastore.findAll(TestModel.class).size(), equalTo(1));

        //when
        datastore.dropDatabase();
        
        //then
        assertThat(datastore.findAll(TestModel.class).size(), equalTo(0));
    }

    @Test
    void testFind() {
        //given
        String name = "foo";
        datastore.dropDatabase();
        TestModel model = new TestModel(name);
        
        //when
        datastore.save(model);

        assertThat(datastore.find(TestModel.class, eq("name", name)), not(nullValue()));
    }

    @Test
    void testFindFirst() {
        //given
        String name = MangooUtils.uuid();
        String name2 = MangooUtils.uuid();
        datastore.dropDatabase();
        TestModel model = new TestModel(name, LocalDateTime.now());
        TestModel model2 = new TestModel(name2, LocalDateTime.now().plusMinutes(25));

        //when
        datastore.save(model);
        datastore.save(model2);

        assertThat(datastore.findFirst(TestModel.class, Sorts.ascending()).getName(), equalTo(name));
    }

    @Test
    void testFindAllLimit() {
        //given
        datastore.dropDatabase();
        TestModel model = new TestModel("foo", LocalDateTime.now());
        TestModel model2 = new TestModel("foo", LocalDateTime.now());
        TestModel model3 = new TestModel("foo", LocalDateTime.now());
        TestModel model4 = new TestModel(MangooUtils.uuid(), LocalDateTime.now());
        TestModel model5 = new TestModel(MangooUtils.uuid(), LocalDateTime.now());

        //when
        datastore.saveAll(List.of(model, model2, model3, model4, model5));

        assertThat(datastore.findAll(TestModel.class, eq("name", "foo"), Sorts.ascending("timestamp"), 2).size(), equalTo(2));
    }

    @Test
    void testConcurrentFind() {
        //given
        datastore.dropDatabase();
        
        MatcherAssert.assertThat(t -> {
            //given
            String name = UUID.randomUUID().toString();
            TestModel model = new TestModel(name);
            
            //when
            datastore.save(model);
            
            // then
            return datastore.find(TestModel.class, eq("name", name)) != null;
        }, new ConcurrentRunner<>(new AtomicInteger(), THREADS));
    }

    @Test
    void testCountAll() {
        //given
        datastore.dropDatabase();
        
        //when
        datastore.save(new TestModel("foo"));
        datastore.save(new TestModel("bar"));
        datastore.save(new TestModel("bla"));

        //then
        assertThat(datastore.countAll(TestModel.class), equalTo(3L));
    }
    
    @Test
    void testFindAll() {
        //given
        datastore.dropDatabase();
        
        //when
        datastore.save(new TestModel("foo"));
        datastore.save(new TestModel("bar"));
        datastore.save(new TestModel("bla"));

        //then
        assertThat(datastore.findAll(TestModel.class).size(), equalTo(3));
    }

    @Test
    void testFindAllSort() {
        //given
        datastore.dropDatabase();

        //when
        datastore.save(new TestModel("foo"));
        datastore.save(new TestModel("bar"));
        datastore.save(new TestModel("bla"));

        //then
        assertThat(datastore.findAll(TestModel.class, Sorts.ascending()).size(), equalTo(3));
    }

    @Test
    void testFindAllSortAndQuery() {
        //given
        String name = "foo";
        datastore.dropDatabase();

        //when
        datastore.save(new TestModel(name));
        datastore.save(new TestModel(name));
        datastore.save(new TestModel("bla"));

        //then
        assertThat(datastore.findAll(TestModel.class, eq("name", name), Sorts.ascending()).size(), equalTo(2));
    }

    @Test
    void testSaveAll() {
        //given
        datastore.dropDatabase();

        //when
        List<TestModel> models = List.of(new TestModel("foo"), new TestModel("bar"), new TestModel("bla"));
        datastore.saveAll(models);

        //then
        assertThat(datastore.findAll(TestModel.class).size(), equalTo(3));
    }
    
    @Test
    void testDropCollection() {
        //given
        datastore.dropDatabase();
        
        //when
        datastore.dropCollection(TestModel.class);
    }

    @Test
    void testQueryByClass() {
        //given
        datastore.dropDatabase();
        datastore.save(new TestModel("foo"));

        //when
        TestModel testModel = (TestModel) datastore.query(TestModel.class).find(eq("name", "foo")).first();

        //when
        assertThat(testModel, not(nullValue()));
    }

    @Test
    void testQueryByCollectionName() {
        //given
        datastore.dropDatabase();
        datastore.save(new TestModel("foo"));

        //when
        Document document = (Document) datastore.query("tests").find(eq("name", "foo")).first();

        //when
        assertThat(document, not(nullValue()));
    }

    @Test
    void testPrefix() {
        //given
        Datastore datastore = Application.getInstance(DatastoreProvider.class).getDatastore("myprefix");
        
        //then
        assertThat(datastore, not(nullValue()));
    }
}