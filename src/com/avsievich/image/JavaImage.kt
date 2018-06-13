package com.avsievich.image

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class JavaImage(width: Int,
                height: Int,
                hasAlpha: Boolean = true,
                flipVertical: Boolean = false) : Image(width, height, hasAlpha, flipVertical) {
    override fun save(name: String) {
        val i = BufferedImage(width, height, if (hasAlpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB)
        i.setRGB(0, 0, width, height, pixels, 0, width)
        ImageIO.write(i, "png", File(name))
    }
}