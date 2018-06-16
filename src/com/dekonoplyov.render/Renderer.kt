package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.avsievich.util.*
import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.render.util.ZBuffer
import com.dekonoplyov.render.util.barycentric
import java.awt.image.BufferedImage
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Renderer(val width: Int, val height: Int) {
    val image = JavaImage(width, height,false, true)
    val zBuffer = ZBuffer(width, height)
    val lightDirection = normalize(Float3(1f, 1f, 1f))
    val camera = Float3(0f, 0f, 3f)
    val projection = Mat4()

    init {
        projection[2, 3] = -1 / camera.z
    }

    fun triangle(model: Model, textureMap: BufferedImage, face: ArrayList<IntArray>) {
        val a = toScreenCoordinates(model.vertices[face[0][0]])
        val b = toScreenCoordinates(model.vertices[face[1][0]])
        val c = toScreenCoordinates(model.vertices[face[2][0]])

        val bboxMin = Float2(min(a.x, min(b.x, c.x)), min(a.y, min(b.y, c.y)))
        val bboxMax = Float2(max(a.x, max(b.x, c.x)), max(a.y, max(b.y, c.y)))

        val faceTextureVerts = model.getTextureVerts(face)
        val normals = model.getNormales(face)
        val intensities = arrayOf(
                dot(normals[0], lightDirection),
                dot(normals[1], lightDirection),
                dot(normals[2], lightDirection))

        val p = Float3(bboxMin.x, bboxMin.y)
        while (p.x in (bboxMin.x..bboxMax.x)) {
            while (p.y in (bboxMin.y..bboxMax.y)) {
                val bc = barycentric(a, b, c, p)

                if (bc.x < 0f || bc.y < 0f || bc.z < 0f) {
                    p.y += 1f
                    continue
                }

                p.z = a.z * bc.x + b.z * bc.y + c.z * bc.z
                // bc.x corresponds to a, because of argument order in barycentric call

                if (zBuffer[p.x, p.y] < p.z) {
                    zBuffer[p.x, p.y] = p.z

                    val textureCoordinates =
                            faceTextureVerts[0] * bc.x + faceTextureVerts[1] * bc.y + faceTextureVerts[2] * bc.z
                    textureCoordinates.x *= textureMap.width
                    textureCoordinates.y *= textureMap.height

                    val textureColor = textureMap.getRGB(textureCoordinates.x.toInt(), textureCoordinates.y.toInt())

                    val intensity = intensities[0] * bc.x + intensities[1] * bc.y + intensities[2] * bc.z
                    if (intensity > 0) {
                        image[p.x.toInt(), p.y.toInt()] = rgb(
                                (textureColor.red() * intensity).toInt(),
                                (textureColor.green() * intensity).toInt(),
                                (textureColor.blue() * intensity).toInt())
                    }
                }

                p.y += 1f
            }

            p.x += 1f
            p.y = bboxMin.y
        }
    }

    fun render(model: Model, textureMap: BufferedImage) {
        for (face in model.faces) {
            triangle(model, textureMap, face)
        }
    }

    private fun toScreenCoordinates(v: Float3): Float3 {
        val p = toFloat3(projection * toFloat4(v))
        p.x = ((p.x + 1.0f) * image.width / 2.0f + 0.5f).roundToInt().toFloat()
        p.y = ((p.y + 1.0f) * image.height / 2.0f + 0.5f).roundToInt().toFloat()
        p.z = p.z
        return p
    }

    private fun toFloat4(v: Float3): Float4 {
        return Float4(v, 1f)
    }

    private fun toFloat3(v: Float4): Float3 {
        return v.xyz / v.w
    }
}