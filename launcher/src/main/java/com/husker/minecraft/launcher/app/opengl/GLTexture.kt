package com.husker.minecraft.launcher.app.opengl

import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GL2ES2.GL_TEXTURE_WRAP_R
import com.jogamp.opengl.GL2GL3.GL_TEXTURE_LOD_BIAS
import com.jogamp.opengl.GLContext
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureCoords
import com.jogamp.opengl.util.texture.TextureIO
import javafx.scene.paint.Color
import javafx.scene.shape.CullFace
import javafx.scene.transform.Affine
import javafx.scene.transform.Transform
import java.io.InputStream
import java.nio.DoubleBuffer
import java.nio.FloatBuffer


open class GLTexture(var point1: Point3D,
                var point2: Point3D,
                var point3: Point3D,
                var point4: Point3D) {

    companion object{
        @JvmStatic val loadingQueue = arrayListOf<() -> Unit>()
        @JvmStatic fun nextLoading(){
            if(loadingQueue.size > 0)
                loadingQueue.removeAt(0).invoke()
        }

        @JvmStatic fun loadTexture(stream: InputStream, mipmap: Boolean, fileSuffix: String): Texture{
            lateinit var texture: Texture
            var initialized = false
            loadingQueue.add {
                texture = TextureIO.newTexture(stream, mipmap, fileSuffix)

                val gl = GLContext.getCurrentGL()
                texture.setTexParameteri(gl, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
                texture.setTexParameteri(gl, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
                texture.setTexParameteri(gl, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

                initialized = true
            }
            while(!initialized)
                Thread.sleep(1)
            return texture
        }
    }

    private val globalTransform: Affine = Affine()
    private var globalTransformMatrix = DoubleBuffer.allocate(0)
    private var globalTransformModified = true

    private val localTransform: Affine = Affine()
    private var localTransformModified = true
    private var localPoint1 = Point3D.ZERO
    private var localPoint2 = Point3D.ZERO
    private var localPoint3 = Point3D.ZERO
    private var localPoint4 = Point3D.ZERO

    lateinit var texture: Texture
    lateinit var coords: TextureCoords
    var cullFace = CullFace.FRONT
    var color: Color = Color.WHITE

    fun transform(transform: Transform){
        this.globalTransform.append(transform)
        globalTransformModified = true
    }

    fun transform(transforms: Iterable<Transform>){
        transforms.forEach { transform(it) }
    }

    fun localTransform(transform: Transform){
        this.localTransform.append(transform)
        localTransformModified = true
    }

    fun localTransform(transforms: Iterable<Transform>){
        transforms.forEach { localTransform(it) }
    }

    fun render(gl: GL2){
        if(!this::texture.isInitialized)
            return

        gl.glPushMatrix()

        // Validate all transforms
        gl.glMultMatrixd(getGlobalTransformMatrix())
        validateLocalTransform()

        // Setting parameters
        gl.glEnable(GL_TEXTURE_2D)
        texture.bind(gl)
        texture.setTexParameteri(gl, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        texture.setTexParameteri(gl, GL_TEXTURE_LOD_BIAS, 0)
        texture.setTexParameteri(gl, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        texture.setTexParameteri(gl, GL_TEXTURE_MAX_ANISOTROPY_EXT, 16)

        gl.glCullFace(when(cullFace){
            CullFace.FRONT -> {
                gl.glEnable(GL_CULL_FACE)
                GL_FRONT
            }
            CullFace.BACK -> {
                gl.glEnable(GL_CULL_FACE)
                GL_BACK
            }
            CullFace.NONE -> {
                gl.glDisable(GL_CULL_FACE)
                GL_NONE
            }
        })

        // Select points
        val coords: TextureCoords = if(this::coords.isInitialized) coords else texture.imageTexCoords

        gl.glBegin(GL2.GL_QUADS)
        gl.glColor4d(color.red, color.green, color.blue, color.opacity)

        gl.glTexCoord2f(coords.left(), coords.top())
        gl.glVertex3d(localPoint3.x, localPoint3.y, localPoint3.z)

        gl.glTexCoord2f(coords.right(), coords.top())
        gl.glVertex3d(localPoint1.x, localPoint1.y, localPoint1.z)

        gl.glTexCoord2f(coords.right(), coords.bottom())
        gl.glVertex3d(localPoint2.x, localPoint2.y, localPoint2.z)

        gl.glTexCoord2f(coords.left(), coords.bottom())
        gl.glVertex3d(localPoint4.x, localPoint4.y, localPoint4.z)

        gl.glEnd()

        // Reset
        gl.glDisable(GL_TEXTURE_2D)

        gl.glPopMatrix()
    }

    fun rotate90(){
        val buf = point4

        point4 = point3
        point3 = point2
        point2 = point1
        point1 = buf
    }

    fun points(): Array<Point3D> = arrayOf(point1, point2, point3, point4)

    private fun getGlobalTransformMatrix(): DoubleBuffer {
        if(globalTransformModified) {
            globalTransformModified = false
            globalTransformMatrix = DoubleBuffer.wrap(globalTransform.toGLMatrix())
        }
        return globalTransformMatrix
    }

    private fun Affine.toGLMatrix() = doubleArrayOf(
        mxx, myx, mzx, 0.0,
        mxy, myy, mzy, 0.0,
        mxz, myz, mzz, 0.0,
        tx, ty, tz, 1.0
    )

    private fun validateLocalTransform(){
        if(localTransformModified){
            localTransformModified = false
            localPoint1 = localTransform.transform(point1)
            localPoint2 = localTransform.transform(point2)
            localPoint3 = localTransform.transform(point3)
            localPoint4 = localTransform.transform(point4)
        }
    }

    private fun Affine.transform(point: Point3D): Point3D{
        val transformed = transform(point.x, point.y, point.z)
        return Point3D(transformed.x, transformed.y, transformed.z)
    }
}