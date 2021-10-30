package io.github.faintcloudy.nyasquidgame.utils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static SimpleCommandMap commandMap;
    static {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void registerTreeCommand(Object o) {
        Class<?> commandClass = o.getClass();
        if (!commandClass.isAnnotationPresent(TreeCommand.class)) {
            throw new IllegalArgumentException("The class " + commandClass.toString() + " isn't annotated by annotation TreeCommand!");
        }

        TreeCommand command = commandClass.getAnnotation(TreeCommand.class);
        Map<BranchedCommand, Method> branchedCommands = new HashMap<>();

        for (Method method : commandClass.getMethods()) {
            if (method.isAnnotationPresent(BranchedCommand.class)) {
                branchedCommands.put(method.getAnnotation(BranchedCommand.class), method);
            }
        }

        commandMap.register(command.name(), new Command(command.name()) {
            @Override
            public boolean execute(CommandSender sender, String s, String[] args) {
                if (!command.target().isTarget(sender)) {
                    sender.sendMessage("§cYou are not a " + command.target().getDisplay());
                    return false;
                }

                for (String permission :command.permissions()) {
                    if (!sender.hasPermission(permission)) {
                        sender.sendMessage("§cYou don't have enough permissions to do that!");
                        return false;
                    }
                }

                if (args.length < 1) {
                    sender.sendMessage(command.help());
                    return false;
                }

                for (Map.Entry<BranchedCommand, Method> branchedCommand : branchedCommands.entrySet()) {
                    BranchedCommand info = branchedCommand.getKey();
                    Method method = branchedCommand.getValue();
                    if (!args[0].equalsIgnoreCase(info.name())) {
                        continue;
                    }

                    for (String permission : info.permissions()) {
                        if (!sender.hasPermission(permission)) {
                            sender.sendMessage("§cYou don't have enough permissions to do that!");
                            return false;
                        }
                    }

                    if (!info.target().isTarget(sender)) {
                        sender.sendMessage("§cYou are not a " + command.target().getDisplay());
                        return false;
                    }

                    try {
                        if (method.getParameterCount() == 0) {
                            method.invoke(o);
                        } else if (method.getParameterCount() == 1) {
                            method.invoke(o, sender);
                        } else if (method.getParameterCount() == 2) {
                            method.invoke(o, sender, args);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });
    }
}
