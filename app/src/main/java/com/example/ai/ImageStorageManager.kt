package com.example.ai

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    private val imagesDir: File by lazy {
        val dir = File(context.filesDir, "photoforge_images")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun saveBitmap(bitmap: Bitmap, prefix: String = "img"): String = withContext(Dispatchers.IO) {
        val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
        val file = File(imagesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        file.absolutePath
    }

    suspend fun loadBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun exportToGallery(bitmap: Bitmap, title: String = "PhotoForge_Edit"): Boolean = withContext(Dispatchers.IO) {
        try {
            val filename = "${title}_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PhotoForge")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false

            resolver.openOutputStream(imageUri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteFile(path: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) file.delete()
        } catch (_: Exception) {}
    }

    /**
     * Generates a high quality test portrait bitmap for instant playground testing
     */
    fun createSamplePortraitBitmap(): Bitmap {
        val width = 800
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient background
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.parseColor("#334155"), Color.parseColor("#0F172A"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Silhouette / Portrait bust
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
        // Head
        canvas.drawCircle(width / 2f, height * 0.42f, 160f, bodyPaint)
        // Eyes
        val featurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1E293B") }
        canvas.drawCircle(width / 2f - 60f, height * 0.41f, 18f, featurePaint)
        canvas.drawCircle(width / 2f + 60f, height * 0.41f, 18f, featurePaint)
        // Smile
        featurePaint.style = Paint.Style.STROKE
        featurePaint.strokeWidth = 8f
        canvas.drawArc(width / 2f - 45f, height * 0.44f, width / 2f + 45f, height * 0.51f, 0f, 180f, false, featurePaint)
        // Torso
        featurePaint.style = Paint.Style.FILL
        canvas.drawOval(width * 0.15f, height * 0.65f, width * 0.85f, height * 1.15f, bodyPaint)

        return bitmap
    }
}
