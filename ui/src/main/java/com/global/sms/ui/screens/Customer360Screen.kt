package com.global.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.data.entity.CrmCustomerEntity
import com.global.sms.ui.viewmodels.EnterpriseViewModel

enum class CustomerLifecycleStage {
    LEAD,
    CUSTOMER,
    VIP,
    INACTIVE
}

data class TimelineEvent(
    val id: String,
    val type: String, // "SMS", "CALL", "TASK", "NOTE", "AI_INSIGHT"
    val timestamp: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Customer360Screen(
    customerId: Long = 0L,
    viewModel: EnterpriseViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val customers by viewModel?.customers?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList<CrmCustomerEntity>()) }
    val activeCustomer = remember(customers, customerId) {
        customers.find { it.id == customerId } ?: customers.firstOrNull() ?: CrmCustomerEntity(
            id = 1L,
            name = "مهندس علیرضا رضایی",
            phoneNumber = "09121112233",
            company = "شرکت همراه سازان پیشرو",
            customerStatus = "VIP",
            tags = "VIP, مشتری ویژه"
        )
    }

    val activeStage = remember(activeCustomer.customerStatus) {
        when (activeCustomer.customerStatus.uppercase()) {
            "LEAD" -> CustomerLifecycleStage.LEAD
            "CUSTOMER", "ACTIVE" -> CustomerLifecycleStage.CUSTOMER
            "VIP" -> CustomerLifecycleStage.VIP
            "INACTIVE" -> CustomerLifecycleStage.INACTIVE
            else -> CustomerLifecycleStage.CUSTOMER
        }
    }

    val timelineEvents = remember(activeCustomer) {
        listOf(
            TimelineEvent("e1", "AI_INSIGHT", "۱۰ دقیقه پیش", "هوش مصنوعی: احتمال بالای خرید سرویس سازمانی بر اساس سابقه مخاطب ${activeCustomer.name}", Icons.Default.Psychology),
            TimelineEvent("e2", "SMS", "امروز ۱۱:۳۰", "پیامک: فاکتور خرید تایید شد، جهت هماهنگی با شماره ${activeCustomer.phoneNumber} تماس حاصل فرمایید.", Icons.AutoMirrored.Filled.Message),
            TimelineEvent("e3", "CALL", "دیروز ۱۵:۴۵", "تماس خروجی موفق - مدت زمان: ۴ دقیقه", Icons.Default.Phone),
            TimelineEvent("e4", "TASK", "دیروز ۱۰:۰۰", "وظیفه: پیگیری تمدید قرارداد سالانه (${activeCustomer.company ?: "سازمان"})", Icons.Default.TaskAlt),
            TimelineEvent("e5", "NOTE", "۲ روز پیش", "یادداشت: تگ‌های اختصاصی مشتری [${activeCustomer.tags}] اعمال گردید.", Icons.AutoMirrored.Filled.Note)
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("customer_360_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "نمای ۳۶۰ درجه مشتری و چرخه عمر (CRM 2.0)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("customer_360_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Profile Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = activeCustomer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = "${activeCustomer.phoneNumber} | ${activeCustomer.company ?: "شخصی"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (activeCustomer.tags.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "برچسب‌ها: ${activeCustomer.tags}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Customer Lifecycle selector
                item {
                    Text(
                        text = "مرحله چرخه عمر مشتری (Customer Lifecycle Stage)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomerLifecycleStage.values().forEach { stage ->
                            val isSelected = activeStage == stage
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newStatus = when (stage) {
                                        CustomerLifecycleStage.LEAD -> "LEAD"
                                        CustomerLifecycleStage.CUSTOMER -> "ACTIVE"
                                        CustomerLifecycleStage.VIP -> "VIP"
                                        CustomerLifecycleStage.INACTIVE -> "INACTIVE"
                                    }
                                    if (activeCustomer.id != 0L) {
                                        viewModel?.updateCustomerLifecycle(activeCustomer.id, newStatus)
                                    }
                                },
                                label = {
                                    Text(
                                        text = when (stage) {
                                            CustomerLifecycleStage.LEAD -> "لید (Lead)"
                                            CustomerLifecycleStage.CUSTOMER -> "مشتری عادی"
                                            CustomerLifecycleStage.VIP -> "مشتری VIP"
                                            CustomerLifecycleStage.INACTIVE -> "غیرفعال"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                // Timeline Section
                item {
                    Text(
                        text = "تایم‌لاین تعاملات و تاریخچه ۳۶۰ درجه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(timelineEvents, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = when (event.type) {
                                            "AI_INSIGHT" -> MaterialTheme.colorScheme.primaryContainer
                                            "SMS" -> Color(0xFFE3F2FD)
                                            "CALL" -> Color(0xFFE8F5E9)
                                            "TASK" -> Color(0xFFFFF3E0)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = event.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = event.type, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = event.timestamp, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
