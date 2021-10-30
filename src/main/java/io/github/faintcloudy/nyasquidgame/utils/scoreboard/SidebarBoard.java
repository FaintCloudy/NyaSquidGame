package io.github.faintcloudy.nyasquidgame.utils.scoreboard;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class SidebarBoard {
    @Getter
    private final Objective objective;
    @Getter
    private final Scoreboard scoreboard;
    @Getter
    private final Plugin plugin;
    @Getter @Setter
    private BoardUpdater boardUpdater;
    private BukkitTask updateTask;
    private List<String> lastBody;
    private SidebarBoard(String title, Scoreboard scoreboard, Plugin plugin) {
        this.plugin = plugin;
        this.scoreboard = scoreboard;
        this.objective = scoreboard.registerNewObjective("board", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(title);
    }

    public void setTitle(String title) {
        this.objective.setDisplayName(title);
    }

    public String getTitle() {
        return this.objective.getDisplayName();
    }

    @SuppressWarnings("unchecked")
    public void update(List<String> body) {
        if (this.lastBody != null)
            this.lastBody.forEach(scoreboard::resetScores);
        if (body == null)
            return;
        Map<Integer, String> lines = new HashMap<>();
        for (int i = 0;i<body.size();i++)
            lines.put(body.size() - i - 1, body.get(i));
        Map.Entry<Integer, String>[] entries = lines.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(entries, Comparator.comparingInt(Map.Entry::getKey));
        for (Map.Entry<Integer, String> line : entries) {
            objective.getScore(line.getValue()).setScore(line.getKey());
        }
        this.lastBody = body;
    }

    public void update(BoardUpdater boardUpdater, int interval) {
        this.boardUpdater = boardUpdater;
        this.update(interval);
    }

    public void update(int interval) {
        if (this.updateTask != null)
            this.updateTask.cancel();
        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                SidebarBoard.this.update(SidebarBoard.this.boardUpdater.body());
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    public static SidebarBoard of(String title, Scoreboard scoreboard, Plugin plugin) {
        return new SidebarBoard(title, scoreboard, plugin);
    }

    public static SidebarBoard of(String title, Plugin plugin) {
        return SidebarBoard.of(title, Bukkit.getScoreboardManager().getNewScoreboard(), plugin);
    }

    public static SidebarBoard of(String title, Player player, Plugin plugin) {
        return SidebarBoard.of(title, player.getScoreboard(), plugin);
    }
}