package io.mangoo.core;

import io.mangoo.TestExtension;
import io.mangoo.persistence.interfaces.Datastore;
import models.AnnotatedModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
class SchemaValidationTest {

    @Test
    void testDefaultSchemaIsCreate() {
        //then
        assertThat(Application.getInstance(Config.class).getMongoSchema(), equalTo("create"));
        assertThat(Application.getInstance(Config.class).isMongoSchemaCreate(), equalTo(true));
    }

    @Test
    void testValidateFailsWithTheCommandForAMissingIndexAndPassesOnceItExists() {
        //given
        var datastore = Application.getInstance(Datastore.class);
        datastore.dropCollection(AnnotatedModel.class);

        //when
        var exception = assertThrows(IllegalStateException.class,
                () -> Application.validateSchema(datastore, List.of(AnnotatedModel.class), List.of()));
        for (Application.IndexDefinition index : Application.getIndexDefinitions(AnnotatedModel.class)) {
            datastore.addIndex(AnnotatedModel.class, index.keys(), index.options());
        }

        //then
        assertThat(exception.getMessage(), containsString("persistence.mongo.schema is validate"));
        assertThat(exception.getMessage(), containsString("db.getCollection(\"annotated\").createIndex({\"email\": 1}, {\"unique\": true})"));
        assertDoesNotThrow(() -> Application.validateSchema(datastore, List.of(AnnotatedModel.class), List.of()));
    }
}
