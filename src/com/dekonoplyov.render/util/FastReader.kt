package com.dekonoplyov.render.util

import com.curiouscreature.kotlin.math.Float2
import com.curiouscreature.kotlin.math.Float3
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class FastReader(stream: InputStream) {
    private val reader = BufferedReader(InputStreamReader(stream))
    private var tokenizer: StringTokenizer? = null

    fun hasNext(): Boolean {
        if (tokenizer != null && tokenizer!!.hasMoreTokens()) {
            return true
        }

        var nextLine = reader.readLine()
        while (nextLine != null) {
            tokenizer = StringTokenizer(nextLine)
            if (tokenizer!!.hasMoreTokens()) {
                return true
            } else {
                nextLine = reader.readLine()
            }
        }

        return false
    }

    fun tokenizer() = tokenizer!!

    fun string(): String {
        while (tokenizer == null || !tokenizer!!.hasMoreTokens()) {
            try {
                tokenizer = StringTokenizer(reader.readLine())
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        return tokenizer!!.nextToken()
    }

    fun int() = string().toInt()
    fun float() = string().toFloat()
    fun float2() = Float2(float(), float())
    fun float3() = Float3(float(), float(), float())

    fun close() {
        reader.close()
    }
}