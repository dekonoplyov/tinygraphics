package com.dekonoplev.render

import com.avsievich.image.JavaImage
import com.avsievich.util.RED
import com.avsievich.util.argb
import com.curiouscreature.kotlin.math.Float3
import com.curiouscreature.kotlin.math.cross
import com.curiouscreature.kotlin.math.dot
import com.curiouscreature.kotlin.math.normalize
import com.dekonoplev.render.util.FastReader
import com.dekonoplev.render.util.Int2
import java.io.File
import java.util.*

class Model(filename: String) {
    var verts = ArrayList<Float3>()
    var faces = ArrayList<ArrayList<IntArray>>()
    var facesVert = ArrayList<IntArray>()

    init {
        parse(filename)
    }

    private fun parse(filename: String) {
        val reader = FastReader(File(filename).inputStream())

        while (reader.hasNext()) {
            when (reader.string()) {
                "v" -> verts.add(reader.float3())
                "vn" -> reader.float3()
                "vt" -> reader.float2()
                "f" -> {
                    val faceVert = IntArray(3)
                    var cnt = 0
                    while (reader.tokenizer().hasMoreTokens()) {
                        val tokenizer = StringTokenizer(reader.tokenizer().nextToken(), "/")
                        faceVert[cnt++] = tokenizer.nextToken().toInt() - 1
                    }
                    facesVert.add(faceVert)
                }
            }
        }

        reader.close()

        println("v# ${verts.size} f# ${facesVert.size}")
    }

    fun draw(image: JavaImage, color: Int) {
        fun ClosedRange<Int>.random() =
                Random().nextInt(endInclusive - start) +  start

        val lightDirection = Float3(0, 0, -1)

        for (faceVert in facesVert) {
            val screenCoords = Array(3, { Int2() })
            val worldCoords = Array(3,  { Float3() })
            for (i in 0..2) {
                val v = verts[faceVert[i]]
                screenCoords[i].x = ((v.x + 1.0) * image.width / 2.0).toInt()
                screenCoords[i].y = ((v.y + 1.0) * image.height / 2.0).toInt()
                worldCoords[i] = v
            }

            val normalToTriangle = normalize(cross(worldCoords[2] - worldCoords[0], worldCoords[1] - worldCoords[0]))
            val intesity = dot(normalToTriangle, lightDirection)

            if (intesity > 0) {
                val c = (intesity * 255).toInt()
                triangle(screenCoords[0], screenCoords[1], screenCoords[2], image,
                        argb((0..255).random(), (0..255).random(), (0..255).random(), c))
            }
        }
    }
}