package com.dekonoplyov.renderer.util

class ZBuffer(private val width: Int, private val height: Int) {
    private val buffer = FloatArray(width * height) { -Float.MAX_VALUE }

    operator fun set(x: Int, y: Int, z: Float) {
        buffer[x + y * width] = z
    }

    operator fun get(x: Int, y: Int): Float {
        return buffer[x + y * width]
    }
}