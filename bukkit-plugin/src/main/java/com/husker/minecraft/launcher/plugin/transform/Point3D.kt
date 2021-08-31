package com.husker.minecraft.launcher.plugin.transform

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import kotlin.math.sqrt

data class Point3D(var x: Double, var y: Double, var z: Double){
    constructor(x: Int, y: Int, z: Int): this(x.toDouble(), y.toDouble(), z.toDouble())

    fun toLocation(world: World): Location = Location(world, x, y, z)
    fun toVector(destination: Point3D): Vector = Vector(destination.x - x, destination.y - y, destination.z - z)
    fun toVector(): Vector = Vector(x, y, z)
    fun mirroredByPoint(point: Point3D): Point3D = Point3D(2 * point.x - x, 2 * point.y - y, 2 * point.z - z)

    fun distance(point: Point3D): Double{
        val a = x - point.x
        val b = y - point.y
        val c = z - point.z
        return sqrt(a * a + b * b + c * c)
    }
}