package io.mangoo.persistence;

import com.mongodb.MongoClientSettings;
import io.mangoo.constants.Default;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatastoreSettingsTest {

    @Test
    void testCredentialsWithSpecialCharactersArePassedSeparately() {
        //given
        String password = "p@ss:w/rd%1+ ä";

        //when
        MongoClientSettings settings = DatastoreImpl.settings("localhost", 27017, true, "app", password, null).build();

        //then
        assertThat(settings.getCredential().getUserName(), equalTo("app"));
        assertThat(new String(settings.getCredential().getPassword()), equalTo(password));
        assertThat(settings.getCredential().getSource(), equalTo("admin"));
        assertThat(settings.getClusterSettings().getHosts().getFirst().toString(), equalTo("localhost:27017"));
    }

    @Test
    void testAuthDbIsUsedAsSource() {
        //when
        MongoClientSettings settings = DatastoreImpl.settings("localhost", 27017, true, "app", "secret", "users").build();

        //then
        assertThat(settings.getCredential().getSource(), equalTo("users"));
    }

    @Test
    void testNoCredentialWithoutAuth() {
        //when
        MongoClientSettings settings = DatastoreImpl.settings("localhost", 27017, false, null, null, null).build();

        //then
        assertThat(settings.getCredential(), nullValue());
    }

    @Test
    void testUrlReplacesHostAndKeepsSeparateCredentials() {
        //given
        String password = "p@ss:w/rd%1+ ä";
        String url = "mongodb://h1:27017,h2:27017/?replicaSet=rs0";

        //when
        MongoClientSettings settings = DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, url, false, true, "app", password, "users", "appdb").build();

        //then
        assertThat(settings.getClusterSettings().getHosts().stream().map(Object::toString).toList(),
                equalTo(List.of("h1:27017", "h2:27017")));
        assertThat(settings.getClusterSettings().getRequiredReplicaSetName(), equalTo("rs0"));
        assertThat(settings.getCredential().getUserName(), equalTo("app"));
        assertThat(new String(settings.getCredential().getPassword()), equalTo(password));
        assertThat(settings.getCredential().getSource(), equalTo("users"));
    }

    @Test
    void testSrvUrlKeepsDriverOptions() {
        //given
        String url = "mongodb+srv://cluster.example.mongodb.net/app?retryWrites=true&w=majority";

        //when
        MongoClientSettings settings = DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, url, false, false, null, null, null, "app").build();

        //then
        assertThat(settings.getClusterSettings().getSrvHost(), equalTo("cluster.example.mongodb.net"));
        assertThat(settings.getRetryWrites(), equalTo(true));
        assertThat(settings.getWriteConcern().getWString(), equalTo("majority"));
        assertThat(settings.getCredential(), nullValue());
    }

    @Test
    void testUrlRejectsCredentials() {
        //when
        var thrown = assertThrows(IllegalArgumentException.class, () -> DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, "mongodb://app:s3cret@h1:27017", false,
                true, "app", "other", "admin", "app"));

        //then
        assertThat(thrown.getMessage(), equalTo("persistence.mongo.url must not contain credentials. Set persistence.mongo.username, persistence.mongo.password and persistence.mongo.authdb instead."));
        assertThat(thrown.getMessage(), not(containsString("s3cret")));
    }

    @Test
    void testUrlDatabaseMustMatchDbName() {
        //when
        var thrown = assertThrows(IllegalArgumentException.class, () -> DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, "mongodb://h1:27017/other", false,
                false, null, null, null, "app"));

        //then
        assertThat(thrown.getMessage(), equalTo("persistence.mongo.url database 'other' does not match persistence.mongo.dbname 'app'"));
    }

    @Test
    void testUrlDatabaseMayMatchDbName() {
        //when
        MongoClientSettings settings = DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, "mongodb://h1:27017/app", false,
                false, null, null, null, "app").build();

        //then
        assertThat(settings.getClusterSettings().getHosts().getFirst().toString(), equalTo("h1:27017"));
    }

    @Test
    void testUrlAuthSourceMustMatchAuthDb() {
        //when
        var thrown = assertThrows(IllegalArgumentException.class, () -> DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, "mongodb://h1:27017/?authSource=other", false,
                false, null, null, null, "app"));

        //then
        assertThat(thrown.getMessage(), equalTo("persistence.mongo.url authSource 'other' does not match persistence.mongo.authdb 'admin'"));
    }

    @Test
    void testUrlAuthSourceMayMatchAuthDb() {
        //when
        MongoClientSettings settings = DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, "mongodb://h1:27017/?authSource=users", false,
                true, "app", "secret", "users", "app").build();

        //then
        assertThat(settings.getCredential().getSource(), equalTo("users"));
    }

    @Test
    void testUrlCannotBeCombinedWithEmbedded() {
        //when
        var thrown = assertThrows(IllegalArgumentException.class, () -> DatastoreImpl.settings(
                Default.PERSISTENCE_PREFIX, "localhost", 27017, "mongodb://h1:27017", true,
                false, null, null, null, "app"));

        //then
        assertThat(thrown.getMessage(), equalTo("persistence.mongo.url cannot be combined with persistence.mongo.embedded"));
    }
}
