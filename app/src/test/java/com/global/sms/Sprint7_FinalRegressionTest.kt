package com.global.sms

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.global.sms.ui.screens.AiChatAssistantScreen
import com.global.sms.ui.screens.AiHomeDashboardScreen
import com.global.sms.ui.screens.OnboardingFlowScreen
import com.global.sms.ui.viewmodels.DashboardViewModel
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], manifest = Config.NONE)
class Sprint7_FinalRegressionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Application

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testOnboardingScreenRendering() {
        composeTestRule.setContent {
            OnboardingFlowScreen(
                onFinishOnboarding = {}
            )
        }

        composeTestRule.onNodeWithTag("onboarding_screen").assertExists()
        composeTestRule.onNodeWithTag("onboarding_skip_button").assertExists()
        composeTestRule.onNodeWithTag("onboarding_next_button").assertExists()
    }

    @Test
    fun testAiHomeDashboardScreenRendering() {
        val viewModel = DashboardViewModel(context)
        composeTestRule.setContent {
            AiHomeDashboardScreen(
                dashboardViewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag("ai_home_dashboard_screen", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("today_summary_header_card", useUnmergedTree = true).assertExists()
    }

    @Test
    fun testAiChatAssistantScreenRendering() {
        composeTestRule.setContent {
            AiChatAssistantScreen(
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithTag("ai_chat_assistant_screen").assertExists()
        composeTestRule.onNodeWithTag("ai_chat_input").assertExists()
        composeTestRule.onNodeWithTag("ai_chat_send_button").assertExists()
    }
}
