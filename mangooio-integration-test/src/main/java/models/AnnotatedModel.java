package models;

import annotations.Label;
import io.mangoo.annotations.Collection;
import io.mangoo.annotations.Indexed;
import io.mangoo.persistence.Entity;

@Label("wrong")
@Collection(name = "annotated")
public class AnnotatedModel extends Entity {
    @Indexed(unique = true)
    @Label("email")
    private String email;

    @Label("secret")
    private String secret;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
