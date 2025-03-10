package server;

public class GameContainer {
    private final BattleShipGame game;
    private final Thread thread;

    /**
     * Creates a new GameContainer with the specified game and thread.
     * @param game the game to be contained
     * @param thread the thread to be associated with the game
     */
    public GameContainer(BattleShipGame game, Thread thread) {
        this.game = game;
        this.thread = thread;
    }

    /**
     * Returns the game contained in this container.
     * @return the game
     */
    public BattleShipGame getGame() {
        return game;
    }

    /**
     * Returns the thread associated with this container.
     * @return the thread
     */
    public Thread getThread() {
        return thread;
    }
}