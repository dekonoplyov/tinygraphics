package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.avsievich.util.*
import com.dekonoplyov.render.util.triangle

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

fun main(args: Array<String>) {
    draw_african_head()
    val width = 200
    val height = 200
    val image = JavaImage(width, height, false, true)
    image.triangle(10, 70, 50, 160, 70, 80, RED)
    image.triangle(180, 50, 150, 1, 70, 180, WHITE)
    image.triangle(180, 150, 120, 160, 130, 180, GREEN)
    image.save("output.png")
}