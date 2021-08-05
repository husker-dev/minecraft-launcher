package com.husker.minecraft.launcher.app

import com.husker.minecraft.launcher.app.scenes.BaseScene
import com.husker.minecraft.launcher.app.scenes.Tab
import com.husker.minecraft.launcher.tools.LauncherProperties
import com.husker.minecraft.launcher.tools.fx.LauncherTimer
import javafx.application.Application
import javafx.stage.Stage
import javafx.stage.WindowEvent
import java.lang.management.ManagementFactory


class Launcher : Application() {

    companion object {
        @JvmStatic lateinit var stage : Stage

        @JvmStatic var tab : Tab
            set(value) {
                BaseScene.instance.selectedTab = value
            }
            get() {
                return BaseScene.instance.selectedTab
            }

        @JvmStatic
        var shutdownListeners = arrayListOf<Runnable>()

        @JvmStatic fun onClose(){

        }
    }

    override fun start(stage: Stage?) {
        if(stage == null)
            throw Exception("Bad JavaFX initializer")
        Resources.initializeFonts()
        Launcher.stage = stage

        stage.icons.add(Resources.image(LauncherProperties.get("icon")))
        stage.title = LauncherProperties.get("title")
        stage.width = 1180.0
        stage.height = 700.0

        stage.scene = BaseScene()

        stage.scene.window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST){
            shutdownListeners.forEach { it.run() }
            Thread{ onClose()}.start()
        }

        if(ManagementFactory.getGarbageCollectorMXBeans()[0].name.contains("Shenandoah")) {
            LauncherTimer.create(1000) { System.gc() }
        }

        stage.show()
    }


}