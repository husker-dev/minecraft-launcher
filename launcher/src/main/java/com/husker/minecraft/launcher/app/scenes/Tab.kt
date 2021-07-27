package com.husker.minecraft.launcher.app.scenes

import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.tools.fx.AnchorUtils
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import java.util.concurrent.atomic.AtomicInteger

abstract class Tab(var title: String, var tabNode : Node = Pane()) {

    var bottom = false
    var color: Color = Color.color(102.0 / 255, 102.0 / 255, 102.0 / 255)

    var content: Pane = Pane()
    val scene: BaseScene
        get() {
            return BaseScene.instance
        }

    var titleVisible : Boolean
        get() {
            if(scene.selectedTab == this)
                return scene.titleVisible
            return false
        }
        set(value) {
            if(scene.selectedTab == this)
                scene.titleVisible = value
        }

    var onShowListeners = arrayListOf<Runnable>()
    var onHideListeners = arrayListOf<Runnable>()
    var currentAnimations = AtomicInteger()

    fun applyFX(name : String){
        try {
            content = Resources.fxml("$name/content.fxml")
        }catch (e : Exception){}
        try {
            BaseScene.instance.stylesheets.add(Resources.style("$name/content.css"))
        }catch (e : Exception){}
    }

    fun findById(selector : String) : Node {
        return content.lookup("#$selector")
    }

}