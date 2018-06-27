package com.dekonoplyov.renderer.util

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.cross
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun PixelImage.line(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Int) {
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

fun barycentric(a: Float3, b: Float3, c: Float3, p: Float3): Float3 {
    val cross = cross(
            Float3(c.x - a.x, b.x - a.x, a.x - p.x),
            Float3(c.y - a.y, b.y - a.y, a.y - p.y))

    if (abs(cross.z) < 1e-2f) {
        return Float3(-1f, 1f, 1f)
    }

    return Float3(
            1 - (cross.x + cross.y) / cross.z,
            cross.y / cross.z,
            cross.x / cross.z)
}

fun PixelImage.triangle(a: Float3, b: Float3, c: Float3, zBuffer: ZBuffer, color: Int) {
    val bboxMin = Float2(min(a.x, min(b.x, c.x)), min(a.y, min(b.y, c.y)))
    val bboxMax = Float2(max(a.x, max(b.x, c.x)), max(a.y, max(b.y, c.y)))

    val p = Float3(bboxMin.x, bboxMin.y)
    while (p.x in (bboxMin.x..bboxMax.x)) {
        while (p.y in (bboxMin.y..bboxMax.y)) {
            val bc = barycentric(a, b, c, p)

            if (bc.x < 0f || bc.y < 0f || bc.z < 0f) {
                p.y += 1f
                continue
            }

            p.z = 0f

            p.z += a.z * bc.x // bc.x corresponds to a
            p.z += b.z * bc.y // because of argument order in barycentric call
            p.z += c.z * bc.z

            if (zBuffer[p.x, p.y] < p.z) {
                zBuffer[p.x, p.y] = p.z
                this[p.x.toInt(), p.y.toInt()] = color
            }

            p.y += 1f
        }

        p.x += 1f
        p.y = bboxMin.y
    }
}

fun PixelImage.triangle(a: Float3, b: Float3, c: Float3, zBuffer: ZBuffer,
                       t0: Float3, t1: Float3, t2: Float3,
                       textureMap: BufferedImage, intensity: Float) {
    val bboxMin = Float2(min(a.x, min(b.x, c.x)), min(a.y, min(b.y, c.y)))
    val bboxMax = Float2(max(a.x, max(b.x, c.x)), max(a.y, max(b.y, c.y)))

    val p = Float3(bboxMin.x, bboxMin.y)
    while (p.x in (bboxMin.x..bboxMax.x)) {
        while (p.y in (bboxMin.y..bboxMax.y)) {
            val bc = barycentric(a, b, c, p)

            if (bc.x < 0f || bc.y < 0f || bc.z < 0f) {
                p.y += 1f
                continue
            }

            p.z = a.z * bc.x + b.z * bc.y + c.z * bc.z
            // bc.x corresponds to a, because of argument order in barycentric call

            if (zBuffer[p.x, p.y] < p.z) {
                zBuffer[p.x, p.y] = p.z

                val interpolated = t0 * bc.x + t1 * bc.y + t2 * bc.z
                interpolated.x *= textureMap.width
                interpolated.y *= textureMap.height

                val textureColor = textureMap.getRGB(interpolated.x.toInt(), interpolated.y.toInt())

                this[p.x.toInt(), p.y.toInt()] = rgb(
                        (textureColor.red() * intensity).toInt(),
                        (textureColor.green() * intensity).toInt(),
                        (textureColor.blue() * intensity).toInt()
                )
            }

            p.y += 1f
        }

        p.x += 1f
        p.y = bboxMin.y
    }
}