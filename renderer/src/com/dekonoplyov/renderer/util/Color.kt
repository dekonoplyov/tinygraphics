package com.dekonoplyov.renderer.util

const val BLACK = -0x1000000
const val DKGRAY = -0xbbbbbc
const val GRAY = -0x777778
const val LTGRAY = -0x333334
const val WHITE = -0x1
const val RED = -0x10000
const val GREEN = -0xff0100
const val BLUE = -0xffff01
const val YELLOW = -0x100
const val CYAN = -0xff0001
const val MAGENTA = -0xff01
const val TRANSPARENT = 0

fun argb(alpha: Int, red: Int, green: Int, blue: Int) =
        alpha shl 24 or (red shl 16) or (green shl 8) or blue

fun rgb(red: Int, green: Int, blue: Int) =
        -0x1000000 or (red shl 16) or (green shl 8) or blue

fun Int.alpha() = this.ushr(24)

fun Int.red() = this shr 16 and 0xFF

fun Int.green() = this shr 8 and 0xFF

fun Int.blue() = this and 0xFF

fun Int.applyIntesity(intensity: Float) = rgb(
            (this.red() * intensity).toInt(),
            (this.green() * intensity).toInt(),
            (this.blue() * intensity).toInt())
