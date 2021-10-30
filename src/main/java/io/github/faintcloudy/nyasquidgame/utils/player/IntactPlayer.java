package io.github.faintcloudy.nyasquidgame.utils.player;

import io.github.faintcloudy.squidgame.SquidGame;
import io.github.faintcloudy.squidgame.utils.scoreboard.SidebarBoard;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;

public class IntactPlayer implements Player {
    @Getter
    private final Player player;
    @Getter
    private final SidebarBoard sidebarBoard;
    private IntactPlayer(Player player) {
        this.player = player;
        this.sidebarBoard = SidebarBoard.of("No Title", this.player, SquidGame.getInstance());
    }

    public String getDisplayNameFromLuckPerms() {
        String defaultResult = "§7" + this.getName();
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                LuckPerms luckPerms = LuckPermsProvider.get();
                User user = luckPerms.getUserManager().getUser(this.getUniqueId());
                if (user == null)
                    return defaultResult;
                CachedMetaData metaData = user.getCachedData().getMetaData();
                return metaData.getPrefix() + " " + this.getName() + " " + metaData.getSuffix();
            } catch (NoClassDefFoundError e) {
                e.addSuppressed(new UnsupportedClassVersionError("The version of LuckPerms is unsupported!"));
                e.printStackTrace();
            }
        }

        return defaultResult;
    }

    public void sendActionBar(String actionBarText) {
        if (!this.player.isOnline())
            return;
        try {
            Object handle = this.player.getClass().getMethod("getHandle").invoke(this.player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            Object chatComponent = chatSerializerClass.getMethod("a", String.class)
                    .invoke(null, "{\"text\": \"" + actionBarText + "\"}");
            Object packet = chatPacketClass.getConstructor(chatSerializerClass, byte.class)
                    .newInstance(chatComponent, (byte) 2);
            connection.getClass().getMethod("sendPacket", chatPacketClass).invoke(connection, packet);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                NoSuchFieldException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void sendTitle(Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(fadeIn, stay, fadeOut, message, null);
    }

    public void sendSubtitle(Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(fadeIn, stay, fadeOut, null, message);
    }

    public void sendFullTitle(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        sendTitle(fadeIn, stay, fadeOut, title, subtitle);
    }

    public void sendPacket(Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"))
                    .invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void sendTitle(Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        try {
            if (title != null) {
                title = ChatColor.translateAlternateColorCodes('&', title);
                title = title.replaceAll("%player%", player.getDisplayName());
                Object e = ((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle")))
                        .getDeclaredClasses()[0].getField("TIMES").get(null);
                Object chatTitle = ((Class<?>) Objects.requireNonNull((T) getNMSClass("IChatBaseComponent")))
                        .getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                Constructor<?> subtitleConstructor = Objects.<Class<?>>requireNonNull(getNMSClass("PacketPlayOutTitle"))
                        .getConstructor(((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle"))).getDeclaredClasses()[0],
                        getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
                Object titlePacket = subtitleConstructor.newInstance(e, chatTitle, fadeIn, stay, fadeOut);
                sendPacket(titlePacket);
                e = ((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle")))
                        .getDeclaredClasses()[0].getField("TITLE").get(null);
                chatTitle = ((Class<?>) Objects.requireNonNull((T) getNMSClass("IChatBaseComponent")))
                        .getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                subtitleConstructor = Objects.<Class<?>>requireNonNull(getNMSClass("PacketPlayOutTitle"))
                        .getConstructor(((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle"))).getDeclaredClasses()[0],
                        getNMSClass("IChatBaseComponent"));
                titlePacket = subtitleConstructor.newInstance(e, chatTitle);
                sendPacket(titlePacket);
            }
            if (subtitle != null) {
                subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
                subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
                Object e = ((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle")))
                        .getDeclaredClasses()[0].getField("TIMES").get(null);
                Object chatSubtitle = ((Class<?>) Objects.requireNonNull((T) getNMSClass("IChatBaseComponent")))
                        .getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + title + "\"}");
                Constructor<?> subtitleConstructor = Objects.<Class<?>>requireNonNull(getNMSClass("PacketPlayOutTitle"))
                        .getConstructor(((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle"))).getDeclaredClasses()[0],
                        getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
                Object subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
                sendPacket(subtitlePacket);
                e = ((Class<?>) Objects.requireNonNull((T) getNMSClass("PacketPlayOutTitle"))).getDeclaredClasses()[0]
                        .getField("SUBTITLE").get(null);
                chatSubtitle = ((Class<?>) Objects.requireNonNull((T) getNMSClass("IChatBaseComponent")))
                        .getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle + "\"}");
                subtitleConstructor = Objects.<Class<?>>requireNonNull(getNMSClass("PacketPlayOutTitle")).getConstructor(((Class<?>) Objects
                                .requireNonNull((T) getNMSClass("PacketPlayOutTitle"))).getDeclaredClasses()[0],
                        getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
                subtitlePacket = subtitleConstructor.newInstance(e, chatSubtitle, fadeIn, stay, fadeOut);
                sendPacket(subtitlePacket);
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    public void clearTitle() {
        sendTitle(0, 0, 0, "", "");
    }

    @SuppressWarnings("unchecked")
    public <T> void sendTabTitle(String header, String footer) {
        if (header == null) {
            header = "";
        }
        header = ChatColor.translateAlternateColorCodes('&', header);
        if (footer == null) {
            footer = "";
        }
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        header = header.replaceAll("%player%", player.getDisplayName());
        footer = footer.replaceAll("%player%", player.getDisplayName());

        try {
            Object tabHeader = ((Class<?>) Objects.requireNonNull((T) getNMSClass("IChatBaseComponent")))
                    .getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = ((Class<?>) Objects.requireNonNull((T) getNMSClass("IChatBaseComponent")))
                    .getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + footer + "\"}");
            Constructor<?> titleConstructor = Objects.<Class<?>>requireNonNull(getNMSClass("PacketPlayOutPlayerListHeaderFooter"))
                    .getConstructor();
            Object packet = titleConstructor.newInstance();
            Field aField = packet.getClass().getDeclaredField("a");
            aField.setAccessible(true);
            aField.set(packet, tabHeader);
            Field bField = packet.getClass().getDeclaredField("b");
            bField.setAccessible(true);
            bField.set(packet, tabFooter);
            sendPacket(packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final Class<?> chatPacketClass;
    private static final Class<?> chatSerializerClass;
    @Getter
    private static final List<IntactPlayer> intactPlayerList = new ArrayList<>();
    public static IntactPlayer of(Player player) {
        if (player instanceof IntactPlayer)
            return (IntactPlayer) player;
        for (IntactPlayer intactPlayer : intactPlayerList) {
            if (intactPlayer.getPlayer() == player)
                return intactPlayer;
        }
        IntactPlayer intactPlayer = new IntactPlayer(player);
        intactPlayerList.add(intactPlayer);
        return intactPlayer;
    }

    static {
        chatPacketClass = getNMSClass("PacketPlayOutChat");
        chatSerializerClass = getNMSClass("IChatBaseComponent.ChatSerializer");
    }

    @Override
    public String getDisplayName() { return player.getDisplayName(); }

    @Override
    public void setDisplayName(String s) { player.setDisplayName(s); }

    @Override
    public String getPlayerListName() { return player.getPlayerListName(); }

    @Override
    public void setPlayerListName(String s) { player.setPlayerListName(s); }

    @Override
    public void setCompassTarget(Location location) { player.setCompassTarget(location); }

    @Override
    public Location getCompassTarget() { return player.getCompassTarget(); }

    @Override
    public InetSocketAddress getAddress() { return player.getAddress(); }

    @Override
    public boolean isConversing() { return player.isConversing(); }

    @Override
    public void acceptConversationInput(String s) { player.acceptConversationInput(s); }

    @Override
    public boolean beginConversation(Conversation conversation) { return player.beginConversation(conversation); }

    @Override
    public void abandonConversation(Conversation conversation) { player.abandonConversation(conversation); }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) { player.abandonConversation(conversation, conversationAbandonedEvent); }

    @Override
    public void sendRawMessage(String s) { player.sendRawMessage(s); }

    @Override
    public void kickPlayer(String s) { player.kickPlayer(s); }

    @Override
    public void chat(String s) { player.chat(s); }

    @Override
    public boolean performCommand(String s) { return player.performCommand(s); }

    @Override
    public boolean isSneaking() { return player.isSneaking(); }

    @Override
    public void setSneaking(boolean b) { player.setSneaking(b); }

    @Override
    public boolean isSprinting() { return player.isSprinting(); }

    @Override
    public void setSprinting(boolean b) { player.setSprinting(b); }

    @Override
    public void saveData() { player.saveData(); }

    @Override
    public void loadData() { player.loadData(); }

    @Override
    public void setSleepingIgnored(boolean b) { player.setSleepingIgnored(b); }

    @Override
    public boolean isSleepingIgnored() { return player.isSleepingIgnored(); }

    @Override
    public void playNote(Location location, byte b, byte b1) { player.playNote(location, b, b1); }

    @Override
    public void playNote(Location location, Instrument instrument, Note note) { player.playNote(location, instrument, note); }

    @Override
    public void playSound(Location location, Sound sound, float v, float v1) { player.playSound(location, sound, v, v1); }

    @Override
    public void playSound(Location location, String s, float v, float v1) { player.playSound(location, s, v, v1); }

    @Override
    public void playEffect(Location location, Effect effect, int i) { player.playEffect(location, effect, i); }

    @Override
    public <T> void playEffect(Location location, Effect effect, T t) {
        player.playEffect(location, effect, t);
    }

    @Override
    public void sendBlockChange(Location location, Material material, byte b) { player.sendBlockChange(location, material, b); }

    @Override
    public boolean sendChunkChange(Location location, int i, int i1, int i2, byte[] bytes) { return player.sendChunkChange(location, i, i1, i2, bytes); }

    @Override
    public void sendBlockChange(Location location, int i, byte b) { player.sendBlockChange(location, i, b); }

    @Override
    public void sendSignChange(Location location, String[] strings) { player.sendSignChange(location, strings); }

    @Override
    public void sendMap(MapView mapView) { player.sendMap(mapView); }

    @Override
    public void updateInventory() { player.updateInventory(); }

    @Override
    public void awardAchievement(Achievement achievement) { player.awardAchievement(achievement); }

    @Override
    public void removeAchievement(Achievement achievement) { player.removeAchievement(achievement); }

    @Override
    public boolean hasAchievement(Achievement achievement) { return player.hasAchievement(achievement); }

    @Override
    public void incrementStatistic(Statistic statistic) { player.incrementStatistic(statistic); }

    @Override
    public void decrementStatistic(Statistic statistic) { player.decrementStatistic(statistic); }

    @Override
    public void incrementStatistic(Statistic statistic, int i) { player.incrementStatistic(statistic, i); }

    @Override
    public void decrementStatistic(Statistic statistic, int i) { player.decrementStatistic(statistic, i); }

    @Override
    public void setStatistic(Statistic statistic, int i) { player.setStatistic(statistic, i); }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        return player.getStatistic(statistic);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) { player.incrementStatistic(statistic, material); }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) { player.decrementStatistic(statistic, material); }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        return player.getStatistic(statistic, material);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int i) { player.incrementStatistic(statistic, material, i); }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int i) { player.decrementStatistic(statistic, material, i); }

    @Override
    public void setStatistic(Statistic statistic, Material material, int i) { player.setStatistic(statistic, material, i); }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) { player.incrementStatistic(statistic, entityType); }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) { player.decrementStatistic(statistic, entityType); }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        return player.getStatistic(statistic, entityType);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int i) throws IllegalArgumentException {
        player.incrementStatistic(statistic, entityType, i);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int i) { player.decrementStatistic(statistic, entityType, i); }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int i) { player.setStatistic(statistic, entityType, i); }

    @Override
    public void setPlayerTime(long l, boolean b) { player.setPlayerTime(l, b); }

    @Override
    public long getPlayerTime() { return player.getPlayerTime(); }

    @Override
    public long getPlayerTimeOffset() { return player.getPlayerTimeOffset(); }

    @Override
    public boolean isPlayerTimeRelative() { return player.isPlayerTimeRelative(); }

    @Override
    public void resetPlayerTime() { player.resetPlayerTime(); }

    @Override
    public void setPlayerWeather(WeatherType weatherType) { player.setPlayerWeather(weatherType); }

    @Override
    public WeatherType getPlayerWeather() { return player.getPlayerWeather(); }

    @Override
    public void resetPlayerWeather() { player.resetPlayerWeather(); }

    @Override
    public void giveExp(int i) { player.giveExp(i); }

    @Override
    public void giveExpLevels(int i) { player.giveExpLevels(i); }

    @Override
    public float getExp() { return player.getExp(); }

    @Override
    public void setExp(float v) { player.setExp(v); }

    @Override
    public int getLevel() { return player.getLevel(); }

    @Override
    public void setLevel(int i) { player.setLevel(i); }

    @Override
    public int getTotalExperience() { return player.getTotalExperience(); }

    @Override
    public void setTotalExperience(int i) { player.setTotalExperience(i); }

    @Override
    public float getExhaustion() { return player.getExhaustion(); }

    @Override
    public void setExhaustion(float v) { player.setExhaustion(v); }

    @Override
    public float getSaturation() { return player.getSaturation(); }

    @Override
    public void setSaturation(float v) { player.setSaturation(v); }

    @Override
    public int getFoodLevel() { return player.getFoodLevel(); }

    @Override
    public void setFoodLevel(int i) { player.setFoodLevel(i); }

    @Override
    public boolean isOnline() { return player.isOnline(); }

    @Override
    public boolean isBanned() { return player.isBanned(); }

    @Override
    public void setBanned(boolean b) { player.setBanned(b); }

    @Override
    public boolean isWhitelisted() { return player.isWhitelisted(); }

    @Override
    public void setWhitelisted(boolean b) { player.setWhitelisted(b); }

    @Override
    public long getFirstPlayed() { return player.getFirstPlayed(); }

    @Override
    public long getLastPlayed() { return player.getLastPlayed(); }

    @Override
    public boolean hasPlayedBefore() { return player.hasPlayedBefore(); }

    @Override
    public Location getBedSpawnLocation() { return player.getBedSpawnLocation(); }

    @Override
    public void setBedSpawnLocation(Location location) { player.setBedSpawnLocation(location); }

    @Override
    public void setBedSpawnLocation(Location location, boolean b) { player.setBedSpawnLocation(location, b); }

    @Override
    public boolean getAllowFlight() { return player.getAllowFlight(); }

    @Override
    public void setAllowFlight(boolean b) { player.setAllowFlight(b); }

    @Override
    public void hidePlayer(Player player) { player.hidePlayer(player); }

    @Override
    public void showPlayer(Player player) { player.showPlayer(player); }

    @Override
    public boolean canSee(Player player) { return player.canSee(player); }

    @Override
    public Location getLocation() { return player.getLocation(); }

    @Override
    public Location getLocation(Location location) { return player.getLocation(location); }

    @Override
    public void setVelocity(Vector vector) { player.setVelocity(vector); }

    @Override
    public Vector getVelocity() { return player.getVelocity(); }

    @Override
    public boolean isOnGround() { return player.isOnGround(); }

    @Override
    public World getWorld() { return player.getWorld(); }

    @Override
    public boolean teleport(Location location) { return player.teleport(location); }

    @Override
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause teleportCause) { return player.teleport(location, teleportCause); }

    @Override
    public boolean teleport(Entity entity) { return player.teleport(entity); }

    @Override
    public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause teleportCause) { return player.teleport(entity, teleportCause); }

    @Override
    public List<Entity> getNearbyEntities(double v, double v1, double v2) { return player.getNearbyEntities(v, v1, v2); }

    @Override
    public int getEntityId() { return player.getEntityId(); }

    @Override
    public int getFireTicks() { return player.getFireTicks(); }

    @Override
    public int getMaxFireTicks() { return player.getMaxFireTicks(); }

    @Override
    public void setFireTicks(int i) { player.setFireTicks(i); }

    @Override
    public void remove() { player.remove(); }

    @Override
    public boolean isDead() { return player.isDead(); }

    @Override
    public boolean isValid() { return player.isValid(); }

    @Override
    public void sendMessage(String s) { player.sendMessage(s); }

    @Override
    public void sendMessage(String[] strings) { player.sendMessage(strings); }

    @Override
    public Server getServer() { return player.getServer(); }

    @Override
    public Entity getPassenger() { return player.getPassenger(); }

    @Override
    public boolean setPassenger(Entity entity) { return player.setPassenger(entity); }

    @Override
    public boolean isEmpty() { return player.isEmpty(); }

    @Override
    public boolean eject() { return player.eject(); }

    @Override
    public float getFallDistance() { return player.getFallDistance(); }

    @Override
    public void setFallDistance(float v) { player.setFallDistance(v); }

    @Override
    public void setLastDamageCause(EntityDamageEvent entityDamageEvent) { player.setLastDamageCause(entityDamageEvent); }

    @Override
    public EntityDamageEvent getLastDamageCause() { return player.getLastDamageCause(); }

    @Override
    public UUID getUniqueId() { return player.getUniqueId(); }

    @Override
    public int getTicksLived() { return player.getTicksLived(); }

    @Override
    public void setTicksLived(int i) { player.setTicksLived(i); }

    @Override
    public void playEffect(EntityEffect entityEffect) { player.playEffect(entityEffect); }

    @Override
    public EntityType getType() { return player.getType(); }

    @Override
    public boolean isInsideVehicle() { return player.isInsideVehicle(); }

    @Override
    public boolean leaveVehicle() { return player.leaveVehicle(); }

    @Override
    public Entity getVehicle() { return player.getVehicle(); }

    @Override
    public void setCustomName(String s) { player.setCustomName(s); }

    @Override
    public String getCustomName() { return player.getCustomName(); }

    @Override
    public void setCustomNameVisible(boolean b) { player.setCustomNameVisible(b); }

    @Override
    public boolean isCustomNameVisible() { return player.isCustomNameVisible(); }

    @Override
    public boolean isFlying() { return player.isFlying(); }

    @Override
    public void setFlying(boolean b) { player.setFlying(b); }

    @Override
    public void setFlySpeed(float v) { player.setFlySpeed(v); }


    public void setWalkSpeed(float v) { player.setWalkSpeed(v); }

    @Override
    public float getFlySpeed() { return player.getFlySpeed(); }

    @Override
    public float getWalkSpeed() { return player.getWalkSpeed(); }

    @Override
    public void setTexturePack(String s) { player.setTexturePack(s); }

    @Override
    public void setResourcePack(String s) { player.setResourcePack(s); }

    @Override
    public Scoreboard getScoreboard() { return player.getScoreboard(); }

    @Override
    public void setScoreboard(Scoreboard scoreboard) { player.setScoreboard(scoreboard); }

    @Override
    public boolean isHealthScaled() { return player.isHealthScaled(); }

    @Override
    public void setHealthScaled(boolean b) { player.setHealthScaled(b); }

    @Override
    public void setHealthScale(double v) { player.setHealthScale(v); }

    @Override
    public double getHealthScale() { return player.getHealthScale(); }

    @Override
    public Entity getSpectatorTarget() { return player.getSpectatorTarget(); }

    @Override
    public void setSpectatorTarget(Entity entity) { player.setSpectatorTarget(entity); }

    @Override @Deprecated
    public void sendTitle(String s, String s1) { player.sendTitle(s, s1); }

    @Override
    public void resetTitle() { player.resetTitle(); }

    @Override
    public Map<String, Object> serialize() { return player.serialize(); }

    @Override
    public String getName() { return player.getName(); }

    @Override
    public PlayerInventory getInventory() { return player.getInventory(); }

    @Override
    public Inventory getEnderChest() { return player.getEnderChest(); }

    @Override
    public boolean setWindowProperty(InventoryView.Property property, int i) { return player.setWindowProperty(property, i); }

    @Override
    public InventoryView getOpenInventory() { return player.getOpenInventory(); }

    @Override
    public InventoryView openInventory(Inventory inventory) { return player.openInventory(inventory); }

    @Override
    public InventoryView openWorkbench(Location location, boolean b) { return player.openWorkbench(location, b); }

    @Override
    public InventoryView openEnchanting(Location location, boolean b) { return player.openEnchanting(location, b); }

    @Override
    public void openInventory(InventoryView inventoryView) { player.openInventory(inventoryView); }

    @Override
    public void closeInventory() { player.closeInventory(); }

    @Override
    public ItemStack getItemInHand() { return player.getItemInHand(); }

    @Override
    public void setItemInHand(ItemStack itemStack) { player.setItemInHand(itemStack); }

    @Override
    public ItemStack getItemOnCursor() { return player.getItemOnCursor(); }

    @Override
    public void setItemOnCursor(ItemStack itemStack) { player.setItemOnCursor(itemStack); }

    @Override
    public boolean isSleeping() { return player.isSleeping(); }

    @Override
    public int getSleepTicks() { return player.getSleepTicks(); }

    @Override
    public GameMode getGameMode() { return player.getGameMode(); }

    @Override
    public void setGameMode(GameMode gameMode) { player.setGameMode(gameMode); }

    @Override
    public boolean isBlocking() { return player.isBlocking(); }

    @Override
    public int getExpToLevel() { return player.getExpToLevel(); }

    @Override
    public double getEyeHeight() { return player.getEyeHeight(); }

    @Override
    public double getEyeHeight(boolean b) { return player.getEyeHeight(b); }

    @Override
    public Location getEyeLocation() { return player.getEyeLocation(); }

    @Override
    public List<Block> getLineOfSight(HashSet<Byte> hashSet, int i) { return player.getLineOfSight(hashSet, i); }

    @Override
    public List<Block> getLineOfSight(Set<Material> set, int i) { return player.getLineOfSight(set, i); }

    @Override
    public Block getTargetBlock(HashSet<Byte> hashSet, int i) { return player.getTargetBlock(hashSet, i); }

    @Override
    public Block getTargetBlock(Set<Material> set, int i) { return player.getTargetBlock(set, i); }

    @Override
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hashSet, int i) { return player.getLastTwoTargetBlocks(hashSet, i); }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> set, int i) { return player.getLastTwoTargetBlocks(set, i); }

    @Override
    public Egg throwEgg() { return player.throwEgg(); }

    @Override
    public Snowball throwSnowball() { return player.throwSnowball(); }

    @Override
    public Arrow shootArrow() { return player.shootArrow(); }

    @Override
    public int getRemainingAir() { return player.getRemainingAir(); }

    @Override
    public void setRemainingAir(int i) { player.setRemainingAir(i); }

    @Override
    public int getMaximumAir() { return player.getMaximumAir(); }

    @Override
    public void setMaximumAir(int i) { player.setMaximumAir(i); }

    @Override
    public int getMaximumNoDamageTicks() { return player.getMaximumNoDamageTicks(); }

    @Override
    public void setMaximumNoDamageTicks(int i) { player.setMaximumNoDamageTicks(i); }

    @Override
    public double getLastDamage() { return player.getLastDamage(); }

    @Override
    public int _INVALID_getLastDamage() { return player._INVALID_getLastDamage(); }

    @Override
    public void setLastDamage(double v) { player.setLastDamage(v); }

    @Override
    public void _INVALID_setLastDamage(int i) { player._INVALID_setLastDamage(i); }

    @Override
    public int getNoDamageTicks() { return player.getNoDamageTicks(); }

    @Override
    public void setNoDamageTicks(int i) { player.setNoDamageTicks(i); }

    @Override
    public Player getKiller() { return player.getKiller(); }

    @Override
    public boolean addPotionEffect(PotionEffect potionEffect) { return player.addPotionEffect(potionEffect); }

    @Override
    public boolean addPotionEffect(PotionEffect potionEffect, boolean b) { return player.addPotionEffect(potionEffect, b); }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> collection) { return player.addPotionEffects(collection); }

    @Override
    public boolean hasPotionEffect(PotionEffectType potionEffectType) { return player.hasPotionEffect(potionEffectType); }

    @Override
    public void removePotionEffect(PotionEffectType potionEffectType) { player.removePotionEffect(potionEffectType); }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() { return player.getActivePotionEffects(); }

    @Override
    public boolean hasLineOfSight(Entity entity) { return player.hasLineOfSight(entity); }

    @Override
    public boolean getRemoveWhenFarAway() { return player.getRemoveWhenFarAway(); }

    @Override
    public void setRemoveWhenFarAway(boolean b) { player.setRemoveWhenFarAway(b); }

    @Override
    public EntityEquipment getEquipment() { return player.getEquipment(); }

    @Override
    public void setCanPickupItems(boolean b) { player.setCanPickupItems(b); }

    @Override
    public boolean getCanPickupItems() { return player.getCanPickupItems(); }

    @Override
    public boolean isLeashed() { return player.isLeashed(); }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        return player.getLeashHolder();
    }

    @Override
    public boolean setLeashHolder(Entity entity) { return player.setLeashHolder(entity); }

    @Override
    public void damage(double v) { player.damage(v); }

    @Override
    public void _INVALID_damage(int i) { player._INVALID_damage(i); }

    @Override
    public void damage(double v, Entity entity) { player.damage(v, entity); }

    @Override
    public void _INVALID_damage(int i, Entity entity) { player._INVALID_damage(i, entity); }

    @Override
    public double getHealth() { return player.getHealth(); }

    @Override
    public int _INVALID_getHealth() { return player._INVALID_getHealth(); }

    @Override
    public void setHealth(double v) { player.setHealth(v); }

    @Override
    public void _INVALID_setHealth(int i) { player._INVALID_setHealth(i); }

    @Override
    public double getMaxHealth() { return player.getMaxHealth(); }

    @Override
    public int _INVALID_getMaxHealth() { return player._INVALID_getMaxHealth(); }

    @Override
    public void setMaxHealth(double v) { player.setMaxHealth(v); }

    @Override
    public void _INVALID_setMaxHealth(int i) { player._INVALID_setMaxHealth(i); }

    @Override
    public void resetMaxHealth() {
        player.resetMaxHealth();
    }

    @Override
    public void setMetadata(String s, MetadataValue metadataValue) { player.setMetadata(s, metadataValue); }

    @Override
    public List<MetadataValue> getMetadata(String s) { return player.getMetadata(s); }

    @Override
    public boolean hasMetadata(String s) { return player.hasMetadata(s); }

    @Override
    public void removeMetadata(String s, Plugin plugin) { player.removeMetadata(s, plugin); }

    @Override
    public boolean isPermissionSet(String s) { return player.isPermissionSet(s); }

    @Override
    public boolean isPermissionSet(Permission permission) { return player.isPermissionSet(permission); }

    @Override
    public boolean hasPermission(String s) { return player.hasPermission(s); }

    @Override
    public boolean hasPermission(Permission permission) { return player.hasPermission(permission); }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) { return player.addAttachment(plugin, s, b); }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) { return player.addAttachment(plugin); }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) { return player.addAttachment(plugin, s, b, i); }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) { return player.addAttachment(plugin, i); }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) { player.removeAttachment(permissionAttachment); }

    @Override
    public void recalculatePermissions() { player.recalculatePermissions(); }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() { return player.getEffectivePermissions(); }

    @Override
    public boolean isOp() { return player.isOp(); }

    @Override
    public void setOp(boolean b) { player.setOp(b); }

    @Override
    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) { player.sendPluginMessage(plugin, s, bytes); }

    @Override
    public Set<String> getListeningPluginChannels() { return player.getListeningPluginChannels(); }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) { return player.launchProjectile(aClass); }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector) { return player.launchProjectile(aClass, vector); }
}
