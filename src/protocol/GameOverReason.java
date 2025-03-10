package protocol;

/**
 * Enum representing the reasons for game over.
 */
public enum GameOverReason {
    PLAYER_LEFT_IN_GAME,
    PLAYER_LEFT_LOBBY,
    TIMEOUT,
    NO_MORE_MOVES,
    PLAYER_WON;
}
