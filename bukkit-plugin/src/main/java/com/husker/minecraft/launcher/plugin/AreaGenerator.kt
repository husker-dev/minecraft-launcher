package com.husker.minecraft.launcher.plugin


import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AreaGenerator(sender: CommandSender, plugin: Plugin, x: Int, y: Int, z: Int, radius: Int, sky: Boolean) {

    init {
        val content = JSONObject()
        val area = JSONArray()
        content.put("area", area)

        val blockNamesMap = arrayListOf<String>()
        val blockDataKeyMap = arrayListOf<String>()
        val blockDataValuesMap = arrayListOf<String>()

        for(lx in x-radius..x+radius){
            for(ly in y-radius..y+radius){
                for(lz in z-radius..z+radius){
                    val block = getBlock(lx, ly, lz)
                    val name = block.toString().split("data=Block{")[1].split(":")[1].split("}")[0]

                    if(name == "air")
                        continue

                    if(name !in blockNamesMap)
                        blockNamesMap.add(name)
                    val nameId = blockNamesMap.indexOf(name)

                    val blockInfo = JSONObject()
                        .put("n", nameId)
                        .put("p", JSONArray()
                            .put(lx - x)
                            .put(ly - y)
                            .put(lz - z)
                        )
                        .put("l", JSONArray()
                            .put(getBlock(lx, ly, lz - 1).lightFromBlocks)  // Front
                            .put(getBlock(lx - 1, ly, lz).lightFromBlocks)  // Left
                            .put(getBlock(lx + 1, ly, lz).lightFromBlocks)  // Right
                            .put(getBlock(lx, ly, lz + 1).lightFromBlocks)  // Back
                            .put(getBlock(lx, ly + 1, lz).lightFromBlocks)  // Top
                            .put(getBlock(lx, ly - 1, lz).lightFromBlocks)  // Bottom
                        )
                    if("[" in block.blockData.toString()) {
                        val blockData = JSONObject()
                        for (dataPair in block.blockData.toString().split("[")[1].split("]")[0].split(",")) {
                            val data = dataPair.split("=")
                            val key = data[0]
                            val value = data[1]

                            if(key !in blockDataKeyMap)
                                blockDataKeyMap.add(key)
                            if(value !in blockDataValuesMap)
                                blockDataValuesMap.add(value)
                            val keyIndex = blockDataKeyMap.indexOf(key)
                            val valueIndex = blockDataValuesMap.indexOf(value)

                            blockData.put(keyIndex.toString(), valueIndex)
                        }
                        blockInfo.put("d", blockData)
                    }

                    area.put(blockInfo)
                }
            }
        }

        content.put("names_map", blockNamesMap.associateBy { blockNamesMap.indexOf(it) })
        content.put("data_keys_map", blockDataKeyMap.associateBy { blockDataKeyMap.indexOf(it) })
        content.put("data_values_map", blockDataValuesMap.associateBy { blockDataValuesMap.indexOf(it) })

        val folder = File(plugin.server.worldContainer, "areas")
        folder.mkdirs()
        val file = File(folder, "area_${x}_${y}_${z}.txt")
        file.writeText(content.toString())

        sender.sendMessage("${ChatColor.GREEN}Completed!!")
    }

    private fun getBlock(x: Int, y: Int, z: Int): Block = Bukkit.getWorlds()[0].getBlockAt(x, y, z)

}