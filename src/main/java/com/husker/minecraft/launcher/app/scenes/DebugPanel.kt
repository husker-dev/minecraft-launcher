package com.husker.minecraft.launcher.app.scenes

import com.husker.minecraft.launcher.tools.fx.FPSUtils
import com.husker.minecraft.launcher.tools.fx.LauncherTimer
import com.sun.prism.GraphicsPipeline
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import java.lang.management.ManagementFactory
import java.util.function.Consumer

class DebugPanel(): VBox() {

    init {
        AnchorPane.setRightAnchor(this, 10.0)
        AnchorPane.setBottomAnchor(this, 10.0)

        addInfo{ label ->
            LauncherTimer.create(500){
                Platform.runLater {
                    label.text = "RAM: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024} Mb"
                }
            }
        }
        addInfo{ label ->
            LauncherTimer.create(100){
                Platform.runLater {
                    label.text = "FPS: ${FPSUtils.getAverageFPS().toInt()} / ${FPSUtils.getInstantFPS().toInt()}"
                }
            }
        }
        addInfoSync{ label ->
            val name = when(val id = GraphicsPipeline.getPipeline().javaClass.canonicalName.split(".")[3]){
                "d3d" -> "Direct3D"
                "es2" -> "OpenGL"
                "sw" -> "Software"
                "j2d" -> "Java2D"
                else -> id
            }

            label.text = "Render: $name"
        }

        addInfoText("OS: ${System.getProperty("os.name")}")
        addInfoText("Arch: ${System.getProperty("os.arch")}")
        addInfoText("JVM: ${System.getProperty("java.vendor.version")}")

        ManagementFactory.getGarbageCollectorMXBeans().forEach {
            println(it.name)
        }

        addInfoSync{ label ->
            val fullName = ManagementFactory.getGarbageCollectorMXBeans()[0].name
            val name = when(fullName){
                "Copy" -> "Serial"
                "PS MarkSweep" -> "Parallel"
                "G1 Young Generation" -> "G1"
                "ZGC" -> "ZGC"
                "Shenandoah Pauses" -> "Shenandoah"
                else -> fullName.lowercase().replace("young generation", "").replace("pauses", "").trim().replaceFirstChar { it.uppercase() }
            }
            label.text = "GC: ${ManagementFactory.getGarbageCollectorMXBeans()[0].name.lowercase()
                                .replace("young generation", "")
                                .replace("pauses", "")
                                .trim().replaceFirstChar { it.uppercase() }}"
        }
    }

    fun addInfo(init: Consumer<Label>){
        children.add(object: Label() {
            init { init.accept(this) }
        })
    }

    fun addInfoSync(init: Consumer<Label>){
        addInfo{
            Platform.runLater { init.accept(it) }
        }
    }

    fun addInfoText(text: String){
        addInfoSync{
            it.text = text
        }
    }
}