package com.husker.minecraft.launcher.plugin.transform

import kotlin.math.cos
import kotlin.math.sin

class Rotation(
    var angle: Double,
    var pivotX: Double,
    var pivotY: Double,
    var pivotZ: Double,
    var axis: Point3D = Z_AXIS) {

    companion object{
        val X_AXIS = Point3D(1, 0, 0)
        val Y_AXIS = Point3D(0, 1, 0)
        val Z_AXIS = Point3D(0, 0, 1)
    }

    constructor(angle: Double, axis: Point3D): this(angle, 0.0, 0.0, 0.0, axis)
    constructor(angle: Double, pivot: Point3D, axis: Point3D): this(angle, pivot.x, pivot.y, pivot.z, axis)

    private var mxx = 0.0
    private var mxy = 0.0
    private var mxz = 0.0
    private var tx = 0.0
    private var myx = 0.0
    private var myy = 0.0
    private var myz = 0.0
    private var ty = 0.0
    private var mzx = 0.0
    private var mzy = 0.0
    private var mzz = 0.0
    private var tz = 0.0

    fun transform(point: Point3D): Point3D{
        updateMatrix()

        return Point3D(
                mxx * point.x + mxy * point.y + mxz * point.z + tx,
                myx * point.x + myy * point.y + myz * point.z + ty,
                mzx * point.x + mzy * point.y + mzz * point.z + tz)
    }

    private fun updateMatrix(){
        val sin = sin(Math.toRadians(angle))
        val cos = cos(Math.toRadians(angle))
        val axisX = axis.x
        val axisY = axis.y
        val axisZ = axis.z

        mxx = cos + axisX * axisX * (1 - cos)
        mxy = axisX * axisY * (1 - cos) - axisZ * sin
        mxz = axisX * axisZ * (1 - cos) + axisY * sin
        tx = pivotX * (1 - mxx) - pivotY * mxy - pivotZ * mxz

        myx = axisY * axisX * (1 - cos) + axisZ * sin
        myy = cos + axisY * axisY * (1 - cos)
        myz = axisY * axisZ * (1 - cos) - axisX * sin
        ty = pivotY * (1 - myy) - pivotX * myx - pivotZ * myz

        mzx = axisZ * axisX * (1 - cos) - axisY * sin
        mzy = axisZ * axisY * (1 - cos) + axisX * sin
        mzz = cos + axisZ * axisZ * (1 - cos)
        tz = pivotZ * (1 - mzz) - pivotX * mzx - pivotY * mzy
    }
}