package com.husker.minecraft.launcher;

import com.husker.minecraft.launcher.app.Launcher;
import javafx.application.Application;


public class Main {

    public static void main(String[] args) {
        initializeProperties();
        Application.launch(Launcher.class);
    }

    public static void initializeProperties(){
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.vsync", "false");
    }

}
