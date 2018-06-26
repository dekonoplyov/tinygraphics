package com.dekonoplyov.renderer

import com.dekonoplyov.renderer.util.TGAReader

fun draw_african_head() {
    val width = 800
    val height = 800
    val model = Model("model/african_head/african_head.obj")
    val texture = TGAReader.getImage("model/african_head/african_head_diffuse.tga")
    val renderer = Renderer(width, height)
    renderer.render(model, texture)
    renderer.image.save("output.png")
}

fun main(args: Array<String>) {
//    draw_african_head()
    val model = Model("model/african_head/african_head.obj")
    val image = drawModel(model)
    image.save("output.png")
}