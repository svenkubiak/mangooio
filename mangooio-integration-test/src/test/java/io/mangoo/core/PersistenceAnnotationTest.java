package io.mangoo.core;

import io.mangoo.TestExtension;
import io.mangoo.utils.PersistenceUtils;
import models.AnnotatedModel;
import models.Person;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class PersistenceAnnotationTest {

    @Test
    void testCollectionNameIsReadFromCollectionAnnotation() {
        assertThat(PersistenceUtils.getCollectionName(AnnotatedModel.class), equalTo("annotated"));
    }

    @Test
    void testIndexedFieldWithFurtherAnnotationGetsIndex() {
        //given
        Application.IndexDefinition index = index(AnnotatedModel.class, "email");

        //then
        assertThat(index, not(nullValue()));
        assertThat(index.keys().toBsonDocument(), equalTo(new BsonDocument("email", new BsonInt32(1))));
        assertThat(index.options().isUnique(), equalTo(true));
        assertThat(index.options().getCollation(), nullValue());
    }

    @Test
    void testFieldWithForeignAnnotationGetsNoIndex() {
        assertThat(index(AnnotatedModel.class, "secret"), nullValue());
        assertThat(Application.getIndexDefinitions(AnnotatedModel.class), hasSize(1));
    }

    @Test
    void testIndexOptionsAreReadFromIndexedAnnotation() {
        //given
        Application.IndexDefinition lastname = index(Person.class, "lastname");
        Application.IndexDefinition firstname = index(Person.class, "firstname");

        //then
        assertThat(lastname.keys().toBsonDocument(), equalTo(new BsonDocument("lastname", new BsonInt32(-1))));
        assertThat(lastname.options().isUnique(), equalTo(true));
        assertThat(firstname.keys().toBsonDocument(), equalTo(new BsonDocument("firstname", new BsonInt32(1))));
        assertThat(firstname.options().isUnique(), equalTo(false));
    }

    private static Application.IndexDefinition index(Class<?> clazz, String field) {
        List<Application.IndexDefinition> indexes = Application.getIndexDefinitions(clazz);
        return indexes.stream()
                .filter(index -> index.field().equals(field))
                .findFirst()
                .orElse(null);
    }
}
