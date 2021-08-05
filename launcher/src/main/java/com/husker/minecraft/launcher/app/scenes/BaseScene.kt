package com.husker.minecraft.launcher.app.scenes

import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.app.animation.EasingTransition
import com.husker.minecraft.launcher.app.animation.NodeAnimation
import com.husker.minecraft.launcher.app.animation.easing.Elastic
import com.husker.minecraft.launcher.app.minecraft.MineVersion
import com.husker.minecraft.launcher.app.minecraft.scene.PreviewScene
import com.husker.minecraft.launcher.app.scenes.nplay.NPlayTab
import com.husker.minecraft.launcher.app.scenes.profile.ProfileTab
import com.husker.minecraft.launcher.app.scenes.settings.SettingsTab
import com.husker.minecraft.launcher.tools.fx.AnchorUtils
import com.husker.minecraft.launcher.tools.fx.EffectBlender
import com.husker.minecraft.launcher.tools.fx.nodes.DebugPanel
import com.husker.minecraft.launcher.tools.fx.scenes.ResourceScene
import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.SVGPath
import javafx.util.Duration


class BaseScene : ResourceScene("base.fxml") {

    companion object {
        @JvmStatic lateinit var instance : BaseScene
    }

    private var topTabPane : Pane
    private var bottomTabPane : Pane

    private var contentPane : Pane
    private var backgroundPane : Pane
    private var previewScene = PreviewScene()

    private var tabsPane : Pane
    private var tabTitleLabel : Label
    private var tabTitlePane : Pane

    private lateinit var _selectedTab : Tab
    private lateinit var _nextTab : Tab
    private var _animThread: Thread? = null

    private var _titleVisible = false
    lateinit var titleAnim : EasingTransition
    var titleVisible : Boolean
        get() = _titleVisible
        set(value) {
            if(_titleVisible == value)
                return
            _titleVisible = value
            titleAnim = if(value)
                NodeAnimation.showNode(tabTitlePane, NodeAnimation.Type.TOP, duration = 600)
            else
                NodeAnimation.hideNode(tabTitlePane, NodeAnimation.Type.TOP, duration = 600)
        }

    var selectedTab : Tab
        set(value) {
            _nextTab = value

            // Hide title

            if(tabTitleLabel.text != "[default]")
                titleVisible = false

            if(_animThread == null || !_animThread!!.isAlive){
                Thread{
                    // Wait for hide animations
                    if(this::_selectedTab.isInitialized) {
                        _selectedTab.onHideListeners.forEach { it.run() }
                        while (_selectedTab.currentAnimations.get() > 0)
                            Thread.sleep(5)
                    }
                    if(this::titleAnim.isInitialized)
                        titleAnim.waitForEnd()

                    // Change content
                    _selectedTab = _nextTab
                    Platform.runLater {
                        contentPane.children.clear()
                        contentPane.children.add(value.content)
                        AnchorUtils.bind(value.content)

                        tabTitleLabel.text = value.title
                        titleVisible = true

                        _selectedTab.onShowListeners.forEach{it.run()}
                    }
                }.start()
            }
        }
        get() {
            return _selectedTab
        }
    var tabs = arrayListOf<Tab>()


    init {
        instance = this
        stylesheets.add(Resources.style("base.css"))

        // Tabs
        topTabPane = lookup("#top_tabs") as Pane
        bottomTabPane = lookup("#bottom_tabs") as Pane
        tabsPane = lookup("#tabs") as Pane
        tabsPane.opacity = 0.0

        // Content
        contentPane = lookup("#content") as Pane
        backgroundPane = lookup("#background_pane") as Pane

        // Preview scene
        val bgContent = AnchorPane()
        backgroundPane.children.add(bgContent)

        bgContent.children.add(previewScene)
        bgContent.children.add(DebugPanel())
        previewScene.widthProperty().bind(widthProperty())
        previewScene.heightProperty().bind(heightProperty())


        // Title
        tabTitleLabel = lookup("#tab_title_label") as Label
        tabTitlePane = lookup("#tab_title_pane") as Pane
        tabTitlePane.opacity = 0.0

        addTab(NPlayTab())
        //addTab(PlayTab())
        addTab(ProfileTab())
        addTab(SettingsTab())
        //addTab(PlayTab())

        Thread{
            NodeAnimation.showNode(tabsPane, NodeAnimation.Type.LEFT, Elastic.In())

            Thread.sleep(1000)
            selectedTab = tabs[0]
        }.start()

    }

