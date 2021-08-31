package com.husker.minecraft.launcher.plugin


import com.husker.minecraft.launcher.plugin.transform.Point3D
import com.husker.minecraft.launcher.plugin.transform.Rotation
import com.husker.minecraft.launcher.plugin.utils.ProgressBar
import com.husker.minecraft.launcher.plugin.writer.AreaFileWriter
import org.bukkit.ChatColor
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.boss.BarColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.BlockIterator
import org.bukkit.util.RayTraceResult
import java.io.File
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class AreaGenerator(val sender: CommandSender, plugin: Plugin, x: Int, y: Int, z: Int, radius: Int, sky: Boolean) {

    private val pivot = Point3D(x + 0.5, y + 4.5, z + 0.5)
    private val camPos = Point3D(x + 0.5, y + 4.5, z - 17.5)

    private val world = (sender as Entity).world

    init {
        val writer = AreaFileWriter(radius, x, y, z, (sender as Entity).world, sky)

        // Getting visible blocks
        val progressBar = ProgressBar(sender)
        with(progressBar){
            title = "Getting visible blocks"
            color = BarColor.GREEN
            max = (radius * 2.0).pow(3).toInt()
        }

        val blocks = arrayListOf<Block>()
        val visibleBlocks = arrayListOf<Block>()

        for(lx in x-radius..x+radius)
            for(ly in y-radius..y+radius)
                for(lz in z-radius..z+radius)
                    blocks.add(getBlock(lx, ly, lz))

        blocks.parallelStream().forEach {
            if(isBlockVisible(it))
                visibleBlocks.add(it)
            progressBar.value++
        }

        // Getting visible sides
        with(progressBar){
            title = "Getting visible sides"
            color = BarColor.YELLOW
            value = 0
            max = visibleBlocks.size
        }

        visibleBlocks.parallelStream().forEach {
            writer.addBlock(it, getVisibleSides(it))
            progressBar.value++
        }

        // Saving
        with(progressBar){
            title = "Saving area"
            color = BarColor.WHITE
            value = 1
            max = 2
        }
        writer.save(File(plugin.server.worldContainer, "areas"))
        progressBar.disable()
        sender.sendMessage("${ChatColor.GREEN}Completed.")
    }

    private fun isBlockVisible(block: Block): Boolean{
        val name = block.toString().split("data=Block{")[1].split(":")[1].split("}")[0]
        if(name == "air" || name == "void_air")
            return false

        val indent = 0.005
        var result = false
        val rotator = CamRotator()
        rotator.start { cam ->
            for(tx in 0..2){
                for(ty in 0..2){
                    for(tz in 0..2){
                        val point = Point3D(block.x + indent, block.y + indent, block.z + indent)
                        if(tx > 0)
                            point.x += (1 - indent * 2.0) / tx.toDouble()
                        if(ty > 0)
                            point.y += (1 - indent * 2.0) / ty.toDouble()
                        if(tz > 0)
                            point.z += (1 - indent * 2.0) / tz.toDouble()

                        if(checkRayTrace(cam, point, block)){
                            result = true
                            rotator.stop()
                            return@start
                        }
                    }
                }
            }
        }

        return result
    }

    private fun getVisibleSides(block: Block): Array<AreaFileWriter.Side>{
        val sides = arrayListOf<AreaFileWriter.Side>()

        val indent = 0.005
        val rotator = CamRotator()
        rotator.start { cam ->
            if(sides.size == AreaFileWriter.Side.values().size) {
                rotator.stop()
                return@start
            }
            for(t1 in 0..2) {
                for (t2 in 0..2) {
                    if(AreaFileWriter.Side.Front !in sides){
                        val point = Point3D(block.x + indent, block.y + indent, block.z.toDouble())
                        if(t1 > 0) point.x += (1 - indent * 2.0) / t1.toDouble()
                        if(t2 > 0) point.y += (1 - indent * 2.0) / t2.toDouble()
                        if(checkRayTraceFace(cam, point, block, BlockFace.NORTH))
                            sides.add(AreaFileWriter.Side.Front)
                    }
                    if(AreaFileWriter.Side.Top !in sides){
                        val point = Point3D(block.x + indent, block.y + 1.0, block.z + indent)
                        if(t1 > 0) point.x += (1 - indent * 2.0) / t1.toDouble()
                        if(t2 > 0) point.z += (1 - indent * 2.0) / t2.toDouble()
                        if(checkRayTraceFace(cam, point, block, BlockFace.UP))
                            sides.add(AreaFileWriter.Side.Top)
                    }
                    if(AreaFileWriter.Side.Bottom !in sides){
                        val point = Point3D(block.x + indent, block.y.toDouble(), block.z + indent)
                        if(t1 > 0) point.x += (1 - indent * 2.0) / t1.toDouble()
                        if(t2 > 0) point.z += (1 - indent * 2.0) / t2.toDouble()
                        if(checkRayTraceFace(cam, point, block, BlockFace.DOWN))
                            sides.add(AreaFileWriter.Side.Bottom)
                    }
                    if(AreaFileWriter.Side.Left !in sides){
                        val point = Point3D(block.x + 1.0, block.y + indent, block.z + indent)
                        if(t1 > 0) point.y += (1 - indent * 2.0) / t1.toDouble()
                        if(t2 > 0) point.z += (1 - indent * 2.0) / t2.toDouble()
                        if(checkRayTraceFace(cam, point, block, BlockFace.WEST))
                            sides.add(AreaFileWriter.Side.Left)
                    }
                    if(AreaFileWriter.Side.Right !in sides){
                        val point = Point3D(block.x.toDouble(), block.y + indent, block.z + indent)
                        if(t1 > 0) point.y += (1 - indent * 2.0) / t1.toDouble()
                        if(t2 > 0) point.z += (1 - indent * 2.0) / t2.toDouble()
                        if(checkRayTraceFace(cam, point, block, BlockFace.EAST))
                            sides.add(AreaFileWriter.Side.Right)
                    }
                }
            }
        }

        return sides.toArray(arrayOf())
    }

    fun isPassable(block: Block) = (block.isEmpty || block.isLiquid || block.isPassable)

    private fun checkRayTraceFace(cam: Point3D, blockPos: Point3D, block: Block, face: BlockFace): Boolean{
        val iterator = BlockIterator(world, cam.toVector(), cam.toVector(blockPos), 0.0, cam.distance(blockPos).toInt() + 2)

        // Near blocks
        val topBlock = block.world.getBlockAt(block.x, block.y + 1, block.z)
        val bottomBlock = block.world.getBlockAt(block.x, block.y - 1, block.z)
        val leftBlock = block.world.getBlockAt(block.x + 1, block.y, block.z)
        val rightBlock = block.world.getBlockAt(block.x - 1, block.y, block.z)
        //val backBlock = block.world.getBlockAt(block.x, block.y, block.z + 1)
        val frontBlock = block.world.getBlockAt(block.x, block.y, block.z - 1)

        while(iterator.hasNext()){
            val foundBlock = iterator.next()

            when(foundBlock){
                topBlock -> return face == BlockFace.UP && isPassable(topBlock)
                bottomBlock -> return face == BlockFace.DOWN && isPassable(bottomBlock)
                leftBlock -> return face == BlockFace.WEST && isPassable(leftBlock)
                rightBlock -> return face == BlockFace.EAST && isPassable(rightBlock)
                //backBlock -> return face == BlockFace.NORTH// && isPassable(backBlock)
                frontBlock -> return face == BlockFace.NORTH && isPassable(frontBlock)
            }

            if(isPassable(foundBlock))
                continue
            return false
        }
        return false
    }

    private fun checkRayTrace(cam: Point3D, blockPos: Point3D, block: Block): Boolean{
        val iterator = BlockIterator(world, cam.toVector(), cam.toVector(blockPos), 0.0, cam.distance(blockPos).toInt() + 2)
        while(iterator.hasNext()){
            val foundBlock = iterator.next()

            if(foundBlock == block)
                return true
            if(isPassable(foundBlock))
                continue
            return false
        }
        return false
    }

    private fun getBlock(x: Int, y: Int, z: Int): Block = world.getBlockAt(x, y, z)


    inner class CamRotator {
        var isWorking = false

        fun start(loop: (Point3D) -> Unit){
            isWorking = true
            var angle = 0.0
            while(angle < Math.PI * 2 && isWorking) {
                angle += 0.2
                val camPoint = camRotationFunc(angle)
                loop.invoke(camPoint)
            }
        }

        fun stop(){
            isWorking = false
        }

        private fun camRotationFunc(value: Double): Point3D{
            // Rotation func from launcher
            val range = 10  // Not 6 because not all blocks detected

            val rotX = sin(value) * range
            val rotY = cos(value) * range

            return Rotation(rotX, pivot, Rotation.Y_AXIS).transform(Rotation(rotY, pivot, Rotation.X_AXIS).transform(camPos))
        }
    }
}