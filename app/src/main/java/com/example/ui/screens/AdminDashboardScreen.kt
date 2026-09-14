package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobEntity
import com.example.data.model.ToolConfigEntity
import com.example.ui.locale.Strings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.PhotoForgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: PhotoForgeViewModel,
    isBn: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allTools by viewModel.allToolsAdmin.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        if (isBn) "টুলস পরিচালনা" else "Tools Control",
        if (isBn) "ক্রেডিট অডিট" else "Credit Audit",
        if (isBn) "জব মনিটর" else "Job Logs"
    )

    var adjustDelta by remember { mutableStateOf("10") }
    var adjustReason by remember { mutableStateOf("Admin Promotional Grant") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.admin(isBn),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Tool Management: Cost update & Enable/Disable
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allTools) { tool ->
                            AdminToolRow(
                                tool = tool,
                                isBn = isBn,
                                onCostChange = { newCost -> viewModel.adminUpdateToolCost(tool.toolId, newCost) },
                                onToggleStatus = { isEnabled -> viewModel.adminToggleTool(tool.toolId, isEnabled) }
                            )
                        }
                    }
                }
                1 -> {
                    // Credit Adjustments & Auditing
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isBn) "ক্রেডিট অ্যাডজাস্টমেন্ট ও রিফান্ড ম্যানেজমেন্ট" else "Credit Adjustments & Audit",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) "টার্গেট ইউজার: ${currentUser?.name} (${currentUser?.email})" else "Target User: ${currentUser?.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = adjustDelta,
                                    onValueChange = { adjustDelta = it },
                                    label = { Text(if (isBn) "ক্রেডিট সংখ্যা (যেমন: 20 বা -5)" else "Credit Delta") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = adjustReason,
                                    onValueChange = { adjustReason = it },
                                    label = { Text(if (isBn) "অডিট নোট / কারণ" else "Audit Reason") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val delta = adjustDelta.toIntOrNull() ?: 0
                                        viewModel.adminAdjustCredits(delta, adjustReason)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = if (isBn) "ক্রেডিট প্রয়োগ করুন" else "Apply Credit Change", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Job Monitoring & Error Tracking
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(jobs) { job ->
                            JobLogRow(job = job)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminToolRow(
    tool: ToolConfigEntity,
    isBn: Boolean,
    onCostChange: (Int) -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBn) tool.nameBn else tool.nameEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${tool.toolId} • ${tool.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Cost Stepper
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (tool.creditCost > 1) onCostChange(tool.creditCost - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${tool.creditCost} cr",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentGold
                )
                IconButton(
                    onClick = { onCostChange(tool.creditCost + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Enable / Disable Switch
            Switch(
                checked = tool.isEnabled,
                onCheckedChange = onToggleStatus,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SecondaryCyan
                )
            )
        }
    }
}

@Composable
fun JobLogRow(job: JobEntity) {
    val dateStr = remember(job.startedAt) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(job.startedAt))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (job.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (job.status == "COMPLETED") SuccessGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tool: ${job.toolId} • Status: ${job.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Time: $dateStr • Cost: ${job.creditCost} credits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (job.errorMessage != null) {
                    Text(
                        text = "Error: ${job.errorMessage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
