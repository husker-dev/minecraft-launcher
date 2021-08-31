package com.husker.minecraft.launcher.app.opengl

import com.husker.openglfx.FXGLEventListener
import com.husker.openglfx.OpenGLCanvas
import com.jogamp.opengl.GL
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane

abstract class GLPane(capabilities: GLCapabilities, fps: Int) : StackPane(), FXGLEventListener {

    private val glCanvas = OpenGLCanvas.create(capabilities, fps)

    init {
        children.add(glCanvas)
        glCanvas.addFXGLEventListener(Event())
    }

    inner class Event: FXGLEventListener {
        override fun init(gl: GL) {
            this@GLPane.init(gl)
        }

        override fun dispose(gl: GL) {
            this@GLPane.dispose(gl)
        }

        override fun display(gl: GL) {
            for(i in 0..2)
                GLTexture.nextLoading()
            this@GLPane.display(gl)
        }

        override fun reshape(gl: GL, width: Float, height: Float) {
            this@GLPane.reshape(gl, width, height)
        }
    }
}