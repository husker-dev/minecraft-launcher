package com.husker.minecraft.launcher.tools.fx.nodes

import com.sun.javafx.geom.BaseBounds
import com.sun.javafx.geom.transform.BaseTransform
import com.sun.javafx.scene.ImageViewHelper
import com.sun.javafx.scene.ImageViewHelper.ImageViewAccessor
import com.sun.javafx.sg.prism.NGImageView
import com.sun.javafx.sg.prism.NGNode
import com.sun.prism.Graphics
import com.sun.prism.Image
import com.sun.prism.Texture
import com.sun.prism.impl.BaseResourceFactory
import javafx.scene.Node
import javafx.scene.image.ImageView

class PixelatedImageView : ImageView() {

    init{
        val field = ImageViewHelper::class.java.getDeclaredField("imageViewAccessor")
        field.isAccessible = true
        val realAccessor = field[null] as ImageViewAccessor

        field[null] = object : ImageViewAccessor {
            override fun doCreatePeer(node: Node): NGNode {
                return object : NGImageView() {
                    private var image: Image? = null
                    override fun setImage(img: Any) {
                        super.setImage(img)
                        image = img as Image
                    }

                    override fun renderContent(g: Graphics) {
                        val factory = g.resourceFactory as BaseResourceFactory
                        val tex = factory.getCachedTexture(image, Texture.WrapMode.CLAMP_TO_EDGE)
                        tex.linearFiltering = false
                        tex.unlock()

                        super.renderContent(g)
                    }
                }
            }

            override fun doUpdatePeer(node: Node) {
                realAccessor.doUpdatePeer(node)
            }

            override fun doComputeGeomBounds(node: Node, bounds: BaseBounds, tx: BaseTransform): BaseBounds {
                return realAccessor.doComputeGeomBounds(node, bounds, tx)
            }

            override fun doComputeContains(node: Node, localX: Double, localY: Double): Boolean {
                return realAccessor.doComputeContains(node, localX, localY)
            }
        }
    }
}