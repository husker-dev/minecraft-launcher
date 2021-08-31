package com.husker.minecraft.launcher.app.minecraft.scene

import com.husker.minecraft.launcher.app.minecraft.blocks.Block
import com.husker.minecraft.launcher.app.minecraft.model.ModelTexture
import com.husker.minecraft.launcher.app.minecraft.blocks.AirBlock
import com.husker.minecraft.launcher.app.minecraft.versions.MineVersion
import com.husker.minecraft.launcher.app.opengl.GLPane
import com.husker.minecraft.launcher.app.opengl.Point3D
import com.husker.minecraft.launcher.tools.fx.LauncherTimer
import com.husker.openglfx.utils.NodeUtils
import com.jogamp.opengl.*
import com.jogamp.opengl.GL2ES1.*
import com.jogamp.opengl.fixedfunc.GLMatrixFunc
import com.jogamp.opengl.glu.GLU
import javafx.scene.*
import javafx.scene.transform.*
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin


class PreviewScene : GLPane(createGLCapabilities(), 60) {

    companion object {
        @JvmStatic fun createGLCapabilities(): GLCapabilities{
            val caps = GLCapabilities(GLProfile.getDefault())
            caps.isBackgroundOpaque = false
            caps.sampleBuffers = true
            caps.numSamples = 4
            //caps.numSamples = 8
            return caps
        }
    }

    private lateinit var glu: GLU

    private val blockSize = 1.0

    //private val content = root as Group
    private val camera = PerspectiveCamera(true)
    private val blocks = arrayListOf<SceneBlock>()
    private val blocksCoordinates = HashMap<Int, HashMap<Int, HashMap<Int, SceneBlock>>>()

    private val rotationX = Rotate(0.0, Rotate.Y_AXIS)
    private val rotationY = Rotate(0.0, Rotate.X_AXIS)

    private val allTextures = arrayListOf<ModelTexture>()

    init{
        rotationX.pivotY = 4 * blockSize
        rotationY.pivotY = 4 * blockSize

        var lastX = 0.0
        var lastY = 0.0
        var rx = 0.0
        var ry = 0.0
        NodeUtils.onWindowReady(this){
            scene.setOnMousePressed {
                lastX = it.x
                lastY = it.y
            }
            scene.setOnMouseDragged {
                rx += it.x - lastX
                ry += it.y - lastY
                lastX = it.x
                lastY = it.y
                rotate(rx, ry)
            }
        }


        var camAnimationFrame = 0.0.toBigDecimal()
        LauncherTimer.create(10){
            val slowDown = 300
            val range = 6

            val curX = sin(camAnimationFrame.toDouble() / slowDown) * range
            val curY = cos(camAnimationFrame.toDouble() / slowDown) * range

            rotate(curX, curY)
            camAnimationFrame++
        }
    }

    private fun rotate(x : Double, y : Double){
        rotationX.angle = x
        rotationY.angle = y
    }

    private fun clear(){
        blocks.clear()
    }

