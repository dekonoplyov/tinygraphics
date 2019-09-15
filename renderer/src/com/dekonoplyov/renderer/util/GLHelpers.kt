package com.dekonoplyov.renderer.util

import com.curiouscreature.kotlin.math.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

fun pointTo4D(p: Float3): Float4 {
    return Float4(p, 1f)
}

fun pointTo3D(p: Float4): Float3 {
    return p.xyz / p.w
}

fun vectorTo4D(v: Float3): Float4 {
    return Float4(v, 0f)
}

fun vectorTo3D(v: Float4): Float3 {
    return v.xyz
}

fun colorToNormal(c: Int): Float3 {
    return Float3(
            c.red() / 255f * 2 - 1,
            c.green() / 255f * 2 - 1,
            c.blue() / 255f * 2 - 1)
}

fun barycentric(a: Float3, b: Float3, c: Float3, p: Float3): Float3 {
    val cross = cross(
            Float3(c.x - a.x, b.x - a.x, a.x - p.x),
            Float3(c.y - a.y, b.y - a.y, a.y - p.y))

    if (abs(cross.z) < 1e-2f) {
        return Float3(-1f, 1f, 1f)
    }

    return Float3(
            1 - (cross.x + cross.y) / cross.z,
            cross.y / cross.z,
            cross.x / cross.z)
}


fun getXRange(a: Float3, b: Float3, c: Float3): IntRange {
    return min(a.x, min(b.x, c.x)).toInt()..(max(a.x, max(b.x, c.x)).toInt() + 1)
}

fun getYRange(a: Float3, b: Float3, c: Float3): IntRange {
    return min(a.y, min(b.y, c.y)).toInt()..(max(a.y, max(b.y, c.y)).toInt() + 1)
}