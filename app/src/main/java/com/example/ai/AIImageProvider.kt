package com.example.ai

import android.graphics.Bitmap
import com.example.data.model.ToolConfigEntity

data class AIProcessResult(
    val bitmap: Bitmap,
    val providerUsed: String, // "Gemini 2.5 Flash Image" or "PhotoForge Core Engine"
    val durationMs: Long,
    val promptExecuted: String
)

interface AIImageProvider {
    suspend fun processImage(
        originalBitmap: Bitmap,
        customPrompt: String,
        toolConfig: ToolConfigEntity,
        aspectRatio: String = "1:1",
        resolution: String = "1K",
        onProgress: (String) -> Unit
    ): Result<AIProcessResult>
}
