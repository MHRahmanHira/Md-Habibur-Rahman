package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.PhotoForgeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: PhotoForgeViewModel,
    isBn: Boolean,
    onNavigateToCrop: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing) {
        if (cameraPermissionState.status.isGranted) {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraScreen", "Use case binding failed", exc)
            }
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(if (isBn) "ক্যামেরা ব্যবহারের অনুমতি প্রয়োজন" else "Camera permission required")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text(if (isBn) "অনুমতি দিন" else "Grant Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onNavigateBack) {
                Text(if (isBn) "ফিরে যান" else "Go Back")
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Top Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
            }) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
            }
        }

        // Shutter Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
                .border(4.dp, Color.White, CircleShape)
                .clickable {
                    takePhoto(imageCapture, context, lensFacing == CameraSelector.LENS_FACING_FRONT) { bitmap ->
                        if (bitmap != null) {
                            viewModel.setSourceBitmap(bitmap)
                            onNavigateToCrop()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

private fun takePhoto(
    imageCapture: ImageCapture,
    context: Context,
    isFrontCamera: Boolean,
    onPhotoCaptured: (Bitmap?) -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                image.close()
                // Handle rotation
                val rotationDegrees = image.imageInfo.rotationDegrees.toFloat()
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees)
                if (isFrontCamera) {
                    matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                onPhotoCaptured(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                onPhotoCaptured(null)
            }
        }
    )
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer: ByteBuffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }
