package com.husker.minecraft.launcher.app.minecraft.scene

import javafx.scene.paint.Color
import org.json.JSONObject
import java.io.InputStream

class PreviewParameters(val color: Color, val map: () -> InputStream)