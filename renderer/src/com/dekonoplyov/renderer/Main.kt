package com.dekonoplyov.renderer

import com.dekonoplyov.renderer.util.PixelImage

fun draw_african_head() {
    val model = Model("model/african_head/african_head.obj")
    model.textureMap = PixelImage.fromTGA("model/african_head/african_head_diffuse.tga")
    model.normalMap = PixelImage.fromTGA("model/african_head/african_head_nm.tga")
    model.specMap = PixelImage.fromTGA("model/african_head/african_head_spec.tga")
    // rendering in flipped coordinates
    model.textureMap?.flipVertical()
    model.normalMap?.flipVertical()
    model.specMap?.flipVertical()

    val image = drawModel(model)
    image.flipVertical()
    image.save("output.png")
}

fun main(args: Array<String>) {
    draw_african_head()
}