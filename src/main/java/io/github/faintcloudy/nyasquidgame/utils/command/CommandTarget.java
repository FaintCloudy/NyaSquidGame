package io.github.faintcloudy.nyasquidgame.utils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum CommandTarget {
    PLAYER("player") {
        @Override
        public boolean isTarget(CommandSender sender) {
            return sender instanceof Player;
        }
    }, CONSOLE("console") {
        @Override
        public boolean isTarget(CommandSender sender) {
            return sender == Bukkit.getConsoleSender();
        }
    }, ALL("error") {
        @Override
        public boolean isTarget(CommandSender sender) {
            return true;
        }
    };
    private final String display;
    CommandTarget(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public abstract boolean isTarget(CommandSender sender);
}
