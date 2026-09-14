package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap?,
    afterBitmap: Bitmap?,
    beforeLabel: String = "Before",
    afterLabel: String = "After",
    modifier: Modifier = Modifier
) {
    if (beforeBitmap == null && afterBitmap == null) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ছবি লোড করা হয়নি (No image loaded)",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // If only beforeBitmap exists
    if (afterBitmap == null && beforeBitmap != null) {
        Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
            Image(
                bitmap = beforeBitmap.asImageBitmap(),
                contentDescription = beforeLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = beforeLabel,
                fontSize = 11.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        return
    }

    // Both before and after bitmaps available: Interactive split-slider
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .testTag("before_after_slider_container")
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight
        val totalWidthPx = constraints.maxWidth.toFloat()

        // 1. Bottom Layer: Full After Image
        if (afterBitmap != null) {
            Image(
                bitmap = afterBitmap.asImageBitmap(),
                contentDescription = afterLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Top Layer: Before Image clipped to splitFraction
        if (beforeBitmap != null) {
            val clippedWidth = totalWidth * splitFraction
            Box(
                modifier = Modifier
                    .width(clippedWidth)
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                Image(
                    bitmap = beforeBitmap.asImageBitmap(),
                    contentDescription = beforeLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(totalWidth, totalHeight)
                )
            }
        }

        // 3. Draggable Divider Line & Handle
        val dividerX = (totalWidthPx * splitFraction).roundToInt()
        Box(
            modifier = Modifier
                .offset { IntOffset(dividerX - 16, 0) }
                .width(32.dp)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newFraction = (splitFraction + (dragAmount.x / totalWidthPx)).coerceIn(0.05f, 0.95f)
                        splitFraction = newFraction
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Vertical thin line
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.9f))
            )
            // Circular drag handle button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Drag to compare",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Badges for Before & After
        Text(
            text = beforeLabel,
            fontSize = 11.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Text(
            text = afterLabel,
            fontSize = 11.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
