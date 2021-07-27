package com.husker.minecraft.launcher.tools.fx

import javafx.beans.value.ChangeListener
import javafx.scene.image.ImageView
import javafx.scene.layout.Region

class ImageContentUtils {

    companion object{
        @JvmStatic
        fun scale(imageView: ImageView) {
            val parent = imageView.parent
            if (parent is Region) {
                imageView.isPreserveRatio = true
                val listener = ChangeListener<Number> { _, _, _ ->
                    if(imageView.image == null)
                        return@ChangeListener
                    val r1 = imageView.image.width / imageView.image.height
                    val r2 = parent.width / parent.height
                    if (r1 < r2) {
                        val scale = parent.height / imageView.image.height
                        imageView.fitWidth = Double.MAX_VALUE
                        imageView.fitHeight = parent.height
                        imageView.translateX = (parent.width - imageView.image.width * scale) / 2
                        imageView.translateY = 0.0
                    } else {
                        val scale = parent.width / imageView.image.width
                        imageView.fitWidth = parent.width
                        imageView.fitHeight = Double.MAX_VALUE
                        imageView.translateX = 0.0
                        imageView.translateY = (parent.height - imageView.image.height * scale) / 2
                    }
                }
                parent.widthProperty().addListener(listener)
                parent.heightProperty().addListener(listener)
                imageView.imageProperty().addListener{_, _, _ -> listener.changed(null, 0, 0)}
            }
        }

        @JvmStatic
        fun fill(imageView: ImageView) {
            val parent = imageView.parent
            if (parent is Region) {
                imageView.isPreserveRatio = true
                val listener = ChangeListener<Number> { _, _, _ ->
                    if(imageView.image == null)
                        return@ChangeListener
                    val r1 = imageView.image.width / imageView.image.height
                    val r2 = parent.width / parent.height
                    if (r1 < r2) {
                        val scale = parent.width / imageView.image.width
                        imageView.fitWidth = parent.width
                        imageView.fitHeight = Double.MAX_VALUE
                        imageView.translateX = 0.0
                        imageView.translateY = (parent.height - imageView.image.height * scale) / 2
                    } else {
                        val scale = parent.height / imageView.image.height
                        imageView.fitWidth = Double.MAX_VALUE
                        imageView.fitHeight = parent.height
                        imageView.translateX = (parent.width - imageView.image.width * scale) / 2
                        imageView.translateY = 0.0
                    }
                }
                parent.widthProperty().addListener(listener)
                parent.heightProperty().addListener(listener)
                imageView.imageProperty().addListener{_, _, _ -> listener.changed(null, 0, 0)}
            }
        }
    }
}