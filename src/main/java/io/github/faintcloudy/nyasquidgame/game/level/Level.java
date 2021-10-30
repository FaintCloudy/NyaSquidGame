package io.github.faintcloudy.nyasquidgame.game.level;

import io.github.faintcloudy.squidgame.SquidGame;
import io.github.faintcloudy.squidgame.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Level {
    @Getter
    private ConfigurationSection levelConfig;
    @Getter @Setter
    private int order;
    @Getter
    private final LevelType levelType;
    @Getter @Setter
    private LevelState levelState;
    @Getter @Setter
    private int colddown;
    @Getter @Setter
    private int eliminated;
    private final Game game;
    public Level(int order, LevelType levelType) {
        this.order = order;
        this.levelType = levelType;
        this.levelState = LevelState.IDLE;
        this.colddown = -1;
        this.eliminated = 0;
        this.game = SquidGame.getInstance().getGame();
    }

    public abstract void init();
    public abstract void start();

    public final void init(ConfigurationSection levelConfig) {
        this.levelConfig = levelConfig;
        this.setLevelState(LevelState.WAITING);
        this.init();
        Level.this.colddown = 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                Level.this.setColddown(Level.this.getColddown() - 1);
                if (Level.this.getColddown() <= 0) {
                    Level.this.setLevelState(LevelState.GAMING);
                    Level.this.start();
                    Level.this.setColddown(-1);
                    this.cancel();
                }
            }
        }.runTaskTimer(SquidGame.getInstance(), 0, 20);
    }

    public final void end() {
        this.setLevelState(LevelState.ENDING);
        Bukkit.broadcastMessage("§c§l本场游戏已结束, 共淘汰 " + eliminated + " 名玩家");
        Bukkit.broadcastMessage("§c§l目前剩余 " + game.getRemainderPlayers().size() + " 名玩家");
        new BukkitRunnable() {
            @Override
            public void run() {
                game.nextLevel();
            }
        }.runTaskLater(SquidGame.getInstance(), 100);
    }

}
