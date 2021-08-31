package com.husker.minecraft.launcher;

import com.husker.minecraft.launcher.app.Launcher;
import javafx.application.Application;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;


public class Main {

    public static void main(String[] args) throws IOException {
        initializeProperties();
        Application.launch(Launcher.class);
    }

    public static void initializeProperties(){
        System.setProperty("prism.order", "es2");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.vsync", "false");
        System.setProperty("javafx.cachedir", System.getProperty("user.home") + "/.openjfx/cache/minecraft-launcher/");
    }

}
