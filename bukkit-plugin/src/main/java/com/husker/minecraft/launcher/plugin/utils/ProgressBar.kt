package com.husker.minecraft.launcher.plugin.utils

import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class ProgressBar(private val entity: Entity) {

    private lateinit var bar: BossBar

    private var _title = "Progress bar"
    var title: String
        get() = _title
        set(value) {
            _title = value
            validate()
        }

    private var _color = BarColor.WHITE
    var color: BarColor
        get() = _color
        set(value) {
            _color = value
            validate()
        }

    private var _max = 100
    var max: Int
        get() = _max
        set(value) {
            _max = value
            validateValue()
        }

    private var _value = 0
    var value: Int
        get() = _value
        set(value) {
            _value = value
            validateValue()
        }

    init{
        validate()
    }

    private fun validate(){
        if(this::bar.isInitialized)
            bar.removeAll()
        bar = entity.server.createBossBar(_title, _color, BarStyle.SEGMENTED_10)
        validateValue()
        if(entity is Player)
            bar.addPlayer(entity)
    }

    private fun validateValue(){
        var value = _value.toDouble() / _max.toDouble()
        if(value > 1)
            value = 1.0
        bar.progress = value
    }

    fun disable(){
        bar.removeAll()
    }
}