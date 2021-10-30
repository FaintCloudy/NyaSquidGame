package io.github.faintcloudy.nyasquidgame.game.level;

import lombok.Getter;

public enum LevelType {
    STATUES(new Statues(1), "123, 木头人");
    @Getter
    private final Level level;
    @Getter
    private String displayName;
    LevelType(Level level, String displayName) {
        this.level = level;
    }

}

