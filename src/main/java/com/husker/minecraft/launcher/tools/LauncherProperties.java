package com.husker.minecraft.launcher.tools;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class LauncherProperties {

    private static final Properties defaultProperties = createLocal("/launcher.properties");

    public static Properties get(){
        return defaultProperties;
    }

    public static String get(String key){
        return get().getProperty(key);
    }

    public static Properties createLocal(String path){
        Properties properties = new Properties();
        try {
            properties.load(LauncherProperties.class.getResourceAsStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Properties create(String path){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
