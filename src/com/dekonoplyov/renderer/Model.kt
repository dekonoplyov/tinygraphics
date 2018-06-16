package com.dekonoplyov.renderer

import com.avsievich.image.JavaImage
import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.renderer.util.FastReader
import com.dekonoplyov.renderer.util.ZBuffer
import com.dekonoplyov.renderer.util.line
import com.dekonoplyov.renderer.util.triangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class Model(filename: String) {
    val vertices = ArrayList<Float3>()
    val faces = ArrayList<ArrayList<IntArray>>()
    val textures = ArrayList<Float3>()
    val norms = ArrayList<Float3>()

    init {
        parse(filename)
    }

    private fun parse(filename: String) {
        val reader = FastReader(File(filename).inputStream())

        while (reader.hasNext()) {
            when (reader.string()) {
                "v" -> vertices.add(reader.float3())
                "vn" -> norms.add(reader.float3())
                "vt" -> textures.add(reader.float3())
                "f" -> {
                    val face = ArrayList<IntArray>()
                    while (reader.tokenizer().hasMoreTokens()) {
                        val tokenizer = StringTokenizer(reader.tokenizer().nextToken(), "/")
                        val triplet = IntArray(3)
                        triplet[0] = tokenizer.nextToken().toInt() - 1
                        triplet[1] = tokenizer.nextToken().toInt() - 1
                        triplet[2] = tokenizer.nextToken().toInt() - 1
                        face.add(triplet)
                    }
                    faces.add(face)
                }
            }
        }

        reader.close()

        println("vertices# ${vertices.size} faces# ${faces.size} textures# ${textures.size} normals# ${norms.size}")
    }

    fun renderWire(image: JavaImage, color: Int) {
        for (face in faces) {
            for (i in 0..2) {
                val from = vertices[face[i][0]]
                val to = vertices[face[(i + 1) % 3][0]]
                val x0 = ((from.x + 1.0) * image.width / 2.0).toInt()
                val y0 = ((from.y + 1.0) * image.height / 2.0).toInt()
                val x1 = ((to.x + 1.0) * image.width / 2.0).toInt()
                val y1 = ((to.y + 1.0) * image.height / 2.0).toInt()
                image.line(x0, y0, x1, y1, color)
            }
        }
    }

    fun getTextureVerts(face: ArrayList<IntArray>): ArrayList<Float3> {
        return arrayListOf(
                textures[face[0][1]],
                textures[face[1][1]],
                textures[face[2][1]]
        )
    }

    fun getNormales(face: ArrayList<IntArray>): ArrayList<Float3> {
        return arrayListOf(
                norms[face[0][0]],
                norms[face[1][0]],
                norms[face[2][0]]
        )
    }

    fun render(image: JavaImage, textureMap: BufferedImage) {
        val zBuffer = ZBuffer(image.width, image.height)

        val lightDirection = normalize(Float3(-1f, -1f, -1f))
        val camera = Float3(0f, 0f, 3f)

        fun Float3.toFloat4(): Float4 {
            return Float4(this, 1f)
        }

        fun Float4.toFloat3(): Float3 {
            return this.xyz.div(this.w)
        }

        val projection = Mat4()
        projection[2, 3] = -1 / camera.z

        for (face in faces) {
            val screenCoords = Array(3) { Float3() }
            val worldCoords = Array(3) { Float3() }
            val textureCoords = Array(3) { Float3() }

            for (i in 0..2) {
                val v = (projection * vertices[face[i][0]].toFloat4()).toFloat3()
                screenCoords[i].x = ((v.x + 1.0f) * image.width / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].y = ((v.y + 1.0f) * image.height / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].z = v.z

                worldCoords[i] = v
                textureCoords[i] = textures[face[i][1]]
            }

            val normalToTriangle = normalize(
                    cross(worldCoords[2] - worldCoords[0], worldCoords[1] - worldCoords[0])
            )
            val intesity = dot(normalToTriangle, lightDirection)

            if (intesity > 0) {
                image.triangle(screenCoords[0], screenCoords[1], screenCoords[2], zBuffer,
                        textureCoords[0], textureCoords[1], textureCoords[2], textureMap, intesity)
            }
        }
    }
}