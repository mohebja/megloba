package com.global.sms.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.global.sms.core.haptic.HapticFeedbackManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamouflageCalculatorScreen(
    onNavigateBack: () -> Unit,
    onUnlockVault: () -> Unit,
    secretPin: String = "1234",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var display by remember { mutableStateOf("0") }
    var currentInputSequence by remember { mutableStateOf("") }

    fun onDigit(d: String) {
        HapticFeedbackManager.vibrateClick(context)
        if (display == "0" || display == "خطا") {
            display = d
        } else {
            display += d
        }
        currentInputSequence += d
    }

    fun onOperator(op: String) {
        HapticFeedbackManager.vibrateClick(context)
        if (!display.endsWith(" ") && display != "خطا") {
            display += " $op "
        }
    }

    fun onClear() {
        HapticFeedbackManager.vibrateClick(context)
        display = "0"
        currentInputSequence = ""
    }

    fun onEquals() {
        // Check secret pin trigger for Ghost Vault
        if (currentInputSequence == secretPin || display == secretPin) {
            HapticFeedbackManager.vibrateVaultUnlocked(context)
            Toast.makeText(context, "صندوقچه استتاری با موفقیت بازگشایی شد", Toast.LENGTH_SHORT).show()
            onUnlockVault()
            return
        }

        try {
            val parts = display.split(" ").filter { it.isNotBlank() }
            if (parts.size == 3) {
                val num1 = parts[0].toDoubleOrNull() ?: 0.0
                val op = parts[1]
                val num2 = parts[2].toDoubleOrNull() ?: 0.0
                val res = when (op) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "×" -> num1 * num2
                    "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                    else -> 0.0
                }
                display = if (res.isNaN()) "خطا" else if (res % 1 == 0.0) res.toLong().toString() else res.toString()
            }
        } catch (e: Exception) {
            display = "خطا"
        }
        currentInputSequence = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ماشین‌حساب (حالت استتار)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        modifier = modifier.testTag("camouflage_calculator_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Calculator Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = display,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("calc_display")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Keypad Grid
            val buttons = listOf(
                listOf("C", "÷", "×", "-"),
                listOf("7", "8", "9", "+"),
                listOf("4", "5", "6", "="),
                listOf("1", "2", "3", "0")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { label ->
                        val isOp = label in listOf("+", "-", "×", "÷", "=")
                        val isClear = label == "C"

                        val btnColor = when {
                            label == "=" -> MaterialTheme.colorScheme.primary
                            isOp -> MaterialTheme.colorScheme.secondaryContainer
                            isClear -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val textColor = when {
                            label == "=" -> MaterialTheme.colorScheme.onPrimary
                            isOp -> MaterialTheme.colorScheme.onSecondaryContainer
                            isClear -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Button(
                            onClick = {
                                when (label) {
                                    "C" -> onClear()
                                    "=" -> onEquals()
                                    "+", "-", "×", "÷" -> onOperator(label)
                                    else -> onDigit(label)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("calc_btn_$label")
                        ) {
                            Text(
                                text = label,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
