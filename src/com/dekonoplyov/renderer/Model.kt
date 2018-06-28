package com.dekonoplyov.renderer

import com.curiouscreature.kotlin.math.*
import com.dekonoplyov.renderer.util.*
import java.io.File
import java.util.*

class Model(filename: String) {
    val vertices = ArrayList<Float3>()
    val faces = ArrayList<ArrayList<IntArray>>()
    val textures = ArrayList<Float3>()
    val norms = ArrayList<Float3>()

    var textureMap: PixelImage? = null
    var normalMap: PixelImage? = null
    var specMap: PixelImage? = null

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

    fun renderWire(image: PixelImage, color: Int) {
        for (face in faces) {
            for (i in 0..2) {
                val from = vertices[face[i][0]]
                val to = vertices[face[(i + 1) % 3][0]]
                val x0 = ((from.x + 1.0) * image.width / 2.0).toInt()
                val y0 = ((from.y + 1.0) * image.height / 2.0).toInt()
                val x1 = ((to.x + 1.0) * image.width / 2.0).toInt()
                val y1 = ((to.y + 1.0) * image.height / 2.0).toInt()
                line(x0, y0, x1, y1, color, image)
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
}