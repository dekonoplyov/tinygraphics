package com.dekonoplyov.renderer.util

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class TGAHeader {
    var idLength: Int = 0
    var colorMapType: Int = 0
    var dataTypeCode: Int = 0
    var colorMapOrigin: Int = 0 // 16 bits
    var colorMapLength: Int = 0 // 16 bits
    var colorMapDepth: Int = 0
    var xOrigin: Int = 0 // 16 bits
    var yOrigin: Int = 0 // 16 bits
    var width: Int = 0   // 16 bits
    var height: Int = 0  // 16 bits
    var bytesPerPixel: Int = 0
    var imageDescriptor: Int = 0

    enum class ColorFormat(val bytesPerPixel: Int) {
        GrayScale(1),
        RGB(3),
        RGBA(4)
    }

    fun isGrayScale(): Boolean {
        return bytesPerPixel == ColorFormat.GrayScale.bytesPerPixel
    }

    fun isRGB(): Boolean {
        return bytesPerPixel == ColorFormat.RGB.bytesPerPixel
    }

    fun isRGBA(): Boolean {
        return bytesPerPixel == ColorFormat.RGBA.bytesPerPixel
    }

    fun isUncompressed(): Boolean {
        return dataTypeCode == 1 || dataTypeCode == 2
    }

    fun isRLECompressed(): Boolean {
        return dataTypeCode == 10 || dataTypeCode == 11
    }

    fun isFlippedVertical(): Boolean {
        return imageDescriptor and 0x20 == 0
    }

    fun isFlippedHorizontal(): Boolean {
        return imageDescriptor and 0x10 != 0
    }
}

class TGAData(val header: TGAHeader, val pixels: IntArray)

class TGAReader(filename: String) {
    private var offset = 0
    private var buf = readByteArray(filename)

    private fun readByteArray(filename: String): ByteArray {
        val f = File(filename)
        val buf = ByteArray(f.length().toInt())
        val bis = BufferedInputStream(FileInputStream(f))
        bis.read(buf)
        bis.close()
        return buf
    }

    private fun btoi(b: Byte): Int {
        val i = b.toInt()
        return if (i < 0) i + 256 else i
    }

    private fun read(): Int {
        return btoi(buf[offset++])
    }

    private fun read16(): Int {
        return read() or (read() shl 8)
    }

    private fun readHeader(): TGAHeader {
        offset = 0
        val header = TGAHeader()
        header.idLength = read()
        header.colorMapType = read()
        header.dataTypeCode = read()
        header.colorMapOrigin = read16()
        header.colorMapLength = read16()
        header.colorMapDepth = read()
        header.xOrigin = read16()
        header.yOrigin = read16()
        header.width = read16()
        header.height = read16()
        header.bytesPerPixel = read() shr 3
        header.imageDescriptor = read()
        return header
    }

    fun getData(): TGAData {
        val header = readHeader()
        val image = TGAData(header, IntArray(header.width * header.height))

        if (header.isRLECompressed()) {
            readCompressed(image.header, image.pixels)
        } else if (header.isUncompressed()) {
            readUncompressed(image.header, image.pixels)
        }

        return image
    }

    private fun readUncompressed(header: TGAHeader, pixels: IntArray) {
        var pixelIdx = 0
        while (pixelIdx < pixels.size) {
            pixels[pixelIdx++] = readColor(header.bytesPerPixel)
        }
    }

    private fun readCompressed(header: TGAHeader, pixels: IntArray) {
        var pixelIdx = 0
        while (pixelIdx < pixels.size) {
            var chunkheader = read()
            if (chunkheader < 128) {
                ++chunkheader
                for (i in 0 until chunkheader) {
                    val color = readColor(header.bytesPerPixel)
                    pixels[pixelIdx++] = color
                }
            } else {
                chunkheader -= 127
                val color = readColor(header.bytesPerPixel)
                for (i in 0 until chunkheader) {
                    pixels[pixelIdx++] = color
                }
            }
        }
    }

    private fun readColor(bytesPerPixel: Int): Int {
        when (bytesPerPixel) {
            1 -> {
                val v = read()
                return rgb(v, v, v)
            }
            3 -> {
                val b = read()
                val g = read()
                val r = read()
                return rgb(r, g, b)
            }
            4 -> {
                val b = read()
                val g = read()
                val r = read()
                val a = read()
                return argb(a, r, g, b)
            }
            else -> throw RuntimeException("some shit")
        }
    }
}
