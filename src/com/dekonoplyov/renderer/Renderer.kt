package com.dekonoplyov.renderer

import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.renderer.util.*
import java.awt.image.BufferedImage
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

class Renderer(val width: Int, val height: Int) {
    private val depth = 255
    val image = PixelImage(width, height,false)
    private val zBuffer = ZBuffer(width, height)

    val lightDirection = normalize(Float3(1f, -1f, 1f))
    val eye = Float3(1f, 1f, 3f)
    val center = Float3(0f, 0f, 0f)
    val up = Float3(0f, 1f, 0f)

    val projection = projection(eye, center)
    val view = view(eye, center, up)
    val viewport = viewport(width / 8, height / 8, 3 * width / 4, 3 * height / 4)

    val transform = viewport * projection * view

    fun render(model: Model, textureMap: BufferedImage) {
        for (face in model.faces) {
            drawFace(model, textureMap, face)
        }
    }

    private fun drawFace(model: Model, textureMap: BufferedImage, face: ArrayList<IntArray>) {
        val a = screenCoordinates(model.vertices[face[0][0]])
        val b = screenCoordinates(model.vertices[face[1][0]])
        val c = screenCoordinates(model.vertices[face[2][0]])

        val faceTextures = model.getTextureVerts(face)
        val intensities = getIntensities(model, face)

        val p = Float3()

        for (x in getXRange(a, b, c)) {
            for (y in getYRange(a, b, c)) {
                p.x = x.toFloat()
                p.y = y.toFloat()

                val bc = barycentric(a, b, c, p)

                if (bc.x < 0f || bc.y < 0f || bc.z < 0f) {
                    continue
                }

                // bc.x corresponds to a, because of argument order in barycentric call
                p.z = a.z * bc.x + b.z * bc.y + c.z * bc.z

                if (zBuffer[p.x, p.y] < p.z) {
                    zBuffer[p.x, p.y] = p.z

                    val textureCoordinates =
                            faceTextures[0] * bc.x + faceTextures[1] * bc.y + faceTextures[2] * bc.z
                    textureCoordinates.x *= textureMap.width
                    textureCoordinates.y *= textureMap.height

                    val textureColor = textureMap.getRGB(textureCoordinates.x.toInt(), textureCoordinates.y.toInt())

                    val intensity = clamp(intensities[0] * bc.x + intensities[1] * bc.y + intensities[2] * bc.z, 0f,1f)
                    image[x, y] = rgb(
                            (textureColor.red() * intensity).toInt(),
                            (textureColor.green() * intensity).toInt(),
                            (textureColor.blue() * intensity).toInt())
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

    private fun getIntensities(model: Model, face: ArrayList<IntArray>): FloatArray {
        val normals = model.getNormales(face)
        return floatArrayOf(
                dot(normals[0], lightDirection),
                dot(normals[1], lightDirection),
                dot(normals[2], lightDirection))
    }

    private fun projection(eye: Float3, center: Float3): Mat4 {
        val m = Mat4()
        m[2, 3] = -1 / distance(eye, center)
        return m
    }

    private fun view(eye: Float3, center: Float3, up: Float3): Mat4 {
        val z = normalize(eye - center)
        val x = normalize(cross(up, z))
        val y = normalize(cross(z, x))
        val res = Mat4()
        for (i in 0..2) {
            res[i][0] = x[i]
            res[i][1] = y[i]
            res[i][2] = z[i]
            res[3][i] = -center[i]
        }
        return res
    }

    //[x,x+w]*[y,y+h]*[0,d]
    private fun viewport(x: Int, y: Int, w: Int, h: Int): Mat4 {
        val m = Mat4()
        m[3][0] = x + w / 2.0f
        m[3][1] = y + h / 2.0f
        m[3][2] = depth / 2.0f

        m[0][0] = w / 2.0f
        m[1][1] = h / 2.0f
        m[2][2] = depth / 2.0f
        return m
    }

    private fun screenCoordinates(v: Float3): Float3 {
        return toFloat3(transform * toFloat4(v))
    }

    private fun toFloat4(v: Float3): Float4 {
        return Float4(v, 1f)
    }

    private fun toFloat3(v: Float4): Float3 {
        return v.xyz / v.w
    }
}