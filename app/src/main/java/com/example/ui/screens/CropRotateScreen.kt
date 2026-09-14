package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.PhotoForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRotateScreen(
    viewModel: PhotoForgeViewModel,
    isBn: Boolean,
    onNavigateToEditor: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val sourceBmp by viewModel.sourceBitmap.collectAsState()

    if (sourceBmp == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }

    var currentBmp by remember { mutableStateOf(sourceBmp!!) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var aspectRatio by remember { mutableStateOf("Original") }

    val ratios = listOf("Original", "1:1", "4:3", "3:4", "16:9")

    val cropBoxSize = remember(viewportSize, aspectRatio, currentBmp) {
        if (viewportSize == IntSize.Zero) return@remember Size.Zero
        val padding = 120f
        val maxW = viewportSize.width - padding
        val maxH = viewportSize.height - padding

        val ratio = when (aspectRatio) {
            "1:1" -> 1f
            "4:3" -> 4f / 3f
            "3:4" -> 3f / 4f
            "16:9" -> 16f / 9f
            else -> currentBmp.width.toFloat() / currentBmp.height.toFloat()
        }

        var w = maxW
        var h = w / ratio
        if (h > maxH) {
            h = maxH
            w = h * ratio
        }
        Size(w, h)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBn) "ক্রপ ও রোটেট" else "Crop & Rotate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val cropped = applyCrop(currentBmp, zoom, offsetX, offsetY, viewportSize, cropBoxSize)
                        viewModel.setSourceBitmap(cropped)
                        onNavigateToEditor()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = SecondaryCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = {
                        currentBmp = rotateBitmap(currentBmp, -90f)
                        zoom = 1f; offsetX = 0f; offsetY = 0f
                    }) {
                        Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = "Left", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBn) "বাম" else "Left")
                    }
                    OutlinedButton(onClick = {
                        currentBmp = rotateBitmap(currentBmp, 90f)
                        zoom = 1f; offsetX = 0f; offsetY = 0f
                    }) {
                        Icon(Icons.Default.Rotate90DegreesCw, contentDescription = "Right", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBn) "ডান" else "Right")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ratios) { ratio ->
                        FilterChip(
                            selected = aspectRatio == ratio,
                            onClick = {
                                aspectRatio = ratio
                                zoom = 1f; offsetX = 0f; offsetY = 0f
                            },
                            label = { Text(ratio) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .onSizeChanged { viewportSize = it }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoomChange, _ ->
                        zoom = (zoom * zoomChange).coerceIn(1f, 10f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (viewportSize != IntSize.Zero) {
                Image(
                    bitmap = currentBmp.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = zoom,
                            scaleY = zoom,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val left = (size.width - cropBoxSize.width) / 2f
                    val top = (size.height - cropBoxSize.height) / 2f
                    val right = left + cropBoxSize.width
                    val bottom = top + cropBoxSize.height

                    // Dark overlay
                    clipRect(left, top, right, bottom, clipOp = ClipOp.Difference) {
                        drawRect(Color.Black.copy(alpha = 0.7f))
                    }

                    // Grid lines
                    val thirdW = cropBoxSize.width / 3f
                    val thirdH = cropBoxSize.height / 3f
                    val strokeW = 2f

                    // Vertical
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(left + thirdW, top), Offset(left + thirdW, bottom), strokeW)
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(left + 2 * thirdW, top), Offset(left + 2 * thirdW, bottom), strokeW)
                    // Horizontal
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(left, top + thirdH), Offset(right, top + thirdH), strokeW)
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(left, top + 2 * thirdH), Offset(right, top + 2 * thirdH), strokeW)

                    // Border
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(left, top),
                        size = cropBoxSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                }
            }
        }
    }
}

private fun applyCrop(bmp: Bitmap, zoom: Float, offsetX: Float, offsetY: Float, viewportSize: IntSize, cropBoxSize: Size): Bitmap {
    val baseScale = minOf(viewportSize.width.toFloat() / bmp.width, viewportSize.height.toFloat() / bmp.height)
    val totalScale = baseScale * zoom

    val cropBmpW = (cropBoxSize.width / totalScale).toInt()
    val cropBmpH = (cropBoxSize.height / totalScale).toInt()

    val centerX = (bmp.width / 2f) - (offsetX / totalScale)
    val centerY = (bmp.height / 2f) - (offsetY / totalScale)

    var left = (centerX - cropBmpW / 2f).toInt()
    var top = (centerY - cropBmpH / 2f).toInt()

    left = left.coerceIn(0, maxOf(0, bmp.width - cropBmpW))
    top = top.coerceIn(0, maxOf(0, bmp.height - cropBmpH))
    val right = (left + cropBmpW).coerceAtMost(bmp.width)
    val bottom = (top + cropBmpH).coerceAtMost(bmp.height)

    val width = right - left
    val height = bottom - top

    if (width <= 0 || height <= 0) return bmp

    return Bitmap.createBitmap(bmp, left, top, width, height)
}

private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}
