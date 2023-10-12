package io.mangoo.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.models.TestModel;

@ExtendWith({TestExtension.class})
class DatastoreTest {
    private static final int THREADS = 50;
    private static Datastore datastore;

    @BeforeAll
    public static void setup() {
        datastore = Application.getInstance(Datastore.class);
    }

    @Test
    void testInit() {
        assertThat(datastore.getMongoClient(), not(nullValue()));
    }
    
//    @Test
//    void testConcurrentInsert() throws InterruptedException {
//        MatcherAssert.assertThat(t -> {
//            //given
//            String id = UUID.randomUUID().toString();
//            TestModel model = new TestModel(id);
//            
//            //when
//            datastore.save(model);
//            
//            // then
//            return datastore.query().find(TestModel.class).filter(Filters.eq("name", id)).first() != null;
//        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
//    }

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
    void testFindById() {
//        //given
//        datastore.dropDatabase();
//        TestModel model = new TestModel("foo");
//        
//        //when
//        datastore.save(model);
//        TestModel storedModel = datastore.getDatastore()
//            .find(TestModel.class)
//            .filter(Filters.eq("name", "foo"))
//            .first();
//
//        assertThat(storedModel, not(nullValue()));
//        assertThat(datastore.findById(storedModel.getId().toString(), TestModel.class), not(nullValue()));
    }
    
    @Test
    @Disabled
    void testConcurrentFindById() {
        //given
        datastore.dropDatabase();
        
        MatcherAssert.assertThat(t -> {
            //given
            String id = UUID.randomUUID().toString();
            TestModel model = new TestModel(id);
            
            //when
            datastore.save(model);
            
            // then
            return datastore.findById(model.getId().toString(), TestModel.class) != null;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
    }

    @Test
    @Disabled
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
    @Disabled
    void testDeleteAll() {
        //given
        datastore.dropDatabase();
        
        //when
        datastore.save(new TestModel("foo"));
        datastore.save(new TestModel("bar"));
        datastore.save(new TestModel("bla"));

        //then
        assertThat(datastore.countAll(TestModel.class), equalTo(3L));

        //when
        datastore.deleteAll(TestModel.class);
        
        //then
        assertThat(datastore.countAll(TestModel.class), equalTo(0L));
    }
    
    @Test
    @Disabled
    void testDelete() {
        //given
        TestModel testModel = new TestModel("foo");
        datastore.dropDatabase();
        
        //when
        datastore.save(testModel);

        //then
        assertThat(datastore.countAll(TestModel.class), equalTo(1L));

        //when
        datastore.delete(testModel);
        
        //then
        assertThat(datastore.countAll(TestModel.class), equalTo(0L));
    }
    
    @Test
    void testPrefix() {
        //given
        Datastore datastore = Application.getInstance(DatastoreProvider.class).getDatastore("myprefix");
        
        //then
        assertThat(datastore, not(nullValue()));
    }
}