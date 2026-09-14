package com.example.ai

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.tasks.await

object FirebaseAIBackgroundRemover {
    suspend fun removeBackground(bitmap: Bitmap): Bitmap? {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        val segmenter = SubjectSegmentation.getClient(options)
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        return try {
            val result = segmenter.process(inputImage).await()
            result.foregroundBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            segmenter.close()
        }
    }
}
