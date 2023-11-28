package io.mangoo.enums;

public enum Annotation {
    COLLECTION("io.mangoo.persistence.annotations.Collection"),
    SCHEDULER("io.mangoo.scheduler.annotations.Run");

    private final String value;

    Annotation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
