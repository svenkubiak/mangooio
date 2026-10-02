package io.mangoo.persistence;

import com.mongodb.MongoClientSettings;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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
}