    fun applyVersionMapFile(version: MineVersion){
        clear()
        try {
            MapReader(version.getPreviewParameters().map.invoke()){ name, point, lights, sides, data ->
                addBlock(
                    version.getBlockInstance(name, lights, sides, data),
                    point.x.toInt(),
                    point.y.toInt(),
                    point.z.toInt()
                )
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        recalculateBlocks()
    }

    private fun addBlock(block: Block, x: Int, y: Int, z: Int){
        val sceneBlock = SceneBlock(block, -x, y, z)

        if(!blocksCoordinates.containsKey(x))
            blocksCoordinates[x] = HashMap()
        if(!blocksCoordinates[x]!!.containsKey(y))
            blocksCoordinates[x]!![y] = HashMap()
        blocksCoordinates[x]!![y]!![z] = sceneBlock

        blocks.add(sceneBlock)
    }

    private fun recalculateBlocks(){
        val defaultBlockTransforms = arrayListOf(
            Scale(blockSize, blockSize, blockSize),
            Translate(-blockSize/2, -blockSize/2, -blockSize/2)
        )

        blocks.sortedWith{ a, b ->
            val queue = a.originalBlock.renderQueuePos.compareTo(b.originalBlock.renderQueuePos)
            return@sortedWith if(queue != 0)
                queue
            else {
                a.distance.compareTo(b.distance)
            }
        }.forEach {
            try {
                val nearSides = arrayListOf<Block.Side>()

                val frontBlock = getBlockAt(it.x, it.y, it.z - 1)
                val backBlock = getBlockAt(it.x, it.y, it.z + 1)
                val leftBlock = getBlockAt(it.x - 1, it.y, it.z)
                val rightBlock = getBlockAt(it.x + 1, it.y, it.z)
                val topBlock = getBlockAt(it.x, it.y + 1, it.z)
                val bottomBlock = getBlockAt(it.x, it.y - 1, it.z)

                if (!frontBlock.sides.contains(Block.Side.Back))
                    nearSides.add(Block.Side.Face)
                if (!backBlock.sides.contains(Block.Side.Face))
                    nearSides.add(Block.Side.Back)
                if (!leftBlock.sides.contains(Block.Side.Right))
                    nearSides.add(Block.Side.Left)
                if (!rightBlock.sides.contains(Block.Side.Left))
                    nearSides.add(Block.Side.Right)
                if (!topBlock.sides.contains(Block.Side.Bottom))
                    nearSides.add(Block.Side.Top)
                if (!bottomBlock.sides.contains(Block.Side.Top))
                    nearSides.add(Block.Side.Bottom)

                if (nearSides.isEmpty())
                    return@forEach

                val blockTextures = it.originalBlock.getAllTextures(Point3D(it.x, it.y, it.z))
                blockTextures.forEach { texture ->
                    texture.transform(defaultBlockTransforms)
                    texture.transform(Translate(
                        it.x.toDouble() * blockSize,
                        -(it.y.toDouble() - 4) * blockSize,
                        it.z.toDouble() * blockSize
                    ))
                }

                allTextures.addAll(blockTextures)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun getBlockAt(x: Int, y: Int, z: Int): Block {
        return try {
            blocksCoordinates[-x]!![y]!![z]!!.originalBlock
        }catch (e: Exception){
            AirBlock()
        }
    }


    data class SceneBlock(val originalBlock: Block, val x: Int, val y: Int, val z: Int){
        companion object{
            val startPoint = Point3D(0.0, 3.0, -3.0)
        }

        val distance: Double by lazy {
            startPoint.distance(Point3D(x.toDouble(), y.toDouble(), z.toDouble()))
        }
    }

    override fun init(gl: GL) {
        if(gl !is GL2)
            return
        glu = GLU.createGLU(gl)

        gl.glClearColor(56/255f,14/255f,12/255f,1.0f)
        gl.glClearDepth(1.0)
        //gl.glShadeModel(GLLightingFunc.GL_SMOOTH)
        gl.glEnable(GL_DEPTH_TEST)
        gl.glEnable(GL_MULTISAMPLE)

        gl.glEnable(GL_CULL_FACE)
        gl.glEnable(GL_TEXTURE_2D)

        gl.glEnable(GL_BLEND)
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        gl.glEnable(GL_ALPHA_TEST);
        gl.glAlphaFunc(GL_GREATER, 0f);
        // Fog

        /*
        gl.glEnable(GL_FOG)
        gl.glEnable(GL_FOG)                       // Включает туман (GL_FOG)
        gl.glFogi(GL_FOG_MODE, GL_LINEAR)// Выбираем тип тумана
        gl.glFogfv(GL_FOG_COLOR, color(56, 14, 12))        // Устанавливаем цвет тумана
        gl.glFogf(GL_FOG_DENSITY, 0.7f);          // Насколько густым будет туман
        gl.glHint(GL_FOG_HINT, GL_NICEST)               // Вспомогательная установка тумана
        gl.glFogf(GL_FOG_START, 15.0f)             // Глубина, с которой начинается туман
        gl.glFogf(GL_FOG_END, 35.0f)               // Глубина, где туман заканчивается.
         */


        //gl.glDepthFunc(GL_LEQUAL)
        //gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST)

    }

    override fun dispose(gl: GL) {

    }

    override fun display(gl: GL) {
        if(gl !is GL2)
            return
        gl.glClear(GL.GL_COLOR_BUFFER_BIT)
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()

        // Cam

        gl.glTranslatef(0.0f, 0.0f, -17.0f)
        gl.glRotatef(180.0f, 0.0f, 1.0f, 0.0f)

        gl.glMultMatrixd(rotationX.toGLMatrix(), 0)
        gl.glMultMatrixd(rotationY.toGLMatrix(), 0)

        // Scene rotation
        for(i in allTextures.indices)
            allTextures[i].render(gl)
    }

    override fun reshape(gl: GL, width: Float, height: Float) {
        if(gl !is GL2)
            return
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION)
        gl.glLoadIdentity()

        glu.gluPerspective(50.0, width.toDouble() / height.toDouble(), 1.0, 1000.0)
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    fun color(r: Int, g: Int, b: Int): FloatBuffer = FloatBuffer.wrap(floatArrayOf(r / 255f, g / 255f, b / 255f, 1f))

    private fun Transform.toGLMatrix(): DoubleArray = doubleArrayOf(
        mxx, myx, mzx, 0.0,
        mxy, myy, mzy, 0.0,
        mxz, myz, mzz, 0.0,
        tx, ty, tz, 1.0
    )
}