package io.github.faintcloudy.nyasquidgame.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtil {

    public static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getCraftBukkitClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getFieldValue(Object o, String name) {
        Object result = null;
        try {
            Field field = o.getClass().getField(name);
            field.setAccessible(true);
            return field.get(name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldValue(Object o, String name, Object value) {
        try {
            Field field = o.getClass().getField(name);
            field.setAccessible(true);
            field.set(o, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object invokeMethod(Object o, String name, Object... args) {
        List<Class<?>> argsClasses = new ArrayList<>();
        for (Object argument : args)
            argsClasses.add(argument.getClass());
        try {
            return o.getClass().getMethod(name, argsClasses.toArray(new Class[0])).invoke(o, args);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeStaticMethod(Class<?> clazz, String name, Object... args) {
        List<Class<?>> argsClasses = new ArrayList<>();
        for (Object argument : args)
            argsClasses.add(argument.getClass());
        try {
            return clazz.getMethod(name, argsClasses.toArray(new Class[0])).invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
