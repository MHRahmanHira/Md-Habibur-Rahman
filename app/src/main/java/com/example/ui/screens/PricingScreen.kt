package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.locale.Strings
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.PhotoForgeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    viewModel: PhotoForgeViewModel,
    isBn: Boolean,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedItemType by remember { mutableStateOf("") } // "PLAN" or "CREDIT"
    var selectedItemName by remember { mutableStateOf("") }
    var selectedItemAmount by remember { mutableStateOf(0) }
    var selectedItemPrice by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("bKash") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.pricing(isBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Current Plan & Balance Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "বর্তমান অ্যাকাউন্ট স্ট্যাটাস" else "Account Status",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.plan ?: "FREE",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (currentUser?.plan?.contains("PRO") == true) AccentGold else SecondaryCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBn) "মোট ক্রেডিট ব্যালান্স" else "Total Credits",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentUser?.creditsBalance ?: 0}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Subscription Plans Section
            item {
                Text(
                    text = if (isBn) "সাবস্ক্রিপশন প্ল্যানসমূহ" else "Subscription Plans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Pro Monthly Card
            item {
                PlanCard(
                    title = Strings.proMonthlyTitle(isBn),
                    price = "৳৩৯৯ / মাস (BDT)",
                    features = listOf(
                        if (isBn) "২০০ মাসিক এআই ক্রেডিট" else "200 Monthly AI Credits",
                        if (isBn) "আল্ট্রা ২K ও ৪K হাই রেজোলিউশন" else "Ultra 2K & 4K High Resolution",
                        if (isBn) "কোনো ওয়াটারমার্ক নেই" else "No Watermark",
                        if (isBn) "প্রায়োরিটি ফাস্ট জেনারেশন" else "Priority Fast Generation"
                    ),
                    isFeatured = true,
                    buttonText = if (isBn) "প্রো মান্থলি বেছে নিন" else "Upgrade to Pro Monthly",
                    onSelect = {
                        selectedItemType = "PLAN"
                        selectedItemName = "PRO_MONTHLY"
                        selectedItemAmount = 200
                        selectedItemPrice = "৳৩৯৯"
                        showPaymentDialog = true
                    }
                )
            }

            // Pro Yearly Card
            item {
                PlanCard(
                    title = Strings.proYearlyTitle(isBn),
                    price = "৳২,৯৯৯ / বছর (BDT)",
                    features = listOf(
                        if (isBn) "২,৫০০ বাৎসরিক ক্রেডিট (সর্বোচ্চ সাশ্রয়ী)" else "2,500 Yearly Credits (Best Value)",
                        if (isBn) "ফুল কমার্শিয়াল ব্যবহারের অধিকার" else "Full Commercial License",
                        if (isBn) "প্রিমিয়াম স্টুডিও ও আর্ট মডেল সাপোর্ট" else "Premium Studio & Anime Models",
                        if (isBn) "VIP সাপোর্ট ও ব্যাচ প্রসেসিং" else "VIP Support & Batch Processing"
                    ),
                    isFeatured = false,
                    buttonText = if (isBn) "প্রো ইয়ারলি বেছে নিন" else "Upgrade to Pro Yearly",
                    onSelect = {
                        selectedItemType = "PLAN"
                        selectedItemName = "PRO_YEARLY"
                        selectedItemAmount = 2500
                        selectedItemPrice = "৳২,৯৯৯"
                        showPaymentDialog = true
                    }
                )
            }

            // Instant Credit Top-ups Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Strings.rechargeTitle(isBn),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Top-up Packs
            val packs = listOf(
                Triple(50, "৳৯৯", "স্টার্টার প্যাক (Starter)"),
                Triple(150, "৳২৪৯", "পপুলার প্যাক (Popular)"),
                Triple(500, "৳৬৯৯", "মেগা প্যাক (Best Deal)")
            )

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    packs.forEach { (credits, price, label) ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$credits ক্রেডিট ($label)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$price BDT",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SecondaryCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedItemType = "CREDIT"
                                        selectedItemName = "$credits Credits"
                                        selectedItemAmount = credits
                                        selectedItemPrice = price
                                        showPaymentDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier.testTag("recharge_pack_${credits}")
                                ) {
                                    Text(text = if (isBn) "রিচার্জ" else "Recharge", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Realistic Payment Gateway Modal (bKash / Nagad / Card)
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = {
                    Text(
                        text = if (isBn) "পেমেন্ট গেটওয়ে ভেরিফিকেশন" else "Payment Gateway Checkout",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (isBn) "প্যাকেজ: $selectedItemName" else "Package: $selectedItemName",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (isBn) "মোট প্রদেয়: $selectedItemPrice BDT" else "Total Payable: $selectedItemPrice BDT",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isBn) "পেমেন্ট মাধ্যম বেছে নিন:" else "Select Payment Method:",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val methods = listOf("bKash", "Nagad", "Debit/Credit Card")
                        methods.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedPaymentMethod == method)
                                            PrimaryIndigo.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedPaymentMethod = method }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = method,
                                    fontWeight = if (selectedPaymentMethod == method) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedPaymentMethod == method) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedPaymentMethod == method) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedItemType == "PLAN") {
                                viewModel.upgradePlan(selectedItemName, selectedItemAmount)
                            } else {
                                viewModel.rechargeCredits(selectedItemAmount, selectedPaymentMethod)
                            }
                            showPaymentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Text(text = if (isBn) "পেমেন্ট নিশ্চিত করুন" else "Confirm Payment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentDialog = false }) {
                        Text(text = if (isBn) "বাতিল" else "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    features: List<String>,
    isFeatured: Boolean,
    buttonText: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) Color(0xFF1E1B4B) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isFeatured) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentGold)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MOST POPULAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = price,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = SecondaryCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            features.forEach { feat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feat,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFeatured) SecondaryCyan else PrimaryIndigo,
                    contentColor = if (isFeatured) Color(0xFF00363D) else Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
