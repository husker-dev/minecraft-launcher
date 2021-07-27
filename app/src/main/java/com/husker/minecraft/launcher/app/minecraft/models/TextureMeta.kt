package com.husker.minecraft.launcher.app.minecraft.models

import org.json.JSONObject

class TextureMeta(json: String = "") {

    companion object{
        @JvmStatic val default = TextureMeta()
    }

    var frametime = 1
    var interpolate = false

    init {
        if (json.isNotEmpty()){
            val jsonMeta = JSONObject(json)
            if (jsonMeta.has("animation")){
                val jsonAnimation = jsonMeta.getJSONObject("animation")
                if(jsonAnimation.has("frametime"))
                    frametime = jsonAnimation.getInt("frametime")
                if(jsonAnimation.has("interpolate"))
                    interpolate = jsonAnimation.getBoolean("interpolate")
            }
        }
    }
}