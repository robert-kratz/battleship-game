package client;

import protocol.messages.*;

public class LobbyHandler implements LobbyClient {

    private ClientHandler clientHandler;

    private boolean inQueue = false;
    private int queueLength = 0;

    public LobbyHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    //SERVER -> CLIENT (this)

    /**
     * Called when the client receives a lobby update message.
     * @param queueUpdateMessage The message containing the queue information.
     */
    @Override
    public void onQueueUpdate(QueueUpdateMessage queueUpdateMessage) {
        this.queueLength = queueUpdateMessage.getQueueSize();
        this.inQueue = queueUpdateMessage.isPlayerInQueue();

        // Update UI
        this.clientHandler.getStageManager().lobbyScene.setQueueLength(queueLength);
    }

    /**
     * Called when the client receives an error message.
     * @param errorMessage The error message.
     */
    @Override
    public void onLobbyError(ErrorMessage errorMessage) {
        System.out.println("Error in lobby: " + errorMessage.getError());

        //Show in swift dialog
        this.clientHandler.showError(errorMessage.getError().getMessage());
    }

    //CLIENT (this) -> SERVER

    /**
     * Join the queue to find a game.
     */
    @Override
    public void sendJoinQueueEvent() {
        if(inQueue) throw new IllegalStateException("Already in queue.");
        this.clientHandler.sendMessage(new JoinQueueMessage());
    }

    /**
     * Leave the queue.
     */
    @Override
    public void sendLeaveQueueEvent() {
        if(!inQueue) throw new IllegalStateException("Not in queue.");
        this.clientHandler.sendMessage(new QuitQueueMessage());
    }

    /**
     * Create a game.
     * @param size The size of the game.
     */
    @Override
    public void sendCreateGameEvent(int size) {
        this.clientHandler.sendMessage(new CreateGameMessage(size));
    }

    /**
     * Join a game with a code.
     * @param code The code of the game.
     */
    @Override
    public void sendJoinGameWithCodeEvent(int code) {
        this.clientHandler.sendMessage(new JoinGameWithCodeMessage(code));
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public boolean isInQueue() {
        return inQueue;
    }
}
