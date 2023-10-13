package io.mangoo.persistence;

import static com.mongodb.client.model.Filters.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
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
    void testConcurrentInsert() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String name = UUID.randomUUID().toString();
            TestModel model = new TestModel(name);
            
            //when
            datastore.save(model);
            
            // then
            return datastore.query(TestModel.class).find(eq("name", name)).first() != null;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
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
    void testFindById() {
        //given
        datastore.dropDatabase();
        TestModel model = new TestModel("foo");
        
        //when
        String id = datastore.save(model);

        assertThat(id, not(nullValue()));
        assertThat(datastore.findById(id, TestModel.class), not(nullValue()));
    }
    
    @Test
    void testConcurrentFindById() {
        //given
        datastore.dropDatabase();
        
        MatcherAssert.assertThat(t -> {
            //given
            String id = UUID.randomUUID().toString();
            TestModel model = new TestModel(id);
            
            //when
            String _id = datastore.save(model);
            
            // then
            return datastore.findById(_id, TestModel.class) != null;
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
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
    void testAddCollection() {
        //given
        datastore.dropDatabase();
        
        //when
        datastore.addCollection(TestModel.class.getClass().getName(), "bar");

        //then
        assertThat(datastore.getCollection(TestModel.class.getClass()), not(nullValue()));
    }
    
    @Test
    void testPrefix() {
        //given
        Datastore datastore = Application.getInstance(DatastoreProvider.class).getDatastore("myprefix");
        
        //then
        assertThat(datastore, not(nullValue()));
    }
}