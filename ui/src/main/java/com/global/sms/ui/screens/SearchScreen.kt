package com.global.sms.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.global.sms.core.util.PersianUtils
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.SearchHistoryEntity
import com.global.sms.ui.theme.BankCategoryColor
import com.global.sms.ui.theme.ImportantCategoryColor
import com.global.sms.ui.theme.PrivateCategoryColor
import com.global.sms.ui.theme.SpamCategoryColor
import com.global.sms.ui.theme.WorkCategoryColor
import com.global.sms.ui.viewmodels.GlobalSmsViewModel
import com.global.sms.ui.viewmodels.SearchFilterState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: GlobalSmsViewModel,
    onBack: () -> Unit,
    onOpenThread: (Long) -> Unit
) {
    val filterState by viewModel.searchFilterState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val usePersianDigits by viewModel.usePersianDigits.collectAsStateWithLifecycle()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsStateWithLifecycle()

    var showFilterBottomSheet by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = filterState.query,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("جستجوی متنی، فرستنده، رمز، بانک...") },
                        singleLine = true,
                        trailingIcon = {
                            if (filterState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.saveCurrentSearchToHistory()
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_bar_input")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("search_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFilterBottomSheet = true },
                        modifier = Modifier.testTag("open_filter_sheet_button")
                    ) {
                        if (filterState.activeFilterCount > 0) {
                            BadgedBox(badge = { Badge { Text(filterState.activeFilterCount.toString()) } }) {
                                Icon(Icons.Default.FilterList, contentDescription = "فیلترهای پیشرفته")
                            }
                        } else {
                            Icon(Icons.Default.FilterList, contentDescription = "فیلترهای پیشرفته")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Quick Filter Chips
            QuickFilterChipsRow(
                filterState = filterState,
                onUpdateFilter = { viewModel.updateSearchFilter(it) }
            )

            if (filterState.isEmpty) {
                // Show Recent Searches & Suggestions
                RecentSearchesSection(
                    recentSearches = recentSearches,
                    onSelectQuery = { query ->
                        viewModel.setSearchQuery(query)
                        viewModel.saveCurrentSearchToHistory()
                    },
                    onDeleteSearch = { id -> viewModel.deleteRecentSearch(id) },
                    onClearAll = { viewModel.clearRecentSearches() }
                )
            } else {
                // Results Stats Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countText = if (usePersianDigits) {
                        PersianUtils.toPersianDigits("${searchResults.size} پیامک یافت شد")
                    } else {
                        "${searchResults.size} messages found"
                    }
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (filterState.activeFilterCount > 0) {
                        TextButton(
                            onClick = { viewModel.resetSearchFilters() },
                            modifier = Modifier.testTag("reset_filters_button")
                        ) {
                            Text("پاک‌سازی فیلترها (${filterState.activeFilterCount})", fontSize = 12.sp)
                        }
                    }
                }

                if (searchResults.isEmpty()) {
                    EmptySearchResultsView()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_list")
                    ) {
                        items(searchResults, key = { it.id }) { message ->
                            SearchResultCard(
                                message = message,
                                query = filterState.query,
                                usePersianDigits = usePersianDigits,
                                usePersianCalendar = usePersianCalendar,
                                onClick = {
                                    viewModel.saveCurrentSearchToHistory()
                                    onOpenThread(message.threadId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterBottomSheet) {
        AdvancedFilterBottomSheet(
            filterState = filterState,
            onUpdateFilter = { viewModel.updateSearchFilter(it) },
            onResetFilters = { viewModel.resetSearchFilters() },
            onDismiss = { showFilterBottomSheet = false }
        )
    }
}

@Composable
fun QuickFilterChipsRow(
    filterState: SearchFilterState,
    onUpdateFilter: ((SearchFilterState) -> SearchFilterState) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            FilterChip(
                selected = filterState.isOtpOnly,
                onClick = { onUpdateFilter { it.copy(isOtpOnly = !it.isOtpOnly) } },
                label = { Text("🔑 کد OTP") },
                modifier = Modifier.testTag("quick_filter_otp")
            )
        }
        item {
            FilterChip(
                selected = filterState.isBankOnly,
                onClick = { onUpdateFilter { it.copy(isBankOnly = !it.isBankOnly) } },
                label = { Text("🏦 تراکنش بانکی") },
                modifier = Modifier.testTag("quick_filter_bank")
            )
        }
        item {
            FilterChip(
                selected = filterState.hasAttachmentOnly,
                onClick = { onUpdateFilter { it.copy(hasAttachmentOnly = !it.hasAttachmentOnly) } },
                label = { Text("📎 عکس و پیوست") },
                modifier = Modifier.testTag("quick_filter_attachment")
            )
        }
        item {
            FilterChip(
                selected = filterState.isUnreadOnly,
                onClick = { onUpdateFilter { it.copy(isUnreadOnly = !it.isUnreadOnly) } },
                label = { Text("✉️ خوانده نشده") },
                modifier = Modifier.testTag("quick_filter_unread")
            )
        }
        item {
            FilterChip(
                selected = filterState.isPinnedOnly,
                onClick = { onUpdateFilter { it.copy(isPinnedOnly = !it.isPinnedOnly) } },
                label = { Text("📌 سنجاق شده") },
                modifier = Modifier.testTag("quick_filter_pinned")
            )
        }
        item {
            FilterChip(
                selected = filterState.includeHidden,
                onClick = { onUpdateFilter { it.copy(includeHidden = !it.includeHidden) } },
                label = { Text("🔒 گاوصندوق") },
                modifier = Modifier.testTag("quick_filter_vault")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentSearchesSection(
    recentSearches: List<SearchHistoryEntity>,
    onSelectQuery: (String) -> Unit,
    onDeleteSearch: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "جستجوهای اخیر",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (recentSearches.isNotEmpty()) {
                TextButton(onClick = onClearAll, modifier = Modifier.testTag("clear_history_button")) {
                    Text("حذف همه", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentSearches.isEmpty()) {
            Text(
                text = "تاریخچه جستجویی وجود ندارد. عبارت مورد نظر، شماره تلفن یا نام فرستنده را وارد کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recentSearches.forEach { history ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onSelectQuery(history.query) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = history.query, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDeleteSearch(history.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "حذف", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SearchResultCard(
    message: MessageEntity,
    query: String,
    usePersianDigits: Boolean,
    usePersianCalendar: Boolean,
    onClick: () -> Unit
) {
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onSurface

    val categoryColor = when (message.category) {
        MessageCategory.BANK -> BankCategoryColor
        MessageCategory.SPAM -> SpamCategoryColor
        MessageCategory.PRIVATE -> PrivateCategoryColor
        MessageCategory.WORK -> WorkCategoryColor
        MessageCategory.IMPORTANT -> ImportantCategoryColor
        else -> MaterialTheme.colorScheme.primary
    }

    val annotatedBody = buildHighlightedAnnotatedString(
        text = if (usePersianDigits) PersianUtils.toPersianDigits(message.body) else message.body,
        query = query,
        highlightColor = highlightColor,
        textColor = textColor
    )

    val annotatedAddress = buildHighlightedAnnotatedString(
        text = if (usePersianDigits) PersianUtils.toPersianDigits(message.address) else message.address,
        query = query,
        highlightColor = highlightColor,
        textColor = textColor
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("search_result_card_${message.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.category == MessageCategory.BANK) Icons.Default.AccountBalance else Icons.Default.Person,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = annotatedAddress,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = PersianUtils.formatTimestamp(message.timestamp, usePersianCalendar, usePersianDigits),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = annotatedBody,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // Tags / Badges Bar
            if (!message.otpCode.isNullOrEmpty() || message.attachmentUri != null || message.isMms || message.isPinned) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!message.otpCode.isNullOrEmpty()) {
                        val otpLabel = if (usePersianDigits) {
                            PersianUtils.toPersianDigits("کد: ${message.otpCode}")
                        } else {
                            "Code: ${message.otpCode}"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = otpLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    if (message.attachmentUri != null || message.isMms) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("پیوست", fontSize = 11.sp)
                            }
                        }
                    }

                    if (message.isPinned) {
                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchResultsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "هیچ پیامکی با مشخصات جستجو شده یافت نشد",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterBottomSheet(
    filterState: SearchFilterState,
    onUpdateFilter: ((SearchFilterState) -> SearchFilterState) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("فیلترهای پیشرفته جستجو", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onResetFilters) {
                    Text("ریست فیلترها")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sender Phone/Name Filter Input
            Text("جستجوی خاص فرستنده / شماره:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = filterState.senderQuery,
                onValueChange = { query -> onUpdateFilter { it.copy(senderQuery = query) } },
                placeholder = { Text("مثال: +98912 یا بانک ملی") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("filter_sender_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text("دسته‌بندی پیامک:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filterState.selectedCategory == null,
                        onClick = { onUpdateFilter { it.copy(selectedCategory = null) } },
                        label = { Text("همه دسته‌ها") }
                    )
                }
                items(MessageCategory.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = filterState.selectedCategory == cat,
                        onClick = { onUpdateFilter { it.copy(selectedCategory = cat) } },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Filters
            Text("محدوده زمانی:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            val now = System.currentTimeMillis()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterState.startDate != null && (now - filterState.startDate) < 86400000L * 1,
                    onClick = {
                        val start = now - 86400000L * 1
                        onUpdateFilter { it.copy(startDate = start, endDate = now) }
                    },
                    label = { Text("۲۴ ساعت گذشته") }
                )
                FilterChip(
                    selected = filterState.startDate != null && (now - filterState.startDate) < 86400000L * 7,
                    onClick = {
                        val start = now - 86400000L * 7
                        onUpdateFilter { it.copy(startDate = start, endDate = now) }
                    },
                    label = { Text("۷ روز گذشته") }
                )
                FilterChip(
                    selected = filterState.startDate != null && (now - filterState.startDate) < 86400000L * 30,
                    onClick = {
                        val start = now - 86400000L * 30
                        onUpdateFilter { it.copy(startDate = start, endDate = now) }
                    },
                    label = { Text("۳۰ روز گذشته") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_filters_button")
            ) {
                Text("اعمال فیلترها")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Builds an [AnnotatedString] that highlights occurrences of [query] in [text].
 */
fun buildHighlightedAnnotatedString(
    text: String,
    query: String,
    highlightColor: Color,
    textColor: Color
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (startIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            } else {
                // Append text before match
                if (index > startIndex) {
                    append(text.substring(startIndex, index))
                }
                // Append matched text with highlight style
                withStyle(
                    style = SpanStyle(
                        background = highlightColor,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                ) {
                    append(text.substring(index, index + query.length))
                }
                startIndex = index + query.length
            }
        }
    }
}
