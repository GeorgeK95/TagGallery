package TagGallery.utils;

import java.util.Random;

public final class RandomNumber {

    private RandomNumber() {
    }

    public static int getRandomNumber(int bound) {
        Random r = new Random();
        return r.nextInt(bound);
    }

    public static int getRandomNumber(int min, int max) {
        return (int) (min + (Math.random() * (max - min)));
    }
}
