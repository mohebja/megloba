package com.global.sms.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeStyle {
    CLASSIC_LIGHT,
    PREMIUM_DARK,
    AMOLED_BLACK,
    PERSIAN_CYAN,
    EMERALD_GREEN,
    ROYAL_PURPLE,
    MATERIAL_YOU,
    USER_CUSTOM
}

data class DynamicThemeConfig(
    val id: String,
    val name: String,
    val style: ThemeStyle,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val backgroundColorHex: String,
    val surfaceColorHex: String,
    val isDark: Boolean,
    val isAmoled: Boolean = false,
    val gradientColorsHex: List<String> = emptyList(),
    val isMarketplacePreset: Boolean = false
)

object DynamicThemeEngine {

    val defaultThemes = listOf(
        DynamicThemeConfig(
            id = "theme_classic",
            name = "کلاسیک روشن",
            style = ThemeStyle.CLASSIC_LIGHT,
            primaryColorHex = "#2196F3",
            secondaryColorHex = "#03A9F4",
            backgroundColorHex = "#F5F5F5",
            surfaceColorHex = "#FFFFFF",
            isDark = false,
            isMarketplacePreset = true
        ),
        DynamicThemeConfig(
            id = "theme_dark",
            name = "تاریک پرمیوم",
            style = ThemeStyle.PREMIUM_DARK,
            primaryColorHex = "#3F51B5",
            secondaryColorHex = "#7986CB",
            backgroundColorHex = "#121212",
            surfaceColorHex = "#1E1E1E",
            isDark = true,
            isMarketplacePreset = true
        ),
        DynamicThemeConfig(
            id = "theme_amoled",
            name = "مشکی مشکی (AMOLED)",
            style = ThemeStyle.AMOLED_BLACK,
            primaryColorHex = "#00E676",
            secondaryColorHex = "#00B0FF",
            backgroundColorHex = "#000000",
            surfaceColorHex = "#0A0A0A",
            isDark = true,
            isAmoled = true,
            isMarketplacePreset = true
        ),
        DynamicThemeConfig(
            id = "theme_persian_cyan",
            name = "فیروزه‌ای ایرانی",
            style = ThemeStyle.PERSIAN_CYAN,
            primaryColorHex = "#00838F",
            secondaryColorHex = "#00ACC1",
            backgroundColorHex = "#E0F7FA",
            surfaceColorHex = "#FFFFFF",
            isDark = false,
            gradientColorsHex = listOf("#00838F", "#00E5FF"),
            isMarketplacePreset = true
        ),
        DynamicThemeConfig(
            id = "theme_emerald",
            name = "زمردی ارشد",
            style = ThemeStyle.EMERALD_GREEN,
            primaryColorHex = "#2E7D32",
            secondaryColorHex = "#4CAF50",
            backgroundColorHex = "#E8F5E9",
            surfaceColorHex = "#FFFFFF",
            isDark = false,
            isMarketplacePreset = true
        )
    )

    fun hexToColor(hex: String, default: Color = Color.Unspecified): Color {
        return try {
            val cleanHex = hex.replace("#", "")
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorInt)
            } else if (cleanHex.length == 8) {
                Color(colorInt)
            } else default
        } catch (e: Exception) {
            default
        }
    }
}
