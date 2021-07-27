package com.husker.minecraft.launcher.app.minecraft.scene

import javafx.scene.paint.Color
import org.json.JSONObject

class PreviewParameters(val color: Color, val json: () -> JSONObject)