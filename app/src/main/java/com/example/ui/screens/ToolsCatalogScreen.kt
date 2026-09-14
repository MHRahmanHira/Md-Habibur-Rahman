package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolConfigEntity
import com.example.ui.components.CreditWalletPill
import com.example.ui.components.ToolCard
import com.example.ui.locale.Strings
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.PhotoForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsCatalogScreen(
    viewModel: PhotoForgeViewModel,
    isBn: Boolean,
    onToolSelected: (ToolConfigEntity) -> Unit,
    onNavigateToPricing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tools by viewModel.tools.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf(
        "ALL" to Strings.catAll(isBn),
        "PORTRAIT" to Strings.catPortrait(isBn),
        "ENHANCE" to Strings.catEnhance(isBn),
        "BACKGROUND" to Strings.catBackground(isBn),
        "RESTORE" to Strings.catRestore(isBn),
        "PRODUCT" to Strings.catProduct(isBn),
        "CREATIVE" to Strings.catCreative(isBn)
    )

    val filteredTools = tools.filter { tool ->
        val matchesCategory = selectedCategory == "ALL" || tool.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                tool.nameBn.contains(searchQuery, ignoreCase = true) ||
                tool.nameEn.contains(searchQuery, ignoreCase = true) ||
                tool.descriptionBn.contains(searchQuery, ignoreCase = true) ||
                tool.descriptionEn.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBn) "এআই টুলস ক্যাটালগ" else "AI Tools Catalog",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    CreditWalletPill(
                        credits = currentUser?.creditsBalance ?: 15,
                        plan = currentUser?.plan ?: "FREE",
                        onClick = onNavigateToPricing
                    )
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBn) "টুলস খুঁজুন..." else "Search tools...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = PrimaryIndigo
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tools_search_input")
            )

            // Category Horizontal Filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(text = label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Tools List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val chunked = filteredTools.chunked(2)
                items(chunked) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (tool in row) {
                            ToolCard(
                                tool = tool,
                                isBn = isBn,
                                onClick = { onToolSelected(tool) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
