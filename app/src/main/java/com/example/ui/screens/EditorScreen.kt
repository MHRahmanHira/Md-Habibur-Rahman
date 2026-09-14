package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.CreditWalletPill
import com.example.ui.components.ProcessingOverlay
import com.example.ui.components.getToolIcon
import com.example.ui.locale.Strings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.viewmodel.PhotoForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: PhotoForgeViewModel,
    isBn: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onNavigateToCrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val tools by viewModel.tools.collectAsState()
    val sourceBmp by viewModel.sourceBitmap.collectAsState()
    val resultBmp by viewModel.resultBitmap.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val customPrompt by viewModel.customPrompt.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val resolution by viewModel.resolution.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingStage by viewModel.processingStage.collectAsState()
    val feedbackMsg by viewModel.feedbackMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMsg) {
        feedbackMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Photo Picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadFromUri(uri)
            onNavigateToCrop()
        }
    }

    val quickSuggestions = listOf(
        if (isBn) "ফেস ও দাঁত হুবহু এক রাখুন" else "Keep face & teeth exactly unchanged",
        if (isBn) "ন্যাচারাল স্কিন টেক্সচার" else "Natural skin texture, no plastic blur",
        if (isBn) "প্রিমিয়াম স্টুডিও লাইটিং" else "Cinematic key studio lighting",
        if (isBn) "সফট ব্যাকগ্রাউন্ড বোকেহ" else "Soft atmospheric background blur"
    )

    val aspectRatios = listOf("Original", "1:1", "4:5", "3:4", "9:16", "16:9")
    val resolutions = listOf("1K", "2K Pro")

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "এআই এডিটর স্টুডিও" else "AI Editor Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Change photo button
                        IconButton(onClick = {
                            photoPicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Pick Image",
                                tint = SecondaryCyan
                            )
                        }
                        CreditWalletPill(
                            credits = currentUser?.creditsBalance ?: 15,
                            plan = currentUser?.plan ?: "FREE",
                            onClick = onNavigateToPricing
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Before/After Split Comparison View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    BeforeAfterSlider(
                        beforeBitmap = sourceBmp,
                        afterBitmap = resultBmp,
                        beforeLabel = Strings.beforeLabel(isBn),
                        afterLabel = Strings.afterLabel(isBn),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar under Canvas: Download, Share
                if (resultBmp != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveToGallery() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Strings.download(isBn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "PhotoForge AI Creation")
                                    putExtra(Intent.EXTRA_TEXT, "Edited with PhotoForge AI Studio!")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Strings.share(isBn), fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 2. Tool Preset Selector Bar
                Text(
                    text = if (isBn) "নির্বাচিত এআই টুলস" else "Select AI Tool",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(tools) { tool ->
                        val isSelected = selectedTool?.toolId == tool.toolId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.selectTool(tool) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("select_tool_${tool.toolId}")
                        ) {
                            Icon(
                                imageVector = getToolIcon(tool.iconName),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else SecondaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) tool.nameBn else tool.nameEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aspect Ratio & Resolution Config
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "অনুপাত (Aspect Ratio)" else "Aspect Ratio",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(aspectRatios) { ratio ->
                                FilterChip(
                                    selected = aspectRatio == ratio,
                                    onClick = { viewModel.setAspectRatio(ratio) },
                                    label = { Text(text = ratio, fontSize = 11.sp) },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Resolution
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBn) "রেজোলিউশন:" else "Resolution:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    resolutions.forEach { res ->
                        FilterChip(
                            selected = resolution == res,
                            onClick = { viewModel.setResolution(res) },
                            label = { Text(text = res, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Smart Prompt Editor
                Text(
                    text = Strings.quickPromptsTitle(isBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Prompt Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickSuggestions) { pill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val newPrompt = if (customPrompt.isBlank()) pill else "$customPrompt, $pill"
                                    viewModel.setCustomPrompt(newPrompt)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ $pill",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { viewModel.setCustomPrompt(it) },
                    placeholder = {
                        Text(
                            text = Strings.promptHint(isBn),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_prompt_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Primary Execute Button
                val creditCost = selectedTool?.creditCost ?: 1
                Button(
                    onClick = { viewModel.executeGeneration() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryCyan,
                        contentColor = Color(0xFF00363D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("execute_generation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.generateButton(isBn, creditCost),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Safety & Identity Notice
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "🛡️ ফেস ও বডি আইডেন্টিটি সুরক্ষিত। অসফল প্রসেসিংয়ে ক্রেডিট সম্পূর্ণ রিফান্ড হবে।" else "🛡️ Identity preserved strictly. Credits automatically refunded on any failure.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Processing Modal
        if (isProcessing) {
            ProcessingOverlay(
                stageText = processingStage,
                onCancel = { viewModel.cancelProcessing() }
            )
        }
    }
}
