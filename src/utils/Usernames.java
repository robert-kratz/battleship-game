package utils;

import java.util.Random;

public class Usernames {
    private static final String[] ADJECTIVES = {
            "Stürmische", "Wütend", "Gefährlich", "Schnell", "Taktisch", "Mutig", "Heimlich", "Listig", "Hartnäckig"
    };

    private static final String[] NOUNS = {
            "Wühlmaus", "Pirat", "Flotte", "Kanone", "Seeräuber", "U-Boot", "Zerstörer", "Admiral", "Schiff"
    };

    private static final Random RANDOM = new Random();

    public static String generate() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        return adjective + " " + noun;
    }
}
