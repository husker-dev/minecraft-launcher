package com.husker.minecraft.launcher.app.animation

import com.husker.minecraft.launcher.app.animation.easing.*
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.application.Platform
import javafx.util.Duration

abstract class EasingTransition : Transition() {

    companion object {
        @JvmStatic fun getReverseAnimation(easing : Easing) : Easing{
            if(easing is Bounce.In)
                return Bounce.Out()
            if(easing is Bounce.Out)
                return Bounce.In()
            if(easing is Back.In)
                return Back.Out()
            if(easing is Back.Out)
                return Back.In()
            if(easing is Elastic.Out)
                return Elastic.In()
            if(easing is Elastic.In)
                return Elastic.Out()
            return Linear()
        }
    }

    var finished = false
    var easingType : Easing = Elastic.In()
    var duration : Duration
        set(value) {
            cycleDuration = value
        }
        get() = cycleDuration

    init{
        interpolator = Interpolator.LINEAR
        cycleDuration = Duration(1000.0)
        rate = 1.0
    }

    override fun play() {
        Platform.runLater {
            super.play()
        }
        setOnFinished { finished = true}
    }

    fun waitForEnd(){
        while(!finished)
            Thread.sleep(2)
    }

    override fun interpolate(frac : Double) {
        try{
            animate(easingType.calculate(0.0, 1.0, frac, 1.0))
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    abstract fun animate(frac : Double)
}