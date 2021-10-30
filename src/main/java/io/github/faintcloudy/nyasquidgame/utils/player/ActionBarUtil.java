package io.github.faintcloudy.nyasquidgame.utils.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class ActionBarUtil {
    private static Class<?> chatPacketClass;
    private static Class<?> chatSerializerClass;

    static {
        try {
            chatPacketClass = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            chatSerializerClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sendActionBar(Player player, String message)
    {
        if (!player.isOnline())
            return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            Object chatComponent = chatSerializerClass.getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + message + "\"}");;
            Object packet = chatPacketClass.getConstructor(chatSerializerClass, byte.class)
                    .newInstance(chatComponent, (byte) 2);
            connection.getClass().getMethod("sendPacket", chatPacketClass).invoke(connection, packet);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                NoSuchFieldException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
