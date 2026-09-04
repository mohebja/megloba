package com.global.sms.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String,
    val accentColor: Color,
    val isPermissionStep: Boolean = false
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingFlowScreen(
    onFinishOnboarding: () -> Unit,
    onRequestDefaultSms: () -> Unit = {}
) {
    val context = LocalContext.current
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "برنامه پیام‌رسان پیش‌فرض هوشمند",
                description = "برای ارسال و دریافت پیامک، همگام‌سازی تاریخچه و حفاظت در برابر فیشینگ، نیاز است برنامه به عنوان پیام‌رسان پیش‌فرض تنظیم شود.",
                icon = Icons.Default.Sms,
                badge = "الزامی و پایه",
                accentColor = Color(0xFF1E88E5),
                isPermissionStep = true
            ),
            OnboardingPage(
                title = "معماری حریم خصوصی آفلاین",
                description = "تمام داده‌های شما با الگوریتم AES-256-GCM کدگذاری شده و هیچ داده‌ای به سرورهای ابری منتقل نمی‌شود.",
                icon = Icons.Default.Security,
                badge = "۱۰۰٪ محرمانه",
                accentColor = Color(0xFF43A047)
            ),
            OnboardingPage(
                title = "هوش مصنوعی محلی و آفلاین",
                description = "پردازش متن، استخراج هزینه‌ها، دسته‌بندی هوشمند و تحلیل احساسات تماماً روی تراشه دستگاه شما انجام می‌شود.",
                icon = Icons.Default.Psychology,
                badge = "مغز هوش مصنوعی محلی",
                accentColor = Color(0xFF8E24AA)
            ),
            OnboardingPage(
                title = "گاوصندوق شخصی و بیومتریک",
                description = "پیامک‌های حساس، تراکنش‌های مالی و مخاطبین خاص خود را در گاوصندوق رمزنگاری شده با اثر انگشت قفل کنید.",
                icon = Icons.Default.Lock,
                badge = "امنیت بیومتریک",
                accentColor = Color(0xFFE53935)
            ),
            OnboardingPage(
                title = "دستیار هوشمند و تحلیل تراکنش‌ها",
                description = "پاسخ سریع هوشمند، تحلیل هوشمند پیام‌های بانکی، استخراج یادآورها و مدیریت امور کاری در یک نگاه.",
                icon = Icons.Default.AutoAwesome,
                badge = "دستیار شخصی کامل",
                accentColor = Color(0xFFFB8C00)
            )
        )
    }

    var currentPageIndex by remember { mutableIntStateOf(0) }
    val page = pages[currentPageIndex]

    // Predictable hardware/gesture back button handling: step backward if > 0, otherwise complete onboarding
    BackHandler(enabled = true) {
        if (currentPageIndex > 0) {
            currentPageIndex--
        } else {
            onFinishOnboarding()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding_screen"),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge or Title tag
                    Surface(
                        shape = CircleShape,
                        color = page.accentColor.copy(alpha = 0.15f),
                        contentColor = page.accentColor
                    ) {
                        Text(
                            text = page.badge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Skip button
                    TextButton(
                        onClick = onFinishOnboarding,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "رد شدن",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress indicators (dots)
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            pages.indices.forEach { index ->
                                val active = index == currentPageIndex
                                val width by animateDpAsState(
                                    targetValue = if (active) 28.dp else 8.dp,
                                    label = "dotWidth"
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(8.dp)
                                        .width(width)
                                        .clip(CircleShape)
                                        .background(
                                            if (active) page.accentColor
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back Button
                            if (currentPageIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentPageIndex-- },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("onboarding_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "قبلی"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("قبلی")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(100.dp))
                            }

                            // Next or Finish Button
                            Button(
                                onClick = {
                                    if (currentPageIndex == 0) {
                                        onRequestDefaultSms()
                                    }
                                    if (currentPageIndex < pages.size - 1) {
                                        currentPageIndex++
                                    } else {
                                        onFinishOnboarding()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = page.accentColor
                                ),
                                modifier = Modifier.testTag("onboarding_next_button")
                            ) {
                                Text(
                                    text = if (currentPageIndex == pages.size - 1) "شروع استفاده"
                                    else if (currentPageIndex == 0) "تنظیم و ادامه"
                                    else "بعدی",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "بعدی"
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 300 })).togetherWith(
                        fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -300 })
                    )
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) { targetPage ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustrated Graphic Box
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        targetPage.accentColor.copy(alpha = 0.35f),
                                        targetPage.accentColor.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = targetPage.accentColor,
                            modifier = Modifier.size(105.dp),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = targetPage.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = targetPage.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = targetPage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )

                    if (targetPage.isPermissionStep) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "در صورت رد مجوز، می‌توانید هر زمان از منوی «تنظیمات برنامه» یا «تنظیمات سیستم اندروید» دسترسی‌ها را فعال کنید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("باز کردن تنظیمات سیستم", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
