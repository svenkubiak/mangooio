package services;

import models.User;

public class DataService {
    public User getUser(String username) {
        //At this point you would normally look up a user from the database by its username
        return new User("demo", "$2a$12$YEYRK8fLNfa8smnOX9lXQ.lFOL1IjaE1peWQn7y57fa4GqX4kYgZS");
    }
}