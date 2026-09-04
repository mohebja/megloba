package com.global.sms.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.parser.BankSmsAnalysis
import com.global.sms.core.parser.TransactionType
import com.global.sms.core.util.PersianUtils
import com.global.sms.ui.viewmodels.GlobalSmsViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDashboardScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bankAnalyses by viewModel.bankAnalyses.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsStateWithLifecycle()

    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedBankFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Overview & Transactions, 1: OTPs & Links, 2: Cards & Balances

    // Calculations
    val filteredAnalyses = remember(bankAnalyses, selectedTypeFilter, selectedBankFilter, searchQuery) {
        bankAnalyses.filter { item ->
            val matchesType = selectedTypeFilter == null || item.transactionType == selectedTypeFilter
            val matchesBank = selectedBankFilter == null || item.bankName == selectedBankFilter
            val cardNum = item.cardNumber
            val trackingNum = item.trackingNumber
            val otpCodeStr = item.otpCode
            val matchesQuery = searchQuery.isBlank() ||
                    item.bankName.contains(searchQuery, ignoreCase = true) ||
                    item.rawBody.contains(searchQuery, ignoreCase = true) ||
                    (cardNum != null && cardNum.contains(searchQuery)) ||
                    (trackingNum != null && trackingNum.contains(searchQuery)) ||
                    (otpCodeStr != null && otpCodeStr.contains(searchQuery))

            matchesType && matchesBank && matchesQuery
        }
    }

    val totalIncomeTomans = remember(bankAnalyses) {
        bankAnalyses.filter { it.transactionType == TransactionType.CREDIT }
            .mapNotNull { it.amountTomans }
            .sum()
    }

    val totalExpenseTomans = remember(bankAnalyses) {
        bankAnalyses.filter { it.transactionType == TransactionType.DEBIT }
            .mapNotNull { it.amountTomans }
            .sum()
    }

    val totalBalanceTomans = remember(bankAnalyses) {
        // Group by card or bank and take latest balance
        bankAnalyses.mapNotNull { analysis ->
            analysis.balanceTomans?.let { bal -> (analysis.cardNumber ?: analysis.bankName) to bal }
        }.toMap().values.sum()
    }

    val latestOtp = remember(bankAnalyses) {
        bankAnalyses.firstOrNull { !it.otpCode.isNullOrBlank() }
    }

    val detectedCards = remember(bankAnalyses) {
        bankAnalyses.filter { !it.cardNumber.isNullOrBlank() || it.balanceTomans != null }
            .groupBy { it.cardNumber ?: it.bankName }
            .map { (_, list) -> list.first() }
    }

    val detectedOtps = remember(bankAnalyses) {
        bankAnalyses.filter { !it.otpCode.isNullOrBlank() }
    }

    val detectedLinks = remember(bankAnalyses) {
        bankAnalyses.filter { !it.paymentLink.isNullOrBlank() }
    }

    val availableBanks = remember(bankAnalyses) {
        bankAnalyses.map { it.bankName }.distinct()
    }

    val numberFormat = DecimalFormat("#,###")

    fun formatMoney(amountTomans: Long): String {
        val formatted = "${numberFormat.format(amountTomans)} تومان"
        return if (usePersianDigits) PersianUtils.toPersianDigits(formatted) else formatted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مرکز مدیریت مالی و بانکی",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = formatMoney(totalBalanceTomans),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits("${bankAnalyses.size} تراکنش بانکی")
                            else "${bankAnalyses.size} تراکنش بانکی",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (latestOtp != null) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.copyToSecureClipboard("رمز پویا", latestOtp.otpCode ?: "")
                                Toast.makeText(context, "کد تایید ${latestOtp.otpCode} کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("copy_latest_otp_button")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (usePersianDigits) "کپی رمز (${PersianUtils.toPersianDigits(latestOtp.otpCode ?: "")})"
                                else "کپی رمز (${latestOtp.otpCode})",
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pinned 4 Top-Level Tabs
            PrimaryTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("خلاصه و نمودارها", fontSize = 12.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("تراکنش‌ها (${filteredAnalyses.size})", fontSize = 12.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("رمز پویا و لینک‌ها (${detectedOtps.size + detectedLinks.size})", fontSize = 12.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("کارت‌ها (${detectedCards.size})", fontSize = 12.sp, fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab 0: Overview & Analytics
                if (activeTab == 0) {
                    item {
                        FinancialOverviewCard(
                            totalBalance = formatMoney(totalBalanceTomans),
                            totalIncome = formatMoney(totalIncomeTomans),
                            totalExpense = formatMoney(totalExpenseTomans),
                            netSavings = formatMoney(totalIncomeTomans - totalExpenseTomans),
                            usePersianDigits = usePersianDigits
                        )
                    }

                    item {
                        FinancialChartsCard(
                            incomeTomans = totalIncomeTomans,
                            expenseTomans = totalExpenseTomans,
                            bankAnalyses = bankAnalyses,
                            usePersianDigits = usePersianDigits
                        )
                    }
                }

                // Tab 1: Transactions
                if (activeTab == 1) {
                    item {
                        // Filters & Search Bar
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("جستجو در کارت، بانک، کد پیگیری یا مبلغ...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bank_search_input")
                            )

                            // Type Filter Chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedTypeFilter == null,
                                        onClick = { selectedTypeFilter = null },
                                        label = { Text("همه") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedTypeFilter == TransactionType.CREDIT,
                                        onClick = { selectedTypeFilter = TransactionType.CREDIT },
                                        label = { Text("واریز (درآمد)") },
                                        leadingIcon = { Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF2E7D32)) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedTypeFilter == TransactionType.DEBIT,
                                        onClick = { selectedTypeFilter = TransactionType.DEBIT },
                                        label = { Text("برداشت (هزینه)") },
                                        leadingIcon = { Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFC62828)) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedTypeFilter == TransactionType.OTP,
                                        onClick = { selectedTypeFilter = TransactionType.OTP },
                                        label = { Text("رمز پویا") },
                                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) }
                                    )
                                }
                            }

                            // Bank Filter Chips
                            if (availableBanks.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        FilterChip(
                                            selected = selectedBankFilter == null,
                                            onClick = { selectedBankFilter = null },
                                            label = { Text("همه بانک‌ها") }
                                        )
                                    }
                                    items(availableBanks) { bank ->
                                        FilterChip(
                                            selected = selectedBankFilter == bank,
                                            onClick = { selectedBankFilter = if (selectedBankFilter == bank) null else bank },
                                            label = { Text(bank) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredAnalyses.isEmpty()) {
                        item {
                            EmptyBankState(
                                title = "تراکنشی یافت نشد",
                                description = "با دریافت پیامک‌های جدید بانکی، تراکنش‌ها، کدهای OTP و موجودی کارت‌ها به طور خودکار استخراج و نمایش داده می‌شوند."
                            )
                        }
                    } else {
                        items(filteredAnalyses) { analysis ->
                            BankTransactionItemCard(
                                analysis = analysis,
                                usePersianDigits = usePersianDigits,
                                usePersianCalendar = usePersianCalendar,
                                onCopyOtp = { code ->
                                    viewModel.copyToSecureClipboard("رمز پویا", code)
                                    Toast.makeText(context, "کد $code کپی شد", Toast.LENGTH_SHORT).show()
                                },
                                onCopyTracking = { tracking ->
                                    viewModel.copyToSecureClipboard("شماره پیگیری", tracking)
                                    Toast.makeText(context, "شماره پیگیری $tracking کپی شد", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Tab 2: OTPs & Links
                if (activeTab == 2) {
                    if (detectedOtps.isNotEmpty()) {
                        item {
                            Text(
                                text = "کدهای تایید و رمز پویا (One Tap Copy)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(detectedOtps) { otp ->
                            OtpCodeCard(
                                otp = otp,
                                usePersianDigits = usePersianDigits,
                                onCopy = { code ->
                                    viewModel.copyToSecureClipboard("رمز پویا", code)
                                    Toast.makeText(context, "کد تایید $code در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    if (detectedLinks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لینک‌های پرداخت و قبوض استخراج شده",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(detectedLinks) { linkItem ->
                            PaymentLinkCard(
                                analysis = linkItem,
                                onOpenLink = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "خطا در باز کردن لینک", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    if (detectedOtps.isEmpty() && detectedLinks.isEmpty()) {
                        item {
                            EmptyBankState(
                                title = "کد تایید یا لینکی یافت نشد",
                                description = "کدهای ورود و لینک‌های پرداخت پیامک‌های بانکی پس از دریافت به صورت هوشمند استخراج و جدول‌بندی می‌شوند."
                            )
                        }
                    }
                }

                // Tab 3: Cards & Balances
                if (activeTab == 3) {
                    if (detectedCards.isEmpty()) {
                        item {
                            EmptyBankState(
                                title = "کارت بانکی ثبت‌نشده است",
                                description = "با دریافت اولین پیامک دارای شماره کارت یا اعلام موجودی، کارت‌های شما به همراه آخرین مانده حساب نمایش داده می‌شوند."
                            )
                        }
                    } else {
                        items(detectedCards) { card ->
                            BankCardDisplay(
                                card = card,
                                usePersianDigits = usePersianDigits,
                                usePersianCalendar = usePersianCalendar
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FinancialOverviewCard(
    totalBalance: String,
    totalIncome: String,
    totalExpense: String,
    netSavings: String,
    usePersianDigits: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "داشبورد هوشمند بانکی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "آنالیز خودکار SMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Main Balance Highlight
            Column {
                Text(
                    text = "مجموع موجودی شناخته‌شده",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = totalBalance,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))

            // Income & Expense Split
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income Block
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مجموع واریزی‌ها",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = totalIncome,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Expense Block
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مجموع برداشت‌ها",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = totalExpense,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialChartsCard(
    incomeTomans: Long,
    expenseTomans: Long,
    bankAnalyses: List<BankSmsAnalysis>,
    usePersianDigits: Boolean
) {
    val totalVolume = (incomeTomans + expenseTomans).coerceAtLeast(1L)
    val incomePercent = ((incomeTomans.toDouble() / totalVolume) * 100).toInt()
    val expensePercent = 100 - incomePercent

    val bankDistribution = remember(bankAnalyses) {
        bankAnalyses.groupBy { it.bankName }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(4)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آمار و نسبت تراکنش‌های بانکی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Custom Income vs Expense Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (usePersianDigits) "واریز: ${PersianUtils.toPersianDigits("$incomePercent%")}" else "واریز: $incomePercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = if (usePersianDigits) "برداشت: ${PersianUtils.toPersianDigits("$expensePercent%")}" else "برداشت: $expensePercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    if (incomePercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(incomePercent.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF4CAF50))
                        )
                    }
                    if (expensePercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(expensePercent.toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFFE53935))
                        )
                    }
                }
            }

            // Bank Distribution Breakdown
            if (bankDistribution.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Text(
                    text = "توزیع پیامک‌ها بر اساس بانک‌ها",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bankDistribution.forEach { (bankName, count) ->
                        val ratio = count.toFloat() / bankAnalyses.size.coerceAtLeast(1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bankName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(100.dp)
                            )
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits("$count پیامک") else "$count پیامک",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BankTransactionItemCard(
    analysis: BankSmsAnalysis,
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean,
    onCopyOtp: (String) -> Unit,
    onCopyTracking: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val iconColor = when (analysis.transactionType) {
        TransactionType.CREDIT -> Color(0xFF2E7D32)
        TransactionType.DEBIT -> Color(0xFFC62828)
        TransactionType.OTP -> Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when (analysis.transactionType) {
        TransactionType.CREDIT -> Icons.Default.ArrowDownward
        TransactionType.DEBIT -> Icons.Default.ArrowUpward
        TransactionType.OTP -> Icons.Default.VpnKey
        TransactionType.BALANCE_INQUIRY -> Icons.Default.AccountBalanceWallet
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }

    val typeLabel = when (analysis.transactionType) {
        TransactionType.CREDIT -> "واریز به حساب"
        TransactionType.DEBIT -> "برداشت از حساب"
        TransactionType.OTP -> "رمز پویا / کد تایید"
        TransactionType.BALANCE_INQUIRY -> "اعلام موجودی"
        else -> "اطلاعیه بانکی"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("bank_transaction_item_${analysis.messageId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = analysis.bankName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        val formattedAmt = analysis.formattedAmount
                        if (formattedAmt != null) {
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits(formattedAmt) else formattedAmt,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = iconColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = PersianUtils.formatTimestamp(analysis.timestamp, usePersianCalendar, usePersianDigits),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Quick Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val cardNum = analysis.cardNumber
                if (!cardNum.isNullOrBlank()) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = if (usePersianDigits) PersianUtils.toPersianDigits("کارت: $cardNum") else "کارت: $cardNum",
                                fontSize = 10.sp
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }

                val otpCode = analysis.otpCode
                if (!otpCode.isNullOrBlank()) {
                    AssistChip(
                        onClick = { onCopyOtp(otpCode) },
                        label = {
                            Text(
                                text = if (usePersianDigits) "کپی: ${PersianUtils.toPersianDigits(otpCode)}" else "کپی: $otpCode",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }

                val trackingNum = analysis.trackingNumber
                if (!trackingNum.isNullOrBlank()) {
                    AssistChip(
                        onClick = { onCopyTracking(trackingNum) },
                        label = {
                            Text(
                                text = if (usePersianDigits) "پیگیری: ${PersianUtils.toPersianDigits(trackingNum)}" else "پیگیری: $trackingNum",
                                fontSize = 10.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (expanded) "بستن متن" else "متن پیامک",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expandable Original Message Body
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "متن کامل پیامک:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = analysis.rawBody,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OtpCodeCard(
    otp: BankSmsAnalysis,
    usePersianDigits: Boolean,
    onCopy: (String) -> Unit
) {
    val code = otp.otpCode ?: ""
    val formattedCode = if (usePersianDigits) PersianUtils.toPersianDigits(code) else code

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = otp.bankName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = formattedCode,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = { onCopy(code) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("کپی کد", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PaymentLinkCard(
    analysis: BankSmsAnalysis,
    onOpenLink: (String) -> Unit
) {
    val url = analysis.paymentLink ?: ""

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = analysis.bankName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = url,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = { onOpenLink(url) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("باز کردن", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun BankCardDisplay(
    card: BankSmsAnalysis,
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean
) {
    val cardGradient = when {
        card.bankName.contains("ملی") -> Brush.horizontalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))
        card.bankName.contains("ملت") -> Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFD32F2F)))
        card.bankName.contains("سامان") -> Brush.horizontalGradient(listOf(Color(0xFF006064), Color(0xFF00838F)))
        card.bankName.contains("بلو") -> Brush.horizontalGradient(listOf(Color(0xFF0288D1), Color(0xFF26C6DA)))
        card.bankName.contains("پاسارگاد") -> Brush.horizontalGradient(listOf(Color(0xFF212121), Color(0xFF424242)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF1A237E), Color(0xFF3F51B5)))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.bankName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Card Number
                Text(
                    text = if (usePersianDigits) PersianUtils.toPersianDigits(card.cardNumber ?: "****  ****  ****  ****") else (card.cardNumber ?: "****  ****  ****  ****"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "آخرین موجودی استخراج شده",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        val formattedBal = card.formattedBalance
                        Text(
                            text = if (formattedBal != null) {
                                if (usePersianDigits) PersianUtils.toPersianDigits(formattedBal) else formattedBal
                            } else "نامشخص",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = PersianUtils.formatTimestamp(card.timestamp, usePersianCalendar, usePersianDigits),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBankState(
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
