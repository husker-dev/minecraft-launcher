package com.husker.minecraft.launcher.plugin

import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin

class AreaGeneratorPlugin : JavaPlugin() {

    override fun onEnable() {
        getCommand("area")!!.setExecutor{ sender, _, _, args ->
            if(args.size == 4)
                AreaGenerator(sender, this, args[0].toInt(), args[1].toInt(), args[2].toInt(), args[3].toInt())
            else
                sender.sendMessage("${ChatColor.RED}Wrong usage!")
            return@setExecutor true
        }
    }
}