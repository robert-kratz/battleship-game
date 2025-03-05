package utils;

import java.util.Random;

public class Usernames {
    private static final String[] ADJECTIVES = {
            "Stormy", "Furious", "Dangerous", "Swift", "Tactical", "Brave", "Stealthy", "Cunning", "Relentless"
    };

    private static final String[] NOUNS = {
            "Mole", "Pirate", "Fleet", "Cannon", "Raider", "Submarine", "Destroyer", "Admiral", "Ship"
    };

    private static final Random RANDOM = new Random();

    public static String generate() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        return adjective + " " + noun;
    }
}