package io.github.faintcloudy.nyasquidgame.game.scoreboard;

import io.github.faintcloudy.squidgame.game.GamePlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class WaitingStateUpdater extends GameBoardUpdater {
    public WaitingStateUpdater(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    @Override
    public List<String> body() {
        return WaitingStateUpdater.getTemplate(); //TODO lots of replacements
    }

    @Getter @Setter
    private static List<String> template;
}
