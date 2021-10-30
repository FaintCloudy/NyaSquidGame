package io.github.faintcloudy.nyasquidgame.game;

import io.github.faintcloudy.squidgame.PlayerState;
import io.github.faintcloudy.squidgame.utils.player.IntactPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GamePlayer {
    @Getter
    private final UUID uuid;
    private IntactPlayer entityCached;
    @Getter @Setter
    private PlayerState playerState;
    private GamePlayer(UUID uuid) {
        this.uuid = uuid;
        this.entityCached = IntactPlayer.of(Bukkit.getPlayer(uuid));
        this.playerState = PlayerState.IDLE;
    }

    public IntactPlayer getEntity() {
        if (this.entityCached != null) return this.entityCached;
        this.entityCached = IntactPlayer.of(Bukkit.getPlayer(uuid));
        return this.entityCached;
    }

    public boolean isOnline() {
        return this.getEntity() != null && this.getEntity().isOnline();
    }

    public static class GamePlayerManager {
        @Getter
        private static final List<GamePlayer> gamePlayerList = new ArrayList<>();
        public static void addToList(GamePlayer player) {
            if (!gamePlayerList.contains(player))
                gamePlayerList.add(player);
        }

        public static GamePlayer getPlayer(UUID uuid) {
            for (GamePlayer player : gamePlayerList) {
                if (player.getUuid() == uuid) {
                    return player;
                }
            }

            GamePlayer player = new GamePlayer(uuid);
            GamePlayerManager.addToList(player);
            return player;
        }

        public static GamePlayer getPlayer(Entity entity) {
            if (!(entity instanceof Player))
                throw new IllegalArgumentException("The entity can only be a player!");
            return GamePlayerManager.getPlayer(entity.getUniqueId());
        }

        public static List<GamePlayer> getOnlinePlayers() {
            List<GamePlayer> onlinePlayers = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> onlinePlayers.add(GamePlayerManager.getPlayer(player)));
            return onlinePlayers;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        GamePlayer that = (GamePlayer) o;
        return Objects.equals(this.uuid, that.uuid);
    }

}
