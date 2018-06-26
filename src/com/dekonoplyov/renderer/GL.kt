package com.dekonoplyov.renderer

import com.avsievich.image.JavaImage
import com.avsievich.util.*
import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.renderer.util.ZBuffer
import com.dekonoplyov.renderer.util.barycentric
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

abstract class BaseShader {
    abstract fun vertex(face: ArrayList<IntArray>, vertIdx: Int): Float3
    abstract fun fragment(bar: Float3, color: Int): Int
}

fun viewModel(eye: Float3, center: Float3, up: Float3): Mat4 {
    val m = Mat4()
    val z = normalize(eye - center)
    val x = normalize(cross(up, z))
    val y = normalize(cross(z, x))
    for (i in 0..2) {
        m[i][0] = x[i]
        m[i][1] = y[i]
        m[i][2] = z[i]
        m[3][i] = -center[i]
    }
    return m
}

fun projection(eye: Float3, center: Float3): Mat4 {
    val m = Mat4()
    m[2, 3] = -1 / distance(eye, center)
    return m
}

fun viewPort(x: Int, y: Int, w: Int, h: Int, depth: Int): Mat4 {
    val m = Mat4()
    m[3][0] = x + w / 2.0f
    m[3][1] = y + h / 2.0f
    m[3][2] = depth / 2.0f

    m[0][0] = w / 2.0f
    m[1][1] = h / 2.0f
    m[2][2] = depth / 2.0f
    return m
}

fun toFloat4(v: Float3): Float4 {
    return Float4(v, 1f)
}

fun toFloat3(v: Float4): Float3 {
    return v.xyz / v.w
}

class GouraudShader(val model: Model) : BaseShader() {
    val varyingIntensity = Float3()
    var uniformMVP = Mat4()
    var uniformLightDir = Float3()

    override fun vertex(face: ArrayList<IntArray>, vertIdx: Int): Float3 {
        varyingIntensity[vertIdx] = dot(model.getNormales(face)[vertIdx], uniformLightDir)
        return toFloat3(uniformMVP * toFloat4(model.vertices[face[vertIdx][0]]))
    }

    override fun fragment(bar: Float3, color: Int): Int {
        val intensity = clamp(dot(varyingIntensity, bar), 0f,1f)
        if (intensity>.85) return color.applyIntesity(1f)
        else if (intensity>.60) return color.applyIntesity(0.8f)
        else if (intensity>.45) return color.applyIntesity(0.60f)
        else if (intensity>.30) return color.applyIntesity(0.45f)
        else if (intensity>.15) return color.applyIntesity(0.3f)
        else return color.applyIntesity(0f)
    }
}

fun drawModel(model: Model): JavaImage {
    val width = 800
    val height = 800
    val image = JavaImage(width, height, false, true)
    val zBuffer = ZBuffer(width, height)

    val lightDirection = normalize(Float3(1f, 1f, 1f))
    val eye = Float3(1f, -1f, 3f)
    val center = Float3(0f, 0f, 0f)
    val up = Float3(0f, 1f, 0f)

    val projection = projection(eye, center)
    val view = viewModel(eye, center, up)
    val viewport = viewPort(width / 8, height / 8, 3 * width / 4, 3 * height / 4, 255)

    val shader = GouraudShader(model)
    shader.uniformMVP = viewport * projection * view
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

fun triangle(screenCoords: Array<Float3>, shader: BaseShader,
             image: JavaImage, zBuffer: ZBuffer) {
    for (x in getXRange(screenCoords[0], screenCoords[1], screenCoords[2])) {
        for (y in getYRange(screenCoords[0], screenCoords[1], screenCoords[2])) {
            val p = Float3(x.toFloat(), y.toFloat(), 0f)
            val bar = barycentric(screenCoords[0], screenCoords[1], screenCoords[2], p)

            if (bar.x < 0f || bar.y < 0f || bar.z < 0f) {
                continue
            }

            p.z = screenCoords[0].z * bar.x + screenCoords[1].z * bar.y + screenCoords[2].z * bar.z

            if (zBuffer[p.x, p.y] < p.z) {
                zBuffer[p.x, p.y] = p.z
                image[x, y] = shader.fragment(bar, RED)
            }
        }
    }
}

private fun getXRange(a: Float3, b: Float3, c: Float3): IntRange {
    return min(a.x, min(b.x, c.x)).toInt()..(max(a.x, max(b.x, c.x)).toInt() + 1)
}

private fun getYRange(a: Float3, b: Float3, c: Float3): IntRange {
    return min(a.y, min(b.y, c.y)).toInt()..(max(a.y, max(b.y, c.y)).toInt() + 1)
}