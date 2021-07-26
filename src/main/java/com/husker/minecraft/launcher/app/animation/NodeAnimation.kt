package com.husker.minecraft.launcher.app.animation

import com.husker.minecraft.launcher.app.Launcher
import com.husker.minecraft.launcher.app.animation.easing.*
import com.husker.minecraft.launcher.app.scenes.Tab
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.scene.Node
import javafx.util.Duration
import java.util.function.Consumer

class NodeAnimation {

    enum class Type {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER,
        RISE
    }


    companion object {
        @JvmStatic fun showNode(node : Node, type : Type, easing : Easing = Exponential.In(), delay : Int = 0, length : Int = 80, duration : Int = 1000) : AnimationTransition {
            AnimationTransition(false, Exponential.In(), delay, duration) { opacityAnim(node, it) }
            return AnimationTransition(false, easing, delay, duration) {
                movingAnim(node, type, it, length)
            }
        }
        @JvmStatic fun hideNode(node : Node, type : Type, easing : Easing = Exponential.Out(), delay : Int = 0, length : Int = 80, duration : Int = 1000) : AnimationTransition {
            AnimationTransition(true, Quintic.Out(), delay, duration) { opacityAnim(node, it) }
            return AnimationTransition(true, easing, delay, duration) {
                movingAnim(node, type, it, length)
            }
        }

        @JvmStatic fun bindTab(tab : Tab, node : Node, type : Type, easing : Easing = Elastic.In(), delay : Int = 0, length : Int = 80, duration : Int = 1000){
            try {
                if (tab != Launcher.tab)
                    node.opacity = 0.0
                else
                    node.opacity = 1.0
            }catch (e : Exception){
                node.opacity = 0.0
            }

            tab.onShowListeners.add{
                tab.currentAnimations.addAndGet(1)
                Thread {
                    showNode(node, type, easing, delay, length, duration).waitForEnd()
                    tab.currentAnimations.decrementAndGet()
                }.start()
            }
            tab.onHideListeners.add{
                tab.currentAnimations.addAndGet(1)
                Thread {
                    hideNode(node, type, EasingTransition.getReverseAnimation(easing), length, duration).waitForEnd()
                    tab.currentAnimations.decrementAndGet()
                }.start()
            }
        }

        private fun movingAnim(node : Node, type : Type, frac : Double, length : Int){
            if(type == Type.RIGHT)
                node.translateX = length * frac
            if(type == Type.LEFT)
                node.translateX = -length * frac
            if(type == Type.TOP)
                node.translateY = -length * frac
            if(type == Type.BOTTOM)
                node.translateY = length * frac
            if(type == Type.CENTER){
                node.scaleX = 1 - frac
                node.scaleY = 1 - frac
            }
        }

        private fun opacityAnim(node : Node, frac : Double){
            node.opacity = (1 - frac)
        }
    }

    class AnimationTransition(isHiding : Boolean, easing : Easing, delay : Int, duration : Int, var action : Consumer<Double>) : EasingTransition(){

        init {
            cycleDuration = Duration.millis(duration.toDouble())
            interpolator = Interpolator.LINEAR
            easingType = easing
            if(isHiding) {
                rate = 1.0
                interpolate(0.0)
                Thread{
                    Thread.sleep(delay.toLong())
                    play()
                }.start()
            } else {
                rate = -1.0
                interpolate(1.0)
                Thread{
                    Thread.sleep(delay.toLong())
                    playFrom(cycleDuration)
                }.start()
            }
        }

        override fun animate(frac: Double) {
            action.accept(frac)
        }
    }

}