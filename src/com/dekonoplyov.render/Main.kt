package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.avsievich.util.*
import com.dekonoplyov.render.util.line

fun draw_african_head() {
    val width = 800
    val height = 800
    val lesson1 = JavaImage(width, height, false, true)
    val lesson2 = JavaImage(width, height, false, true)
    val model = Model("model/african_head/african_head.obj")
    model.renderLines(lesson1, RED)
    model.renderTriangles(lesson2)
    lesson1.save("results/lesson1/wire_african_head.png")
    lesson2.save("results/lesson2/african_head.png")
}

fun rasterize(x0: Int, y0: Int, x1: Int, y1: Int, image: JavaImage, color: Int, ybuffer: IntArray) {
    var px0 = x0
    var py0 = y0
    var px1 = x1
    var py1 = y1

    if (x0 > x1) {
        px0 = px1.also { px1 = px0 }
        py0 = py1.also { py1 = py0 }
    }

    for (x in px0..px1) {
        val t = (x - px0) / (px1 - px0).toFloat()
        val y = (py0 * (1 - t) + py1 * t).toInt()
        if (ybuffer[x] < y) {
            ybuffer[x] = y
            image[x, 0] = color
        }

    }
}

fun main(args: Array<String>) {
    val width = 800
    val height = 500
    val scene = JavaImage(width, height, false, true)

    // scene "2d mesh"
    scene.line(20, 34, 744, 400, RED)
    scene.line(120, 434, 444, 400, GREEN)
    scene.line(330, 463, 594, 200, YELLOW)

    // screen line
    scene.line(10, 10, 790, 10, WHITE)

    scene.save("scene.png")


    val render = JavaImage(width, 16, false, true)
    val ybuffer = IntArray(width) { Int.MIN_VALUE }

    rasterize(20, 34, 744, 400, render, RED, ybuffer)
    rasterize(120, 434, 444, 400, render, GREEN, ybuffer)
    rasterize(330, 463, 594, 200, render, YELLOW, ybuffer)

    for (i in 0 until width) {
        for (j in 1..15) {
            render[i, j] = render[i, 0]
        }
    }
    render.save("render.png")

}