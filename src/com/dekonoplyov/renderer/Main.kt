package com.dekonoplyov.renderer

import com.dekonoplyov.renderer.util.PixelImage
import com.dekonoplyov.renderer.util.TGAReader

fun draw_african_head() {
    val model = Model("model/african_head/african_head.obj")
}

fun main(args: Array<String>) {
//    draw_african_head()
    val image = PixelImage.fromTGA("model/african_head/african_head_nm.tga")
    image.save("output1.png")
}