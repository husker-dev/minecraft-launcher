package com.husker.minecraft.launcher.app.opengl

import kotlin.math.sqrt

open class Point3D(var x: Double, var y: Double, var z: Double): Cloneable{
    companion object{
        @JvmStatic val ZERO = Point3D(0.0, 0.0, 0.0)
    }

    constructor(x: Int, y: Int, z: Int): this(x.toDouble(), y.toDouble(), z.toDouble())

    fun distance(x1: Double, y1: Double, z1: Double): Double {
        val a: Double = x - x1
        val b: Double = y - y1
        val c: Double = z - z1
        return sqrt(a * a + b * b + c * c)
    }

    fun distance(point: Point3D): Double {
        return distance(point.x, point.y, point.z)
    }

    override fun toString(): String {
        return "Point3D($x, $y, $z)"
    }

    public override fun clone(): Point3D{
        val obj = super.clone() as Point3D
        obj.x = x
        obj.y = y
        obj.z = z
        return obj
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point3D

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }
}