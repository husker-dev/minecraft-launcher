package com.husker.minecraft.launcher.tools.fx

import javafx.geometry.Point3D
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.CullFace
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import javafx.scene.transform.Transform

open class RectangleShape(var point1: Point3D, var point2: Point3D, var point3: Point3D, var point4: Point3D) : MeshView() {


    val phongMaterial: PhongMaterial
        get(){
            return material as PhongMaterial
        }

    init{
        val mesh = TriangleMesh()
        val points = arrayOf(
            point1.x, point1.y, point1.z,
            point2.x, point2.y, point2.z,
            point3.x, point3.y, point3.z,
            point4.x, point4.y, point4.z,
        ).map { it.toFloat() }.toFloatArray()

        val texCoords = floatArrayOf(
            1f, 1f,
            0f, 1f,
            1f, 0f,
            0f, 0f,
        )

        val faces = intArrayOf(
            2, 3, 0, 2, 1, 0,
            2, 3, 1, 0, 3, 1
        )

        mesh.points.setAll(*points)
        mesh.texCoords.setAll(*texCoords)
        mesh.faces.setAll(*faces)
        setMesh(mesh)

        material = PhongMaterial()
        cullFace = CullFace.FRONT
    }

    fun rotate90(){
        val points = (mesh as TriangleMesh).points
        val newPoints = arrayOfNulls<Float>(points.size())

        // 1 -> 3
        newPoints[0] = points[6]
        newPoints[1] = points[7]
        newPoints[2] = points[8]

        // 2 -> 1
        newPoints[3] = points[0]
        newPoints[4] = points[1]
        newPoints[5] = points[2]

        // 3 -> 4
        newPoints[6] = points[9]
        newPoints[7] = points[10]
        newPoints[8] = points[11]

        // 4 -> 2
        newPoints[9] = points[3]
        newPoints[10] = points[4]
        newPoints[11] = points[5]

        (mesh as TriangleMesh).points.setAll(*newPoints.map { it!! }.toFloatArray())
    }

    fun applyTransform(transform: Transform){
        val points = (mesh as TriangleMesh).points
        val newPoints = arrayOfNulls<Float>(points.size())

        for(i in 0 until points.size() step 3) {
            val point = transform.transform(Point3D(points[i].toDouble(), points[i + 1].toDouble(), points[i + 2].toDouble()))
            newPoints[i] = point.x.toFloat()
            newPoints[i + 1] = point.y.toFloat()
            newPoints[i + 2] = point.z.toFloat()
        }

        (mesh as TriangleMesh).points.setAll(*newPoints.map { it!! }.toFloatArray())
    }

}