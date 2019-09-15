package com.dekonoplyov.renderer.util

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PixelImage(val width: Int,
                 val height: Int,
                 val hasAlpha: Boolean = true,
                 private val pixels: IntArray = IntArray(width * height)) {

    operator fun set(x: Int, y: Int, color: Int) {
        pixels[y * width + x] = color
    }

    operator fun get(x: Int, y: Int): Int {
        return pixels[y * width + x]
    }

    fun flipVertical() {
        val half = height / 2
        for (y in 0 until half) {
            for (x in 0 until width) {
                val c1 = pixels[y * width + x]
                val c2 = pixels[(height - y - 1) * width + x]
                pixels[y * width + x] = c2
                pixels[(height - y - 1) * width + x] = c1
            }
        }
    }

    fun flipHorizontal() {
        val half = width / 2
        for (y in 0 until height) {
            for (x in 0 until half) {
                val c1 = pixels[y * width + x]
                val c2 = pixels[y * width + (width - x - 1)]
                pixels[y * width + x] = c2
                pixels[y * width + (width - x - 1)] = c1
            }
        }
    }

    fun save(name: String, formatName: String = "png") {
        val i = BufferedImage(width, height, if (hasAlpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB)
        i.setRGB(0, 0, width, height, pixels, 0, width)
        ImageIO.write(i, formatName, File(name))
    }

    companion object {
        fun fromTGA(filename: String): PixelImage {
            val imageData = TGAReader(filename).getData()
            val header = imageData.header
            val hasAlpha = header.isRGBA()
            val image = PixelImage(header.width, header.height, hasAlpha, imageData.pixels)

            if (header.isFlippedHorizontal()) {
                image.flipHorizontal()
            }

            if (header.isFlippedVertical()) {
                image.flipVertical()
            }

            return image
        }
    }
}
