package com.husker.minecraft.launcher.tools.fx

import javafx.animation.AnimationTimer




class FPSUtils {

    companion object{
        private var lastUpdate: Long = 0
        private var index = 0
        private val frameRates = DoubleArray(100)

        init{
            val frameRateMeter = object: AnimationTimer(){
                override fun handle(now: Long) {
                    if (lastUpdate > 0) {
                        val nanosElapsed = now -lastUpdate
                        val frameRate = 1000000000.0 / nanosElapsed
                        index %= frameRates.size
                        frameRates[index++] = frameRate
                    }

                    lastUpdate = now
                }
            }
            frameRateMeter.start()
        }

        fun getInstantFPS(): Double {
            return frameRates[index % frameRates.size]
        }


        fun getAverageFPS(): Double {
            var total = 0.0
            for (i in frameRates.indices) {
                total += frameRates[i]
            }
            return total / frameRates.size
        }
    }


}