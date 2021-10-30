package io.github.faintcloudy.nyasquidgame.game;

import io.github.faintcloudy.squidgame.PlayerState;
import io.github.faintcloudy.squidgame.SquidGame;
import io.github.faintcloudy.squidgame.game.level.Level;
import io.github.faintcloudy.squidgame.game.level.LevelType;
import io.github.faintcloudy.squidgame.game.scoreboard.WaitingStateUpdater;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Game {
    @Getter
    private final List<Level> levels = new ArrayList<>();
    @Getter @Setter
    private int currentLevel = 0;
    @Getter
    private final Configuration config;
    @Getter @Setter
    private GameState gameState;
    @Getter
    private final List<GamePlayer> initialGamePlayers;
    public Game(Configuration config) {
        this.config = config;
        this.gameState = GameState.WAITING;
        this.initialGamePlayers = new ArrayList<>();
        Arrays.stream(LevelType.values()).collect(Collectors.toList()).forEach(levelType
                -> levels.add(levelType.getLevel()));
        Bukkit.getPluginManager().registerEvents(new EnvironmentProtection(), SquidGame.getInstance());
    }

    public void start() {
        this.setGameState(GameState.GAMING);
        this.initialGamePlayers.addAll(GamePlayer.GamePlayerManager.getOnlinePlayers());
        this.initialGamePlayers.forEach(player -> player.setPlayerState(PlayerState.GAMING));
        this.nextLevel();
    }

    public void nextLevel() {
        if (this.currentLevel + 1 > levels.size()) {
            this.end();
            return;
        }

        this.currentLevel++;
        Level level = this.getInstancedLevel();
        level.init(this.config.getConfigurationSection("levels").getConfigurationSection(level.getLevelType().name()));
    }

    public Level getInstancedLevel() {
        return this.levels.get(this.currentLevel);
    }

    public void end() {
        this.setGameState(GameState.ENDING);
        //TODO a lot
    }

    @SuppressWarnings("unchecked")
    public List<GamePlayer> getRemainderPlayers() {
        return ((ArrayList<GamePlayer>) (((ArrayList<GamePlayer>) this.initialGamePlayers).clone())).stream().filter(player ->
                player.getPlayerState() == PlayerState.GAMING).collect(Collectors.toList());
    }

    public void handlePlayerJoin(GamePlayer gamePlayer) {
        Location lobbyLocation = (Location) config.get("map.lobby");
        gamePlayer.getEntity().teleport(lobbyLocation);
        gamePlayer.getEntity().getSidebarBoard().update(new WaitingStateUpdater(gamePlayer), 20);
        Bukkit.broadcastMessage(gamePlayer.getEntity().getDisplayNameFromLuckPerms() + String
                .format(" §e加入了游戏 (§b%d§e/§b%d§e)", Bukkit.getOnlinePlayers().size(), config.getInt("max-players")));
        //TODO load scoreboard and send message, titles, sound, effect. replace skin.
    }

    public class EnvironmentProtection implements Listener {
        @EventHandler
        public void onLobbyDamage(EntityDamageEvent event) {
            if (gameState == GameState.WAITING)
                event.setCancelled(true);
        }

        @EventHandler
        public void onLobbyBreak(BlockBreakEvent event) {
            if (gameState == GameState.WAITING && event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }

        @EventHandler
        public void onLobbyPlace(BlockPlaceEvent event) {
            if (gameState == GameState.WAITING && event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }

        @EventHandler
        public void onLobbyPickupItem(PlayerPickupItemEvent event) {
            if (gameState == GameState.WAITING)
                event.setCancelled(true);
        }

        @EventHandler
        public void onLobbyDropItem(PlayerDropItemEvent event) {
            if (gameState == GameState.WAITING)
                event.setCancelled(true);
        }

        @EventHandler
        public void onLobbyInventoryClick(InventoryClickEvent event) {
            if (gameState == GameState.WAITING && event.getWhoClicked().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }

        @EventHandler
        public void onFoodLevel(FoodLevelChangeEvent event) {
            event.setFoodLevel(20);
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            event.setJoinMessage("");
            if (gameState == GameState.WAITING) {
                handlePlayerJoin(GamePlayer.GamePlayerManager.getPlayer(event.getPlayer().getUniqueId()));
            } else {
                event.getPlayer().kickPlayer("§c游戏已开始");
            }
        }

        @EventHandler
        public void onLogin(PlayerLoginEvent event) {
            if (gameState != GameState.WAITING) {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.setKickMessage("§c游戏已开始");
            }
        }
    }

    public void eliminate(GamePlayer player) {
        player.setPlayerState(PlayerState.ELIMINATED);
        this.toSpectator(player);

        Bukkit.broadcastMessage("§c§l" + player.getEntity().getName() + " 已被淘汰");
    }

    public void toSpectator(GamePlayer player) {

        //TODO a lot
    }
}
