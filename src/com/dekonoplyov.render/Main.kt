package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.avsievich.util.GREEN
import com.avsievich.util.RED
import com.avsievich.util.YELLOW
import com.curiouscreature.kotlin.math.Float3
import com.dekonoplyov.render.util.TGAReader
import com.dekonoplyov.render.util.ZBuffer
import com.dekonoplyov.render.util.triangle

fun draw_african_head() {
    val width = 800
    val height = 800
    val image = JavaImage(width, height, false, true)
    val model = Model("model/african_head/african_head.obj")
    val texture = TGAReader.getImage("model/african_head/african_head_diffuse.tga")
    model.renderTextures(image, texture)
    image.save("output.png")
}

fun main(args: Array<String>) {
    draw_african_head()

//    val image = JavaImage(100, 100, false, true)
//    val zBuffer = ZBuffer(image.width, image.height)
//
//    image.triangle(Float3(90f, 10f, 0.5f), Float3(90f, 80f, 0.5f), Float3(10f, 50f, 0f),
//            zBuffer, YELLOW)
//    image.triangle(Float3(10f, 10f, 0.5f), Float3(10f, 80f, 0.5f), Float3(90f, 50f, 0f),
//            zBuffer, RED)
//    image.triangle(Float3(30f, 90f, 0.25f), Float3(80f, 80f, 0.25f), Float3(50f, 0f, 0.25f),
//            zBuffer, GREEN)
//    image.save("output.png")
}