package com.husker.minecraft.launcher.app.scenes.play

import com.husker.minecraft.launcher.app.animation.NodeAnimation
import com.husker.minecraft.launcher.app.animation.easing.Back
import com.husker.minecraft.launcher.app.scenes.Tab
import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.app.animation.AnimatedMoving
import com.husker.minecraft.launcher.tools.fx.HiDpiUtils
import com.husker.minecraft.launcher.tools.fx.nodes.CoolScrollPane
import javafx.scene.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.Box
import javafx.scene.shape.Shape3D


class PlayTab : Tab("Play", Resources.svg("play.svg")) {

    private val scrollContent : AnchorPane
    private val scroll : CoolScrollPane
    private val versionsPanes = FlowPane(60.0, 60.0)
    private val pane3D : SubScene
    private val objectGroup = Group()
    private var camera = PerspectiveCamera()

    private val topIndent = 85.0
    private val bottomIndent = 10.0
    private val leftIndent = 110.0
    private val rightIndent = 260.0

    init {
        color = Color.web("#3FCC62")    // Green tab button
        applyFX("play")

        NodeAnimation.bindTab(this, findById("right_panel"), NodeAnimation.Type.RIGHT)

        scroll = findById("versions_scroll") as CoolScrollPane
        scrollContent = findById("versions_content") as AnchorPane

        scroll.scrollListeners.add{
            titleVisible = it >= 0
        }
        onShowListeners.add{ scroll.y = 0.0 }

        // 3D scene
        pane3D = SubScene(objectGroup, 100.0, 100.0, true, SceneAntialiasing.BALANCED)    // Initial size, later will be changed
        pane3D.translateX = -leftIndent

        scrollContent.children.add(versionsPanes)
        scrollContent.children.add(pane3D)

        versionsPanes.prefWidthProperty().bind(scrollContent.prefWidthProperty())
        scrollContent.prefWidthProperty().addListener { _, _, newValue ->
            pane3D.width = newValue.toDouble() + leftIndent + rightIndent
        }
        scroll.heightProperty().addListener { _, _, newValue ->
            pane3D.height = newValue.toDouble() + topIndent + bottomIndent
        }

        // Camera
        pane3D.camera = camera
        camera.fieldOfView = 30.0
        camera.isVerticalFieldOfView = false
        camera.translateX = -leftIndent
        val camUpdateFunc = {
            val scrollY = scrollContent.layoutY
            camera.translateY = -scrollY - topIndent
            pane3D.translateY = -scrollY - topIndent
        }
        scrollContent.layoutYProperty().addListener { _, _, _ -> camUpdateFunc.invoke() }
        camUpdateFunc.invoke()

        // Light
        val camLight = PointLight(Color.WHITE)
        val ambientLight = AmbientLight(Color(1.0, 1.0, 1.0, 0.3))
        objectGroup.children.add(camLight)
        objectGroup.children.add(ambientLight)

        val calcUpdateFunc = {
            val dpi = HiDpiUtils.getDpi()
            camLight.translateY = pane3D.translateY * dpi
            camLight.translateZ = -100.0 * dpi
            camLight.translateX = scroll.width / 2 * dpi
        }
        camLight.constantAttenuation = 0.9                                              // Brightness (1 - darkest)
        HiDpiUtils.addDpiListener{ calcUpdateFunc.invoke() }                            // Update light when window dpi is changed
        scroll.widthProperty().addListener { _, _, _ -> calcUpdateFunc.invoke() }       // Move camera when width is changed
        pane3D.translateYProperty().addListener{_, _, _ -> calcUpdateFunc.invoke()}     // Sync with scroll
        calcUpdateFunc.invoke()                                                         // (Initial update)


        for(i in 1..20)
            addVersionPane(VersionPane(this))
    }


    private fun addVersionPane(pane : VersionPane){
        versionsPanes.children.add(pane)
        objectGroup.children.add(pane.content)
    }


    open class BaseVersionPane(tab : Tab) : AnchorPane(){
        companion object{
            var count = 0
        }

        init{
            count++
            setPrefSize(200.0, 200.0)
            NodeAnimation.bindTab(tab, this, NodeAnimation.Type.CENTER, delay = 500 + (40 * (count - 1)), easing = Back.In(), duration = 600)
            AnimatedMoving.setAnimatedMoving(this, Back.Out(), 400)
        }
    }

    class VersionPane(tab : Tab) : BaseVersionPane(tab) {

        var content = Group()
        private val blockSize = prefWidth / 7

        init{
            apply3DClue()

            val pane = Pane()
            setLeftAnchor(pane, 30.0)
            setRightAnchor(pane, 30.0)
            setTopAnchor(pane, 0.0)
            setBottomAnchor(pane, 30.0)
            pane.styleClass.add("base_panel")
            children.add(pane)


            content.children.add(createBlock("v1_16/netherrack", 0, 1, 0))
            content.children.add(createBlock("v1_16/netherrack", 1, 0, 0))
            content.children.add(createBlock("v1_16/netherrack", 0, 0, 1))
            content.children.add(createBlock("v1_16/netherrack", 0, 0, 0))

            content.children.add(createBlock("v1_16/netherrack", 4, 0, 0))
            content.children.add(createBlock("v1_16/netherrack", 6, 0, 0))


        }

        private fun apply3DClue(){
            content.layoutXProperty().bind(layoutXProperty())
            content.layoutYProperty().bind(layoutYProperty())

            opacityProperty().addListener { _, _, newValue -> set3DOpacity(newValue.toDouble()) }

            content.scaleXProperty().bind(scaleXProperty())
            content.scaleXProperty().bind(scaleYProperty())
            content.scaleZProperty().bind(scaleZProperty())
        }

        private fun set3DOpacity(opacity : Double){
            for(shape in content.children){
                if(shape is Shape3D){
                    val material = shape.material
                    if(material is PhongMaterial)
                        material.diffuseColor = Color(1.0, 1.0, 1.0, opacity)
                }
            }
        }

        private fun createBlock(textureName : String, x : Int, y : Int, z : Int) : Box{
            val box = Box(blockSize, blockSize, blockSize)
            val material = PhongMaterial()
            box.material = material
            material.diffuseMap = Resources.image("minecraft/$textureName.png", 256, 256)
            translateObject(box, x, y, z)
            return box
        }

        private fun translateObject(node : Node, x : Int, y : Int, z : Int) {
            node.translateX = translateX(x)
            node.translateY = translateY(y)
            node.translateZ = translateZ(z)
        }

        private fun translateX(value : Int) : Double{
            return value * blockSize + (blockSize / 2)
        }

        private fun translateY(value : Int) : Double{
            return prefHeight - ((value + 1) * blockSize) + (blockSize / 2)
        }

        private fun translateZ(value : Int) : Double{
            return -value * blockSize - (blockSize / 2)
        }
    }


}