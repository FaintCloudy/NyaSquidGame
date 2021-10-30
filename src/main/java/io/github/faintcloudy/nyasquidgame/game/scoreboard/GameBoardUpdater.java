package io.github.faintcloudy.nyasquidgame.game.scoreboard;

import io.github.faintcloudy.squidgame.game.GamePlayer;
import io.github.faintcloudy.squidgame.utils.scoreboard.BoardUpdater;
import lombok.Getter;

public abstract class GameBoardUpdater implements BoardUpdater {
    @Getter
    private final GamePlayer gamePlayer;
    public GameBoardUpdater(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
}
