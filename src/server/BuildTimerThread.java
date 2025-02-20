package server;

import java.util.Date;

public class BuildTimerThread extends Thread {

    private final BattleShipGame battleShipGame;
    private final int buildTime;

    public BuildTimerThread(BattleShipGame battleShipGame, int buildTime) {
        this.battleShipGame = battleShipGame;
        this.buildTime = buildTime;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(buildTime);

            System.out.println("Build time is up!");

            this.battleShipGame.getGameState().setStatus(GameState.GameStatus.IN_GAME);
            this.battleShipGame.getGameState().setPlayersTurn(this.battleShipGame.getGameState().playerA);

            Date playersTurnStart = new Date();
            Date playersTurnEnd = new Date(playersTurnStart.getTime() + (1000 * Parameters.SHOOT_TIME_IN_SECONDS));

            this.battleShipGame.getGameState().setPlayersTurnStart(playersTurnStart);
            this.battleShipGame.getGameState().setPlayersTurnEnd(playersTurnEnd);

            System.out.println("Player A's turn started at: " + playersTurnStart);
            System.out.println("Player A's turn ends at: " + playersTurnEnd);
            System.out.println("Gamestate: " + this.battleShipGame.getGameState().getStatus().toString());

            System.out.println("Server GameState vor sendGameStateUpdate(): " + this.battleShipGame.getGameState().getStatus());
            battleShipGame.sendGameStateUpdate();
            System.out.println("Server GameState nach sendGameStateUpdate(): " + this.battleShipGame.getGameState().getStatus());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
