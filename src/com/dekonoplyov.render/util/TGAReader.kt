package com.dekonoplyov.render.util

import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

internal object TGAReader {
    private var offset: Int = 0

    @Throws(IOException::class)
    fun getImage(fileName: String): BufferedImage {
        val f = File(fileName)
        val buf = ByteArray(f.length().toInt())
        val bis = BufferedInputStream(FileInputStream(f))
        bis.read(buf)
        bis.close()
        return decode(buf)
    }

    private fun btoi(b: Byte): Int {
        val a = b.toInt()
        return if (a < 0) 256 + a else a
    }

    private fun read(buf: ByteArray): Int {
        return btoi(buf[offset++])
    }

    fun decode(buf: ByteArray): BufferedImage {
        offset = 0

        // Reading header bytes
        // buf[2]=image type code 0x02=uncompressed BGR or BGRA
        // buf[12]+[13]=width
        // buf[14]+[15]=height
        // buf[16]=image pixel size 0x20=32bit, 0x18=24bit
        // buf{17]=Image Descriptor Byte=0x28 (00101000)=32bit/origin upperleft/non-interleaved
        for (i in 0..11)
            read(buf)
        val width = read(buf) + (read(buf) shl 8)   // 00,04=1024
        val height = read(buf) + (read(buf) shl 8)  // 40,02=576
        read(buf)
        read(buf)

        var n = width * height
        val pixels = IntArray(n)
        var idx = 0

        if (buf[2].toInt() == 0x02 && buf[16].toInt() == 0x20) { // uncompressed BGRA
            while (n > 0) {
                val b = read(buf)
                val g = read(buf)
                val r = read(buf)
                val a = read(buf)
                val v = a shl 24 or (r shl 16) or (g shl 8) or b
                pixels[idx++] = v
                n -= 1
            }
        } else if (buf[2].toInt() == 0x02 && buf[16].toInt() == 0x18) {  // uncompressed BGR
            while (n > 0) {
                val b = read(buf)
                val g = read(buf)
                val r = read(buf)
                val a = 255 // opaque pixel
                val v = a shl 24 or (r shl 16) or (g shl 8) or b
                pixels[idx++] = v
                n -= 1
            }
        } else {
            // RLE compressed
            while (n > 0) {
                var nb = read(buf) // num of pixels
                if (nb and 0x80 == 0) { // 0x80=dec 128, bits 10000000
                    for (i in 0..nb) {
                        val b = read(buf)
                        val g = read(buf)
                        val r = read(buf)
                        pixels[idx++] = -0x1000000 or (r shl 16) or (g shl 8) or b
                    }
                } else {
                    nb = nb and 0x7f
                    val b = read(buf)
                    val g = read(buf)
                    val r = read(buf)
                    val v = -0x1000000 or (r shl 16) or (g shl 8) or b
                    for (i in 0..nb)
                        pixels[idx++] = v
                }
                n -= nb + 1
            }
        }

        val bimg = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        bimg.setRGB(0, 0, width, height, pixels, 0, width)
        return bimg
    }
}
