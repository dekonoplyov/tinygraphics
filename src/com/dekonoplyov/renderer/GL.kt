package com.dekonoplyov.renderer

import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.renderer.util.*
import java.lang.Math.pow
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

abstract class BaseShader {
    abstract fun vertex(face: ArrayList<IntArray>, vertIdx: Int): Float3
    abstract fun fragment(bar: Float3): Int
}

class Shader(private val model: Model) : BaseShader() {
    var uniformViewPort = Mat4()
    var uniformMVP = Mat4()
    var uniformMVPIT = Mat4()
    var uniformLightDir = Float3()
    private val varyingUV = Array(3) { Float3() }

    override fun vertex(face: ArrayList<IntArray>, vertIdx: Int): Float3 {
        varyingUV[vertIdx] = model.getTextureVerts(face)[vertIdx]
        return pointTo3D(uniformViewPort * pointTo4D(model.vertices[face[vertIdx][0]]))
    }

    override fun fragment(bar: Float3): Int {
        val textureCoords = varyingUV[0] * bar.x + varyingUV[1] * bar.y + varyingUV[2] * bar.z
        val u = (textureCoords.x * model.textureMap!!.width).toInt()
        val v = (textureCoords.y * model.textureMap!!.height).toInt()

        val n = normalize(vectorTo3D(uniformMVPIT * vectorTo4D(colorToNormal(model.normalMap!![u, v]))))
        val l = normalize(vectorTo3D(uniformMVP * vectorTo4D(uniformLightDir)))
        val r = clamp(normalize(n * dot(n, l) * 2f - l).z, 0f, 1f)

        val spec = pow(r.toDouble(), model.specMap!![u, v].red().toDouble()).toFloat()
        val intensity = clamp(dot(n, l), 0f,1f)

        return model.textureMap!![u, v].applyIntesity(intensity + 0.6f * spec)
    }
}

fun drawModel(model: Model): PixelImage {
    val width = 800
    val height = 800
    val image = PixelImage(width, height, false)
    val zBuffer = ZBuffer(width, height)

    val lightDirection = normalize(Float3(1f, -1f, 1f))
    val eye = Float3(1f, 1f, 3f)
    val center = Float3(0f, 0f, 0f)
    val up = Float3(0f, 1f, 0f)

    val projection = projection(eye, center)
    val view = viewModel(eye, center, up)
    val viewport = viewPort(width / 8, height / 8, 3 * width / 4, 3 * height / 4, 255)

    val shader = Shader(model)
    shader.uniformViewPort = viewport * projection * view
    shader.uniformMVP = projection * view
    shader.uniformMVPIT = transpose(inverse(shader.uniformMVP))
    shader.uniformLightDir = lightDirection

    for (face in model.faces) {
        val screenCoords = Array(3) { Float3() }
        for (i in 0..2) {
            screenCoords[i] = shader.vertex(face, i)
        }

        triangle(screenCoords, shader, image, zBuffer)
    }

    return image
}

fun triangle(screenCoordinates: Array<Float3>, shader: BaseShader, image: PixelImage, zBuffer: ZBuffer) {
    for (x in getXRange(screenCoordinates[0], screenCoordinates[1], screenCoordinates[2])) {
        for (y in getYRange(screenCoordinates[0], screenCoordinates[1], screenCoordinates[2])) {
            val p = Float3(x.toFloat(), y.toFloat(), 0f)
            val bar = barycentric(screenCoordinates[0], screenCoordinates[1], screenCoordinates[2], p)

            if (bar.x < 0f || bar.y < 0f || bar.z < 0f) {
                continue
            }

            p.z = screenCoordinates[0].z * bar.x + screenCoordinates[1].z * bar.y + screenCoordinates[2].z * bar.z

            if (zBuffer[x, y] < p.z) {
                zBuffer[x, y] = p.z
                image[x, y] = shader.fragment(bar)
            }
        }
    }
}

fun line(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Int, image: PixelImage) {
    var x0 = fromX
    var y0 = fromY
    var x1 = toX
    var y1 = toY

    var steep = false

    if (kotlin.math.abs(x0 - x1) < kotlin.math.abs(y0 - y1)) {
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
    val derror2 = kotlin.math.abs(dy) * 2
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
