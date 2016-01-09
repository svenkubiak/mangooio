package utils;

import interfaces.Constants;
import it.unimi.dsi.util.XorShift128PlusRandom;

/**
 *
 * @author svenkubiak
 *
 */
public final class RandomUtils {
    private static XorShift128PlusRandom random = new XorShift128PlusRandom();

    private RandomUtils() {
    }

    public static int getRandomId() {
        return random.nextInt(Constants.ROWS + 1);
    }
}