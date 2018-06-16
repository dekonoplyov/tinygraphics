package com.dekonoplyov.renderer.util

class ZBuffer(private val width: Int, private val height: Int) {
    private val buffer = FloatArray(width * height) { -Float.MAX_VALUE }

    operator fun set(x: Float, y: Float, z: Float) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return
        }
        if ((x + y * width).toInt() >= buffer.size) {
            return
        }
        buffer[(x + y * width).toInt()] = z
    }

    operator fun get(x: Float, y: Float): Float {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Float.MAX_VALUE
        }
        if ((x + y * width).toInt() >= buffer.size) {
            return -Float.MAX_VALUE
        }
        return buffer[(x + y * width).toInt()]
    }
}