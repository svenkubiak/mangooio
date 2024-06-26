package io.mangoo.enums;

public enum Sort {
    ASCENDING("io.mangoo.enums.Sort.ASCENDING"),
    DESCENDING("io.mangoo.enums.Sort.DESCENDING");

    private final String value;

    Sort(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
