package io.github.faintcloudy.nyasquidgame;

import io.github.faintcloudy.nyasquidgame.game.Game;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class NyaSquidGame extends JavaPlugin {
    @Getter
    private Game game;

    @Override
    public void onEnable() {
        
    }

    public static NyaSquidGame getInstance() {
        return JavaPlugin.getPlugin(NyaSquidGame.class);
    }
}
