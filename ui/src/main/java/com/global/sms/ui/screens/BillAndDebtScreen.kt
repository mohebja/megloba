package com.global.sms.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.debt.BillAndDebtItem
import com.global.sms.core.debt.BillAndDebtTracker
import com.global.sms.core.debt.DebtType
import com.global.sms.core.util.PersianUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillAndDebtScreen(
    usePersianDigits: Boolean = true,
    usePersianCalendar: Boolean = true,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val trackedItems by BillAndDebtTracker.trackedItems.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredItems = remember(trackedItems, selectedTab) {
        when (selectedTab) {
            1 -> trackedItems.filter { it.type == DebtType.UTILITY_BILL }
            2 -> trackedItems.filter { it.type == DebtType.LOAN_INSTALLMENT }
            3 -> trackedItems.filter { it.type == DebtType.SAYAD_CHECK }
            else -> trackedItems
        }
    }

    val unpaidTotalTomans = remember(trackedItems) {
        trackedItems.filter { !it.isPaid }.mapNotNull { it.amountTomans }.sum()
    }
    val unpaidCount = remember(trackedItems) {
        trackedItems.count { !it.isPaid }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مرکز یادآوری قبوض، اقساط و چک‌ها",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits("$unpaidCount مورد پرداخت‌نشده") else "$unpaidCount مورد پرداخت‌نشده",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_debt")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مجموع تعهدات مالی باز:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = BillAndDebtTracker.formatMoneyTomans(unpaidTotalTomans, usePersianDigits),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tabs Row
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("همه (${trackedItems.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("قبوض", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("اقساط وام", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("چک صیادی", fontSize = 11.sp) }
                )
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تعهد مالی یا قبض ثبت‌شده‌ای یافت نشد.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "پیامک‌های جدید قبوض و اقساط به طور خودکار شناسایی و در این بخش نمایش داده می‌شوند.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        DebtItemCard(
                            item = item,
                            usePersianDigits = usePersianDigits,
                            onTogglePaid = { BillAndDebtTracker.togglePaidStatus(item.id) },
                            onDelete = { BillAndDebtTracker.removeItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtItemCard(
    item: BillAndDebtItem,
    usePersianDigits: Boolean,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isPaid) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Paid status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (item.type) {
                        DebtType.UTILITY_BILL -> Icons.Default.FlashOn
                        DebtType.LOAN_INSTALLMENT -> Icons.Default.AccountBalance
                        DebtType.SAYAD_CHECK -> Icons.Default.Assignment
                        else -> Icons.Default.Receipt
                    },
                    contentDescription = null,
                    tint = if (item.isPaid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                FilterChip(
                    selected = item.isPaid,
                    onClick = onTogglePaid,
                    label = {
                        Text(
                            text = if (item.isPaid) "پرداخت شده" else "در انتظار پرداخت",
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (item.isPaid) Icons.Default.Check else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount & Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مبلغ قابل پرداخت:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BillAndDebtTracker.formatMoneyTomans(item.amountTomans, usePersianDigits),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (item.isPaid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                    )
                }

                val dueDate = item.formattedDueDate
                if (dueDate != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("مهلت سررسید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (usePersianDigits) PersianUtils.toPersianDigits(dueDate) else dueDate,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Identifiers Row (Bill ID / Pay ID / Check ID)
            val bId = item.billId
            val pId = item.paymentId
            if (bId != null && pId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شناسه قبض: ${if (usePersianDigits) PersianUtils.toPersianDigits(bId) else bId}  |  شناسه پرداخت: ${if (usePersianDigits) PersianUtils.toPersianDigits(pId) else pId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Bill IDs", "شناسه قبض: $bId\nشناسه پرداخت: $pId")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "شناسه‌های قبض کپی شدند", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "کپی شناسه‌ها", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            val cId = item.checkId
            if (cId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شناسه ۱۶ رقمی صیادی: ${if (usePersianDigits) PersianUtils.toPersianDigits(cId) else cId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Sayad Check ID", cId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "شناسه صیادی کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "کپی شناسه صیادی", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Calendar, USSD, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar button
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = BillAndDebtTracker.createCalendarReminderIntent(item)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "برنامه تقویم در دسترس نیست", Toast.LENGTH_SHORT).show()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ثبت تقویم", fontSize = 11.sp)
                }

                // USSD payment for utility bills
                if (item.billId != null && item.paymentId != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = BillAndDebtTracker.createPaymentUssdIntent(item)
                                if (intent != null) context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "امکان شماره‌گیری مستقیم وجود ندارد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پرداخت سریع", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
