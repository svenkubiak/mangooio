package io.mangoo.enums;

public enum Annotation {
    COLLECTION("io.mangoo.annotations.Collection"),
    INDEXED("io.mangoo.annotations.Indexed"),
    SCHEDULER("io.mangoo.annotations.Run");

    private final String value;

    Annotation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
