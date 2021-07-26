package com.husker.minecraft.launcher.app.animation

import javafx.animation.Transition
import javafx.scene.Node
import javafx.scene.transform.Translate
import javafx.util.Duration
import kotlin.math.PI
import kotlin.math.sin

class WaveAnimation {

    companion object{

        @JvmStatic
        fun apply(node: Node, distance: Int = 10, duration: Int = 5000){
            val transform = Translate()
            node.transforms.add(transform)
            val transition = object : Transition(){
                init{
                    cycleDuration = Duration.millis(duration.toDouble())
                    setOnFinished {
                        play()
                    }
                }
                override fun interpolate(frac: Double) {
                    val value = sin(frac * PI) * distance - distance/2
                    transform.y = value
                }
            }
            transition.play()
        }
    }
}