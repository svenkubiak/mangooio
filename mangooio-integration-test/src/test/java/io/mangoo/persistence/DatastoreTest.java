package io.mangoo.persistence;

import com.mongodb.client.model.Sorts;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.models.TestModel;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.utils.CommonUtils;
import models.Person;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class DatastoreTest {
    private static Datastore datastore;

    @BeforeAll
    public static void setup() {
        datastore = Application.getInstance(Datastore.class);
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
    void testIndexesExist() {
        //given
        String firstname = null;
        String lastname = null;
        String address = null;
        String age = null;

        //when
        for (Object object : datastore.query(Person.class).listIndexes()) {
            Document document = (Document) ((Document) object).get("key");

            if (document.get("firstname") != null) {
                firstname = document.get("firstname").toString();
            }

            if (document.get("lastname") != null) {
                lastname = document.get("lastname").toString();
            }

            if (document.get("address") != null) {
                address = document.get("address").toString();
            }

            if (document.get("age") != null) {
                age = document.get("age").toString();
            }
        }

        //then
        assertThat(firstname, not(nullValue()));
        assertThat(firstname, equalTo("1"));
        assertThat(lastname, not(nullValue()));
        assertThat(lastname, equalTo("-1"));
        assertThat(address, not(nullValue()));
        assertThat(address, equalTo("-1"));
        assertThat(age, not(nullValue()));
        assertThat(age, equalTo("-1"));
    }

    @Test
    void testFindFirst() {
        //given
        String name = CommonUtils.uuidV6();
        String name2 = CommonUtils.uuidV6();
        datastore.dropDatabase();
        TestModel model = new TestModel(name, LocalDateTime.now());
        TestModel model2 = new TestModel(name2, LocalDateTime.now().plusMinutes(25));

        //when
        datastore.save(model);
        datastore.save(model2);

        //then
        assertThat(datastore.findFirst(TestModel.class, Sorts.descending("timestamp")).getName(), equalTo(name2));
    }

    @Test
    void testFindAllLimit() {
        //given
        datastore.dropDatabase();
        TestModel model = new TestModel("foo", LocalDateTime.now());
        TestModel model2 = new TestModel("foo", LocalDateTime.now());
        TestModel model3 = new TestModel("foo", LocalDateTime.now());
        TestModel model4 = new TestModel(CommonUtils.uuidV6(), LocalDateTime.now());
        TestModel model5 = new TestModel(CommonUtils.uuidV6(), LocalDateTime.now());

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
            String name = CommonUtils.uuidV6();
            TestModel model = new TestModel(name);
            
            //when
            datastore.save(model);
            
            // then
            return datastore.find(TestModel.class, eq("name", name)) != null;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
    void testFindAllSort() {
        //given
        datastore.dropDatabase();

        //when
        datastore.save(new TestModel("foo"));
        datastore.save(new TestModel("bar"));
        datastore.save(new TestModel("bla"));

        //then
        List<TestModel> models = datastore.findAll(TestModel.class, Sorts.ascending("name"));
        assertThat(models.stream().map(TestModel::getName).toList(), contains("bar", "bla", "foo"));
    }

    @Test
    void testFindAllSortAndQuery() {
        //given
        String name = "foo";
        datastore.dropDatabase();

        //when
        datastore.save(new TestModel(name, LocalDateTime.now()));
        datastore.save(new TestModel(name, LocalDateTime.now().plusMinutes(25)));
        datastore.save(new TestModel("bla", LocalDateTime.now().plusMinutes(50)));

        //then
        List<TestModel> models = datastore.findAll(TestModel.class, eq("name", name), Sorts.descending("timestamp"));
        assertThat(models.size(), equalTo(2));
        assertThat(models.get(0).getTimestamp().isAfter(models.get(1).getTimestamp()), equalTo(true));
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
        datastore.save(new TestModel("foo"));

        //when
        datastore.dropCollection(TestModel.class);

        //then
        assertThat(datastore.countAll(TestModel.class), equalTo(0L));
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
    void testSaveWithIdSetByApplicationInsertsDocument() {
        //given
        TestModel model = new TestModel(CommonUtils.uuidV6());
        ObjectId id = new ObjectId();
        model.setId(id);

        //when
        String result = datastore.save(model);

        //then
        assertThat(result, equalTo(id.toString()));
        TestModel found = datastore.query(TestModel.class).find(eq("_id", id)).first();
        assertThat(found, not(nullValue()));
        assertThat(found.getName(), equalTo(model.getName()));
    }

    @Test
    void testSaveExistingDocumentReplacesIt() {
        //given
        String initial = CommonUtils.uuidV6();
        datastore.save(new TestModel(initial));
        TestModel model = datastore.query(TestModel.class).find(eq("name", initial)).first();
        String name = CommonUtils.uuidV6();
        model.setName(name);

        //when
        datastore.save(model);

        //then
        assertThat(datastore.query(TestModel.class).countDocuments(eq("_id", model.getId())), equalTo(1L));
        TestModel found = datastore.query(TestModel.class).find(eq("_id", model.getId())).first();
        assertThat(found.getName(), equalTo(name));
    }

    @Test
    void testCloseReleasesTheClient() {
        //given
        var closable = new DatastoreImpl(Application.getInstance(Config.class));
        boolean healthyBefore = closable.isHealthy();

        //when
        closable.close();

        //then
        assertThat(healthyBefore, equalTo(true));
        assertThat(closable.isHealthy(), equalTo(false));
        assertThat(datastore.isHealthy(), equalTo(true));
    }

    @Test
    void testConcurrentFirstCallsForAPrefixShareOneDatastore() throws Exception {
        //given
        var provider = Application.getInstance(DatastoreProvider.class);
        String prefix = "concurrent" + CommonUtils.uuidV6().replace("-", "");
        var start = new java.util.concurrent.CountDownLatch(1);
        java.util.Set<Datastore> datastores = java.util.concurrent.ConcurrentHashMap.newKeySet();

        //when
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(20)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (var i = 0; i < 20; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    datastores.add(provider.getDatastore(prefix));
                    return null;
                }));
            }
            start.countDown();
            for (var future : futures) {
                future.get();
            }
        }

        //then
        assertThat(datastores.size(), equalTo(1));
        ((DatastoreImpl) datastores.iterator().next()).close();
    }
}
