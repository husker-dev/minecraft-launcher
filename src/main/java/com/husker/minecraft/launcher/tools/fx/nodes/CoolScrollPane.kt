package com.husker.minecraft.launcher.tools.fx.nodes

import com.husker.minecraft.launcher.app.animation.easing.Quintic
import com.husker.minecraft.launcher.app.animation.AnimatedMoving
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import java.math.BigDecimal
import java.util.function.Consumer

class CoolScrollPane : Pane() {

    private var _y = BigDecimal.valueOf(0)
    var y : Double
        set(value) {
            _y = value.toBigDecimal()
            updateLayout()
        }
        get() = _y.toDouble()

    lateinit var baseContent : Region
    var scrollListeners = arrayListOf<Consumer<Double>>()
    var animatedContent = HashSet<Region>()

    init {
        widthProperty().addListener { _ -> updateLayout() }
        heightProperty().addListener { _ -> updateLayout() }
        setOnScroll {
            moveScroll(it.deltaY * 4)
        }
    }

    private fun moveScroll(addition : Double){
        val down = addition < 0 && baseContent.height > height && -_y.toDouble() + height < baseContent.height
        val up = addition > 0 //&& _y.toDouble() < 0

        if(down || up){
            _y = _y.add(addition.toBigDecimal())
            scrollListeners.forEach { it.accept(_y.toDouble()) }
            updateLayout()
        }
    }

    private fun updateLayout() {
        for(node in children){
            if(node is Region){
                if(!this::baseContent.isInitialized)
                    baseContent = node
                if(!animatedContent.contains(node)){
                    animatedContent.add(node)
                    AnimatedMoving.setAnimatedMoving(node, Quintic.Out(), 350)
                }

                node.prefWidth = width
                node.layoutY = _y.toDouble()
            }
        }
    }
}