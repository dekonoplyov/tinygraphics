package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.render.util.FastReader
import com.dekonoplyov.render.util.ZBuffer
import com.dekonoplyov.render.util.line
import com.dekonoplyov.render.util.triangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import kotlin.math.roundToInt

class Model(filename: String) {
    val verts = ArrayList<Float3>()
    val faces = ArrayList<ArrayList<IntArray>>()
    val textureVerts = ArrayList<Float3>()

    init {
        parse(filename)
    }

    private fun parse(filename: String) {
        val reader = FastReader(File(filename).inputStream())

        while (reader.hasNext()) {
            when (reader.string()) {
                "v" -> verts.add(reader.float3())
                "vn" -> reader.float3() // skip vn line
                "vt" -> textureVerts.add(reader.float3())
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

        println("verts# ${verts.size} faces# ${faces.size} textures# ${textureVerts.size}")
    }

    fun renderWire(image: JavaImage, color: Int) {
        for (face in faces) {
            for (i in 0..2) {
                val from = verts[face[i][0]]
                val to = verts[face[(i + 1) % 3][0]]
                val x0 = ((from.x + 1.0) * image.width / 2.0).toInt()
                val y0 = ((from.y + 1.0) * image.height / 2.0).toInt()
                val x1 = ((to.x + 1.0) * image.width / 2.0).toInt()
                val y1 = ((to.y + 1.0) * image.height / 2.0).toInt()
                image.line(x0, y0, x1, y1, color)
            }
        }
    }

    fun render(image: JavaImage, textureMap: BufferedImage) {
        val zBuffer = ZBuffer(image.width, image.height)

        val lightDirection = Float3(0, 0, -1)
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
                val v = (projection * verts[face[i][0]].toFloat4()).toFloat3()
                screenCoords[i].x = ((v.x + 1.0f) * image.width / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].y = ((v.y + 1.0f) * image.height / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].z = v.z

                worldCoords[i] = v
                textureCoords[i] = textureVerts[face[i][1]]
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