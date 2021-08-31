package com.husker.minecraft.launcher.plugin.writer

class CachedHashSet<T>: LinkedHashSet<T>(){

    fun addAndGetIndex(value: T): Int{
        add(value)
        return indexOf(value)
    }
}