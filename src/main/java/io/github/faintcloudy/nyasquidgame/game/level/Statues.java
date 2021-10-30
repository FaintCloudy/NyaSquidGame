package io.github.faintcloudy.nyasquidgame.game.level;

import io.github.faintcloudy.squidgame.SquidGame;
import io.github.faintcloudy.squidgame.game.GamePlayer;
import io.github.faintcloudy.squidgame.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.List;

public class Statues extends Level implements Listener {

    public Statues(int order) {
        super(order, LevelType.STATUES);
    }

    @Override
    public void init() {
        List<GamePlayer> gamePlayerList = SquidGame.getInstance().getGame().getInitialGamePlayers();
        ConfigurationSection levelConfig = this.getLevelConfig();
        Location spawnAreaPosition1 = (Location) levelConfig.get("spawn-area.spawn-area-position-1");
        Location spawnAreaPosition2 = (Location) levelConfig.get("spawn-area.spawn-area-position-2");
        gamePlayerList.forEach(player -> {
            player.getEntity().teleport(LocationUtil.randomPositionInCube(spawnAreaPosition1, spawnAreaPosition2));
        });

        Bukkit.getPluginManager().registerEvents(this, SquidGame.getInstance());
    }

    public void start() {

    }

}
