package com.global.sms

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.global.sms.ui.classic.components.ClassicThreadCard
import com.global.sms.ui.enterprise.components.EnterpriseKpiCard
import com.global.sms.ui.smart.components.AiSummaryCard
import com.global.sms.ui.theme.GlobalSmsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiArchitectureAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAllUiSystemsRendering() {
        composeTestRule.setContent {
            GlobalSmsTheme {
                Surface {
                    Column {
                        ClassicThreadCard(
                            conversation = com.global.sms.data.entity.ConversationEntity(
                                threadId = 1L,
                                address = "+989120000000",
                                contactName = "رضا محمدی",
                                lastMessage = "سلام، لطفاً فایل صورتحساب را ارسال کنید.",
                                unreadCount = 2
                            ),
                            onClick = {}
                        )

                        AiSummaryCard(
                            summaryText = "۲ پیامک رمز دوم دریافت شد و ۱ تراکنش واریزی بانکی ثبت گردید.",
                            onDismiss = {}
                        )

                        EnterpriseKpiCard(
                            title = "نرخ تحویل پیامک انبوه",
                            value = "۹۹.۴٪",
                            subtitle = "۱۲,۴۵۰ پیامک ارسال شده امروز",
                            icon = Icons.Default.Email
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("رضا محمدی").assertIsDisplayed()
        composeTestRule.onNodeWithText("۲ پیامک رمز دوم", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("نرخ تحویل پیامک انبوه").assertIsDisplayed()
    }
}