    fun setBackgroundVersion(version: MineVersion){
        val parameters = version.getPreviewParameters()

        backgroundPane.background = Background(BackgroundFill(parameters.color, CornerRadii(0.0), Insets(0.0, 0.0, 0.0, 0.0)))
        Thread {
            previewScene.applyVersionMap(version)
        }.start()

    }

    fun addTab(tab : Tab){
        tabs.add(tab)
        val pane = Resources.fxml<VBox>("tab_icon.fxml")
        val content = pane.lookup("#content") as Pane
        val button = pane.lookup("#btn") as Pane

        button.setOnMousePressed {
            selectedTab = tab
        }

        button.background = Background(BackgroundFill(Color.color(0.878, 0.898, 0.925), CornerRadii(3.0), Insets.EMPTY))

        if(tab.tabNode is SVGPath) {
            val svgShape = Region()
            svgShape.shape = tab.tabNode as SVGPath
            svgShape.setPrefSize(25.0, 25.0)
            svgShape.background = Background(BackgroundFill(tab.color, CornerRadii.EMPTY, Insets.EMPTY))
            content.children.add(svgShape)
        }else {
            val node = tab.tabNode
            node.prefWidth(button.prefWidth)
            node.prefHeight(button.prefHeight)
            val clip = Rectangle(50.0, 50.0)
            clip.arcWidth = 6.0
            clip.arcHeight = 6.0
            node.clip = clip
            content.children.add(node)
        }

        val animation = TabButtonTransition(Duration.millis(70.0), button)
        content.setOnMouseEntered {
            animation.rate = 1.0
            animation.play()
        }

        content.setOnMouseExited {
            animation.rate = -1.0
            animation.play()
        }

        if(tab.bottom)
            bottomTabPane.children.add(pane)
        else
            topTabPane.children.add(pane)
    }

    class TabButtonTransition(duration: Duration, var node: Node) : Transition() {

        init {
            cycleDuration = duration
            interpolator = Interpolator.EASE_IN
            interpolate(0.0)
        }

        override fun interpolate(frac: Double) {
            node.effect = EffectBlender(

                InnerShadow(BlurType.THREE_PASS_BOX, Color.color(0.454, 0.490, 0.533, .3), 6.0, 0.0, 4.0, 4.0),
                InnerShadow(BlurType.THREE_PASS_BOX, Color.color(1.0, 1.0, 1.0, .5), 6.0, 0.0, -4.0, -4.0),
                DropShadow(BlurType.THREE_PASS_BOX, Color.color(1.0, 1.0, 1.0, .5), 6.0, 0.0, 4.0, 4.0),
                DropShadow(BlurType.THREE_PASS_BOX, Color.color(0.454, 0.490, 0.533, .2), 6.0, 0.0, -4.0, -4.0)

            ).effect

            node.effect = EffectBlender(
                DropShadow(BlurType.THREE_PASS_BOX, Color.web("#fff9"), 20.0, 0.0, -7.0, -7.0),
                DropShadow(BlurType.THREE_PASS_BOX, Color.web("#fff9"), 5.0, 0.0, -4.0, -4.0),
                DropShadow(BlurType.THREE_PASS_BOX, Color.web("#0002"), 20.0, 0.0, 7.0, 7.0),
                DropShadow(BlurType.THREE_PASS_BOX, Color.web("#0001"), 5.0, 0.0, 4.0, 4.0)
            ).effect



        }
    }
}