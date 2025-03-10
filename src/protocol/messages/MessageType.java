package protocol.messages;

public enum MessageType {

    /**
     * Server -> Client
     */
    REGISTER,
    QUEUE_UPDATE,

    JOIN_GAME,
    BUILDING_PHASE_STARTS,
    BUILD_READY_STATE_CHANGE,
    GAME_IN_GAME_START,
    TURN_CHANGE,
    MOVE_MADE,
    GAME_OVER,
    ERROR,

    /**
     * Client -> Server
     */
    JOIN_QUEUE,
    LEAVE_QUEUE,
    CREATE_GAME,
    JOIN_GAME_WITH_CODE,

    PLAYER_UPDATE_SHIP_PLACEMENT,
    PLAYER_READY,
    PLAYER_MOVE,
    LEAVE_GAME,

    /**
     * Client -> Server; then Server -> Client (2x)
     */
    PLAYER_HOVER,
}
