package com.dekonoplyov.render.util

import com.avsievich.image.JavaImage
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.cross
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun JavaImage.line(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Int) {
    var x0 = fromX
    var y0 = fromY
    var x1 = toX
    var y1 = toY

    var steep = false

    if (abs(x0 - x1) < abs(y0 - y1)) {
        steep = true
        x0 = y0.also { y0 = x0 }
        x1 = y1.also { y1 = x1 }
    }

    if (x0 > x1) {
        x0 = x1.also { x1 = x0 }
        y0 = y1.also { y1 = y0 }
    }

    val dx = x1 - x0
    val dy = y1 - y0
    val derror2 = abs(dy) * 2
    var error2 = 0
    var y = y0

    for (x in x0..x1) {
        if (steep) {
            this[y, x] = color
        } else {
            this[x, y] = color
        }

        error2 += derror2
        if (error2 > dx) {
            y += if (y1 > y0) 1 else -1
            error2 -= dx * 2
        }
    }
}

fun isPointInsideTriangle(x0: Int, y0: Int, x1: Int, y1: Int, x2: Int, y2: Int,
                          x: Int, y: Int): Boolean {
    val barycentric = cross(
            Float3(x2 - x0, x1 - x0, x0 - x),
            Float3(y2 - y0, y1 - y0, y0 - y))

    if (abs(barycentric.z) < 1f) {
        return false // triangle is degenerate
    }

    return 1 - (barycentric.x + barycentric.y) / barycentric.z >= 0f &&
            barycentric.y / barycentric.z >= 0f &&
            barycentric.x / barycentric.z >= 0f
}

fun JavaImage.triangle(x0: Int, y0: Int, x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
    val bboxMinX = min(x0, min(x1, x2))
    val bboxMinY = min(y0, min(y1, y2))
    val bboxMaxX = max(x0, max(x1, x2))
    val bboxMaxY = max(y0, max(y1, y2))

    for (x in bboxMinX..bboxMaxX) {
        for (y in bboxMinY..bboxMaxY) {
            if (isPointInsideTriangle(x0, y0, x1, y1, x2, y2, x, y)) {
                this[x, y] = color
            }
        }
    }
}