package mangoo.io.enums;

/**
 *
 * @author svenkubiak
 *
 */
public enum Mode {
    DEV("dev"),
    TEST("test"),
    PROD("prod");

    private final String value;

    Mode (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}