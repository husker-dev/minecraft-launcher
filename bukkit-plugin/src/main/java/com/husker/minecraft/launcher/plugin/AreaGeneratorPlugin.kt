package com.husker.minecraft.launcher.plugin

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.UnsupportedOperationException

class AreaGeneratorPlugin : JavaPlugin() {

    override fun onEnable() {
        getCommand("area")!!.setExecutor{ sender, _, _, args ->
            try {
                when (args.size) {
                    5 -> AreaGenerator(
                        sender,
                        this,
                        args[0].toInt(),
                        args[1].toInt(),
                        args[2].toInt(),
                        args[3].toInt(),
                        args[4] == "true"
                    )
                    4 -> AreaGenerator(
                        sender,
                        this,
                        args[0].toInt(),
                        args[1].toInt(),
                        args[2].toInt(),
                        args[3].toInt(),
                        false
                    )
                    else -> sender.sendMessage("${ChatColor.RED}Usage: /area [x] [y] [z] [radius] [sky_light]")
                }
            }catch (e: UnsupportedOperationException){
                sender.sendMessage("${ChatColor.RED}Too large radius or too many blocks!")
            }
            return@setExecutor true
        }
        getCommand("area")!!.tabCompleter = AreaCommandCompleter
    }

    object AreaCommandCompleter: TabCompleter{
        override fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
        ): MutableList<String>? {
            try {
                if (sender is Player) {
                    val block = sender.rayTraceBlocks(4.0)
                    if (block!!.hitBlock == null)
                        return null

                    if (args.size == 1)
                        return arrayListOf(block.hitBlock!!.x.toString())
                    if (args.size == 2)
                        return arrayListOf(block.hitBlock!!.y.toString())
                    if (args.size == 3)
                        return arrayListOf(block.hitBlock!!.z.toString())
                    if (args.size == 4)
                        return arrayListOf("12")
                    if (args.size == 5)
                        return arrayListOf("true", "false")
                }
            }catch (e: Exception){}
            return null
        }

    }
}