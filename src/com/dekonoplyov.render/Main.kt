package com.dekonoplev.render

import com.avsievich.image.JavaImage
import com.avsievich.util.*
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.cross
import com.dekonoplev.render.util.Int2
import kotlin.math.abs
import java.awt.Color.white
import kotlin.math.max
import kotlin.math.min

fun line(fromX: Int, fromY: Int, toX: Int, toY: Int, image: JavaImage, color: Int) {
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
            image[y, x] = color
        } else {
            image[x, y] = color
        }

        error2 += derror2
        if (error2 > dx) {
            y += if (y1 > y0) 1 else -1
            error2 -= dx * 2
        }
    }
}



fun pointInTrinange(t0: Int2, t1: Int2, t2: Int2, p: Int2): Boolean {
    val barycentric = cross(
            Float3(t2.x - t0.x, t1.x - t0.x, t0.x - p.x),
            Float3(t2.y - t0.y, t1.y - t0.y, t0.y - p.y))

    if (abs(barycentric.z) < 1f) {
        return false // triangle is degenerate
    }

    return 1 - (barycentric.x + barycentric.y) / barycentric.z >= 0f &&
            barycentric.y / barycentric.z >= 0f &&
            barycentric.x / barycentric.z >= 0f
}

fun triangle(t0: Int2, t1: Int2, t2: Int2, image: JavaImage, color: Int) {
    val bboxMin = Int2(image.width - 1, image.height - 1)
    val bboxMax = Int2(0, 0)
    for (t in arrayOf(t0, t1, t2)) {
        bboxMin.x = max(0, min(bboxMin.x, t.x))
        bboxMin.y = max(0, min(bboxMin.y, t.y))
        bboxMax.x = min(image.width - 1, max(bboxMax.x, t.x))
        bboxMax.y = min(image.height - 1, max(bboxMax.y, t.y))
    }

    for (x in bboxMin.x..bboxMax.x) {
        for (y in bboxMin.y..bboxMax.y) {
            if (pointInTrinange(t0, t1, t2, Int2(x, y))) {
                image.set(x, y, color)
            }
        }
    }
}

fun draw_african_head() {
    val width = 800
    val height = 800
    val image = JavaImage(width, height, false, true)
    val model = Model("model/african_head/african_head.obj")
    model.draw(image, RED)
    image.save("african_head.png")
}

fun main(args: Array<String>) {
    draw_african_head()
    val width = 200
    val height = 200
    val image = JavaImage(width, height, false, true)
    triangle(Int2(10, 70), Int2(50, 160), Int2(70, 80), image, RED)
    triangle(Int2(180, 50), Int2(150, 1), Int2(70, 180), image, WHITE)
    triangle(Int2(180, 150), Int2(120, 160), Int2(130, 180), image, GREEN)
    image.save("output.png")
}