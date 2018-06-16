package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.dekonoplyov.render.util.TGAReader

fun draw_african_head() {
    val width = 800
    val height = 800
    val image = JavaImage(width, height, false, true)
    val model = Model("model/african_head/african_head.obj")
    val texture = TGAReader.getImage("model/african_head/african_head_diffuse.tga")
    model.render(image, texture)
    image.save("results/lesson4/african_head.png")
}

fun main(args: Array<String>) {
    draw_african_head()
}