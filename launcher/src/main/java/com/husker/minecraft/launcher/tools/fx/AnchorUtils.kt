package com.husker.minecraft.launcher.tools.fx

import javafx.scene.Node
import javafx.scene.layout.AnchorPane

class AnchorUtils {

    companion object {

        @JvmStatic fun bind(node : Node) = bind(node, 0)
        @JvmStatic fun bind(node : Node, indent : Int) = bind(node, indent, indent, indent, indent)

        @JvmStatic
        fun bind(node : Node, left : Int, top : Int, right : Int, bottom : Int){
            AnchorPane.setLeftAnchor(node, left.toDouble())
            AnchorPane.setTopAnchor(node, top.toDouble())
            AnchorPane.setRightAnchor(node, right.toDouble())
            AnchorPane.setBottomAnchor(node, bottom.toDouble())
        }
    }
}