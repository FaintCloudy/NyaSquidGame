package io.github.faintcloudy.nyasquidgame.utils;

import org.bukkit.Location;

import java.util.Random;

public class LocationUtil {
    private static final Random random = new Random();
    public static Location randomPositionInCube(Location vertex1, Location vertex2) {
        double x, y, z;
        x = LocationUtil.getRandomBetween(vertex1.getX(), vertex2.getY());
        y = LocationUtil.getRandomBetween(vertex1.getY(), vertex2.getY());
        z = LocationUtil.getRandomBetween(vertex1.getZ(), vertex2.getZ());
        return new Location(vertex1.getWorld(), x, y, z);
    }

    private static double getRandomBetween(double d1, double d2) {
        return d1 > d2 ? d1 - d2 * random.nextDouble() : d2 - d1 * random.nextDouble();
    }
}
