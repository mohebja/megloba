package com.global.sms

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.global.sms.ui.theme.GlobalSmsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Sprint5_4_FinalRegressionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInstallationAndUiThemeRendering() {
        composeTestRule.setContent {
            GlobalSmsTheme {
                Text("Global SMS v5.4.0 Release Candidate")
            }
        }
        composeTestRule.onNodeWithText("Global SMS v5.4.0 Release Candidate").assertIsDisplayed()
    }
}



