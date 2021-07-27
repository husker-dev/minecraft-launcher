package com.husker.minecraft.launcher.tools.fx

import com.husker.minecraft.launcher.app.Launcher
import java.util.*
import java.util.function.Consumer
import java.util.Timer

class LauncherTimer {

    companion object{

        fun create(period: Long, action: Consumer<Timer>): Timer{
            return create(0, period, action)
        }

        fun create(delay: Long, period: Long, action: Consumer<Timer>): Timer{
            val instance = Timer()
            instance.schedule(object: TimerTask(){
                override fun run() {
                    action.accept(instance)
                }
            }, delay, period)

            Launcher.shutdownListeners.add{
                instance.cancel()
            }
            return instance
        }
    }

}