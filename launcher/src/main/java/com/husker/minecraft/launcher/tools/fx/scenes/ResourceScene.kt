package com.husker.minecraft.launcher.tools.fx.scenes

import com.husker.minecraft.launcher.app.Launcher
import com.husker.minecraft.launcher.app.Resources
import javafx.scene.Scene
import javafx.stage.Stage

open class ResourceScene(path : String) : Scene(Resources.fxml(path)) {

    val stage : Stage
        get() = Launcher.stage
}