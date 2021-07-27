package com.husker.minecraft.launcher.plugin


import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AreaGenerator(sender: CommandSender, plugin: Plugin, x: Int, y: Int, z: Int, radius: Int) {

    init {
        val content = JSONObject()
        val area = JSONArray()
        content.put("area", area)

        for(lx in x-radius..x+radius){
            for(ly in y-radius..y+radius){
                for(lz in z-radius..z+radius){
                    val block = getBlock(lx, ly, lz)
                    val name = block.toString().split("data=Block{")[1].split(":")[1].split("}")[0]

                    if(name == "air")
                        continue

                    area.put(JSONObject()
                        .put("name", name)
                        .put("x", lx - x)
                        .put("y", ly - y)
                        .put("z", lz - z)
                        .put("light", JSONObject()
                            .put("face", getBlock(lx, ly, lz - 1).lightFromBlocks)
                            .put("left", getBlock(lx - 1, ly, lz).lightFromBlocks)
                            .put("right", getBlock(lx + 1, ly, lz).lightFromBlocks)
                            .put("back", getBlock(lx, ly, lz + 1).lightFromBlocks)
                            .put("top", getBlock(lx, ly + 1, lz).lightFromBlocks)
                            .put("bottom", getBlock(lx, ly - 1, lz).lightFromBlocks)
                        )
                    )

                    plugin.logger.info(name)
                }
            }
        }

        val folder = File(plugin.server.worldContainer, "areas")
        folder.mkdirs()
        val file = File(folder, "area_${x}_${y}_${z}.txt")
        file.writeText(content.toString(4))

        sender.sendMessage("${ChatColor.GREEN}Completed!!")
    }

    private fun getBlock(x: Int, y: Int, z: Int): Block = Bukkit.getWorlds()[0].getBlockAt(x, y, z)

}