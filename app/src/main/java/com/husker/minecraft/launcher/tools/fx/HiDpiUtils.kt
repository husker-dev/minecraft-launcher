package com.husker.minecraft.launcher.tools.fx

import com.husker.minecraft.launcher.app.Launcher
import javafx.beans.value.ChangeListener
import javafx.stage.Screen
import java.util.function.Consumer


class HiDpiUtils {

    companion object{

        @JvmStatic
        fun addDpiListener(listener: Consumer<Double>){
            var oldDpi = getDpi()
            val action = ChangeListener<Number> {_, _, _ ->
                val currentDpi = getDpi()
                if(oldDpi != currentDpi)
                    listener.accept(currentDpi)
                oldDpi = currentDpi
            }
            Launcher.stage.xProperty().addListener(action)
            Launcher.stage.yProperty().addListener(action)
            Launcher.stage.widthProperty().addListener(action)
            Launcher.stage.heightProperty().addListener(action)
        }

        @JvmStatic
        fun getDpi() : Double {
            val window = Launcher.stage
            val screens = Screen.getScreensForRectangle(window.x, window.y, window.width / 2, window.height / 2)
            if(screens.size > 0)
                return screens[0].outputScaleX
            return 1.0
        }
    }
}