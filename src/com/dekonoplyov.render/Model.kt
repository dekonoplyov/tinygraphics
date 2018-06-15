package com.dekonoplyov.render

import com.avsievich.image.JavaImage
import com.avsievich.util.*
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
    val facesVert = ArrayList<IntArray>()
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
                    val faceVert = IntArray(3)
                    val face = ArrayList<IntArray>()
                    var cnt = 0
                    while (reader.tokenizer().hasMoreTokens()) {
                        val tokenizer = StringTokenizer(reader.tokenizer().nextToken(), "/")
                        val triplet = IntArray(3)
                        triplet[0] = tokenizer.nextToken().toInt() - 1
                        triplet[1] = tokenizer.nextToken().toInt() - 1
                        triplet[2] = tokenizer.nextToken().toInt() - 1
                        face.add(triplet)
                        faceVert[cnt++] = triplet[0]
                    }
                    faces.add(face)
                    facesVert.add(faceVert)
                }
            }
        }

        reader.close()

        println("v# ${verts.size} f# ${facesVert.size}")
    }

    fun renderLines(image: JavaImage, color: Int) {
        for (faceVert in facesVert) {
            for (i in 0..2) {
                val from = verts[faceVert[i]]
                val to = verts[faceVert[(i + 1) % 3]]
                val x0 = ((from.x + 1.0) * image.width / 2.0).toInt()
                val y0 = ((from.y + 1.0) * image.height / 2.0).toInt()
                val x1 = ((to.x + 1.0) * image.width / 2.0).toInt()
                val y1 = ((to.y + 1.0) * image.height / 2.0).toInt()
                image.line(x0, y0, x1, y1, color)
            }
        }
    }

    fun renderTriangles(image: JavaImage) {
        val lightDirection = Float3(0, 0, -1)

        for (faceVert in facesVert) {
            val screenCoords = Array(3) { IntArray(2) }
            val worldCoords = Array(3) { Float3() }
            for (i in 0..2) {
                val v = verts[faceVert[i]]
                screenCoords[i][0] = ((v.x + 1.0f) * image.width / 2.0f).toInt()
                screenCoords[i][1] = ((v.y + 1.0f) * image.height / 2.0f).toInt()
                worldCoords[i] = v
            }

            val normalToTriangle = normalize(cross(worldCoords[2] - worldCoords[0], worldCoords[1] - worldCoords[0]))
            val intesity = dot(normalToTriangle, lightDirection)

            if (intesity > 0) {
                image.triangle(screenCoords[0][0], screenCoords[0][1],
                        screenCoords[1][0], screenCoords[1][1],
                        screenCoords[2][0], screenCoords[2][1],
                        argb((255 * intesity).toInt(), (255 * intesity).toInt(),
                                (255 * intesity).toInt(), (255 * intesity).toInt()))
            }
        }
    }

    fun renderWithZBuffer(image: JavaImage, color: Int) {
        val zBuffer = ZBuffer(image.width, image.height)
        val lightDirection = Float3(0, 0, -1)

        for (faceVert in facesVert) {
            val screenCoords = Array(3) { Float3() }
            val worldCoords = Array(3) { Float3() }
            for (i in 0..2) {
                val v = verts[faceVert[i]]
                screenCoords[i].x = ((v.x + 1.0f) * image.width / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].y = ((v.y + 1.0f) * image.height / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].z = v.z
                worldCoords[i] = v
            }

            val normalToTriangle = normalize(cross(worldCoords[2] - worldCoords[0], worldCoords[1] - worldCoords[0]))
            val intesity = dot(normalToTriangle, lightDirection)

            if (intesity > 0) {
                image.triangle(screenCoords[0], screenCoords[1], screenCoords[2], zBuffer,
                        argb((255 * intesity).toInt(), (255 * intesity).toInt(),
                                (255 * intesity).toInt(), (255 * intesity).toInt()))
            }
        }
    }

    fun renderTextures(image: JavaImage, textureMap: BufferedImage) {
        val zBuffer = ZBuffer(image.width, image.height)
        val lightDirection = Float3(0, 0, -1)

        for (face in faces) {
            val screenCoords = Array(3) { Float3() }
            val worldCoords = Array(3) { Float3() }
            val textureCoords = Array(3) { Float3() }
            for (i in 0..2) {
                val v = verts[face[i][0]]
                screenCoords[i].x = ((v.x + 1.0f) * image.width / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].y = ((v.y + 1.0f) * image.height / 2.0f + 0.5f).roundToInt().toFloat()
                screenCoords[i].z = v.z
                worldCoords[i] = v
                textureCoords[i] = textureVerts[face[i][1]]
            }

            val normalToTriangle = normalize(cross(worldCoords[2] - worldCoords[0], worldCoords[1] - worldCoords[0]))
            val intesity = dot(normalToTriangle, lightDirection)

            if (intesity > 0) {
                image.triangle(screenCoords[0], screenCoords[1], screenCoords[2], zBuffer,
                        textureCoords[0], textureCoords[1], textureCoords[2], textureMap)
            }
        }
    }
}