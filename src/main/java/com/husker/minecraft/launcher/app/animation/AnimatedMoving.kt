package com.husker.minecraft.launcher.app.animation

import com.husker.minecraft.launcher.app.animation.easing.Back
import com.husker.minecraft.launcher.app.animation.easing.Easing
import javafx.beans.property.DoubleProperty
import javafx.beans.property.DoublePropertyBase
import javafx.scene.Node
import javafx.util.Duration

class AnimatedMoving {

    companion object {

        @JvmStatic
        fun setAnimatedMoving(node : Node, easing : Easing = Back.Out(), duration : Int = 400){
            bindAnimatedField(node, node.layoutXProperty(), "layoutX", easing, duration)
            bindAnimatedField(node, node.layoutYProperty(), "layoutY", easing, duration)
        }

        private fun bindAnimatedField(node : Node, oldProperty : DoubleProperty, field : String, easing : Easing, duration : Int){
            val invalidate = DoublePropertyBase::class.java.getDeclaredMethod("invalidated")
            val layout = Node::class.java.getDeclaredField(field)
            val set = DoublePropertyBase::class.java.getDeclaredMethod("set", Double::class.java)
            invalidate.isAccessible = true
            layout.isAccessible = true
            set.isAccessible = true

            var currentValue = 0.0
            var toValue = 0.0
            var tmpValue = 0.0

            // Easing animation, 'tmpValue' used as real property value
            val animation = object : EasingTransition(){
                override fun animate(frac: Double) {
                    tmpValue = currentValue + ((toValue - currentValue) * frac)
                    set.invoke(layout[node], tmpValue)
                }
            }
            animation.easingType = easing
            animation.duration = Duration.millis(duration.toDouble())

            // Override old property object. Used to disable 'set' action except 'tmpValue'
            layout[node] = object : DoublePropertyBase(0.0){
                override fun set(number : Double){
                    if(number == tmpValue){
                        super.set(number)
                        oldProperty.set(number)
                    } else if(toValue != number){
                        toValue = number
                        currentValue = get()
                        animation.playFromStart()
                    }
                }

                override fun invalidated() {
                    invalidate.invoke(oldProperty)
                }
                override fun getBean(): Any = node
                override fun getName(): String = field
            }
        }
    }
}