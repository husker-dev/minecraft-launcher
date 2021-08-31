package com.husker.minecraft.launcher.app.scenes.nplay

import com.husker.minecraft.launcher.app.Resources
import com.husker.minecraft.launcher.app.animation.NodeAnimation
import com.husker.minecraft.launcher.app.animation.WaveAnimation
import com.husker.minecraft.launcher.app.minecraft.versions.MineVersion
import com.husker.minecraft.launcher.app.scenes.Tab
import com.husker.minecraft.launcher.tools.fx.ImageContentUtils
import javafx.scene.image.ImageView
import javafx.scene.paint.Color


class NPlayTab : Tab("Play", Resources.svg("play.svg")) {

    private val logo : ImageView

    private lateinit var version : MineVersion

    init {
        color = Color.web("#3FCC62")    // Green tab button
        applyFX("nplay")

        logo = findById("version_logo") as ImageView
        NodeAnimation.bindTab(this, logo, NodeAnimation.Type.TOP)
        WaveAnimation.apply(logo)
        ImageContentUtils.scale(logo)

        setVersion(MineVersion.v1_16)
    }

    private fun setVersion(version : MineVersion){
        Thread{
            Thread.sleep(1000)

            NodeAnimation.hideNode(logo, NodeAnimation.Type.TOP)
            /*
            if(background.opacity > 0)
                NodeAnimation.hideNode(background, NodeAnimation.Type.RISE)

             */
            Thread.sleep(1000)
            this.version = version
            logo.image = version.getLogo()

            NodeAnimation.showNode(logo, NodeAnimation.Type.TOP)
            //NodeAnimation.showNode(background, NodeAnimation.Type.RISE)

            //preview.root.opacity = 0.3
            scene.setBackgroundVersion(version)

            //Thread.sleep(3000)
            //NodeAnimation.showNode(preview.root, NodeAnimation.Type.BOTTOM)
        }.start()

    }

}