package com.global.sms.ui.theme

import androidx.compose.ui.graphics.Color

data class CustomThemePalette(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val incomingBubble: Color,
    val outgoingBubble: Color,
    val incomingText: Color,
    val outgoingText: Color,
    val isDark: Boolean = false,
    val isAmoled: Boolean = false
)

object ColorPaletteRepository {

    val palettes: List<CustomThemePalette> = listOf(
        // 1. Classic Blue
        CustomThemePalette(
            id = "classic_blue",
            name = "آبی کلاسیک (Persian Blue)",
            primary = Color(0xFF1A73E8),
            secondary = Color(0xFF1565C0),
            tertiary = Color(0xFF0D47A1),
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE8F0FE),
            outgoingBubble = Color(0xFF1A73E8),
            incomingText = Color(0xFF202124),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 2. Emerald Teal
        CustomThemePalette(
            id = "emerald_teal",
            name = "زمردی (Emerald Teal)",
            primary = Color(0xFF00897B),
            secondary = Color(0xFF00695C),
            tertiary = Color(0xFF004D40),
            background = Color(0xFFF2F9F8),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE0F2F1),
            outgoingBubble = Color(0xFF00897B),
            incomingText = Color(0xFF00332C),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 3. Twilight Purple
        CustomThemePalette(
            id = "twilight_purple",
            name = "ارغوانی غروب (Twilight Purple)",
            primary = Color(0xFF7B1FA2),
            secondary = Color(0xFF6A1B9A),
            tertiary = Color(0xFF4A148C),
            background = Color(0xFFFAF5FC),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFF3E5F5),
            outgoingBubble = Color(0xFF7B1FA2),
            incomingText = Color(0xFF311B92),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 4. Sunset Orange
        CustomThemePalette(
            id = "sunset_orange",
            name = "نارنجی غروب (Sunset Orange)",
            primary = Color(0xFFE65100),
            secondary = Color(0xFFEF6C00),
            tertiary = Color(0xFFF57C00),
            background = Color(0xFFFFF8F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFE0B2),
            outgoingBubble = Color(0xFFE65100),
            incomingText = Color(0xFF3E2723),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 5. Crimson Red
        CustomThemePalette(
            id = "crimson_red",
            name = "سرخ یاقوتی (Crimson Red)",
            primary = Color(0xFFC62828),
            secondary = Color(0xFFB71C1C),
            tertiary = Color(0xFF880E4F),
            background = Color(0xFFFFF5F5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFEBEE),
            outgoingBubble = Color(0xFFC62828),
            incomingText = Color(0xFF3E2723),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 6. Royal Gold
        CustomThemePalette(
            id = "royal_gold",
            name = "طلایی سلطنتی (Royal Gold)",
            primary = Color(0xFFD4AF37),
            secondary = Color(0xFFB8860B),
            tertiary = Color(0xFF996515),
            background = Color(0xFFFCFBF7),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFF8E1),
            outgoingBubble = Color(0xFFB8860B),
            incomingText = Color(0xFF261C00),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 7. Dark Sapphire
        CustomThemePalette(
            id = "dark_sapphire",
            name = "یاقوت کبود شب (Dark Sapphire)",
            primary = Color(0xFF42A5F5),
            secondary = Color(0xFF1E88E5),
            tertiary = Color(0xFF1565C0),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            incomingBubble = Color(0xFF334155),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFFF8FAFC),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 8. AMOLED Pitch Black
        CustomThemePalette(
            id = "amoled_black",
            name = "مشکی عمیق (AMOLED Pitch Black)",
            primary = Color(0xFF3B82F6),
            secondary = Color(0xFF60A5FA),
            tertiary = Color(0xFF93C5FD),
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            incomingBubble = Color(0xFF1E1E1E),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFFFFFFFF),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 9. Cyber Neon Dark
        CustomThemePalette(
            id = "cyber_neon",
            name = "سایبر نئون (Cyber Neon)",
            primary = Color(0xFF00E676),
            secondary = Color(0xFF00B0FF),
            tertiary = Color(0xFFD500F9),
            background = Color(0xFF0A0A12),
            surface = Color(0xFF141424),
            incomingBubble = Color(0xFF1F1F38),
            outgoingBubble = Color(0xFF00C853),
            incomingText = Color(0xFFE0E0E0),
            outgoingText = Color(0xFF000000),
            isDark = true
        ),
        // 10. Rose Gold
        CustomThemePalette(
            id = "rose_gold",
            name = "رز گلد (Rose Gold)",
            primary = Color(0xFFB76E79),
            secondary = Color(0xFFC98CA7),
            tertiary = Color(0xFFD8A7B1),
            background = Color(0xFFFFF9FA),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFCE4EC),
            outgoingBubble = Color(0xFFB76E79),
            incomingText = Color(0xFF4A2E35),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 11. Nordic Frost
        CustomThemePalette(
            id = "nordic_frost",
            name = "یخ شمالی (Nordic Frost)",
            primary = Color(0xFF5E81AC),
            secondary = Color(0xFF81A1C1),
            tertiary = Color(0xFF88C0D0),
            background = Color(0xFFECEFF4),
            surface = Color(0xFFE5E9F0),
            incomingBubble = Color(0xFFD8DEE9),
            outgoingBubble = Color(0xFF5E81AC),
            incomingText = Color(0xFF2E3440),
            outgoingText = Color(0xFFECEFF4)
        ),
        // 12. Mint Green
        CustomThemePalette(
            id = "mint_green",
            name = "نعناعی تازه‌ساز (Mint Green)",
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF388E3C),
            tertiary = Color(0xFF4CAF50),
            background = Color(0xFFF1F8E9),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDCEDC8),
            outgoingBubble = Color(0xFF2E7D32),
            incomingText = Color(0xFF1B5E20),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 13. Coral Pink
        CustomThemePalette(
            id = "coral_pink",
            name = "مرجانی (Coral Pink)",
            primary = Color(0xFFF4511E),
            secondary = Color(0xFFFF7043),
            tertiary = Color(0xFFFF8A65),
            background = Color(0xFFFBE9E7),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFCCBC),
            outgoingBubble = Color(0xFFF4511E),
            incomingText = Color(0xFFBF360C),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 14. Charcoal Obsidian
        CustomThemePalette(
            id = "charcoal_obsidian",
            name = "ذغالی آتشفشانی (Charcoal Dark)",
            primary = Color(0xFF818CF8),
            secondary = Color(0xFFA5B4FC),
            tertiary = Color(0xFFC7D2FE),
            background = Color(0xFF18181B),
            surface = Color(0xFF27272A),
            incomingBubble = Color(0xFF3F3F46),
            outgoingBubble = Color(0xFF4F46E5),
            incomingText = Color(0xFFFAFAFA),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 15. Slate Gray
        CustomThemePalette(
            id = "slate_gray",
            name = "سنگی مدرن (Slate Gray)",
            primary = Color(0xFF475569),
            secondary = Color(0xFF64748B),
            tertiary = Color(0xFF94A3B8),
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE2E8F0),
            outgoingBubble = Color(0xFF475569),
            incomingText = Color(0xFF0F172A),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 16. Desert Warm
        CustomThemePalette(
            id = "desert_warm",
            name = "کویر گرم (Desert Warm)",
            primary = Color(0xFFA0522D),
            secondary = Color(0xFFCD853F),
            tertiary = Color(0xFFDEB887),
            background = Color(0xFFFAF0E6),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFE4C4),
            outgoingBubble = Color(0xFFA0522D),
            incomingText = Color(0xFF4A2511),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 17. Deep Amethyst
        CustomThemePalette(
            id = "deep_amethyst",
            name = "آمتیست تیره (Deep Amethyst)",
            primary = Color(0xFFA855F7),
            secondary = Color(0xFFC084FC),
            tertiary = Color(0xFFE9D5FF),
            background = Color(0xFF170F23),
            surface = Color(0xFF26193C),
            incomingBubble = Color(0xFF3B275B),
            outgoingBubble = Color(0xFF9333EA),
            incomingText = Color(0xFFFAF5FF),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 18. Ocean Wave
        CustomThemePalette(
            id = "ocean_wave",
            name = "موج اقیانوس (Ocean Wave)",
            primary = Color(0xFF0284C7),
            secondary = Color(0xFF38BDF8),
            tertiary = Color(0xFF7DD3FC),
            background = Color(0xFFF0F9FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE0F2FE),
            outgoingBubble = Color(0xFF0284C7),
            incomingText = Color(0xFF0C4A6E),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 19. Mocha Coffee
        CustomThemePalette(
            id = "mocha_coffee",
            name = "قهوه موکا (Mocha Coffee)",
            primary = Color(0xFF6D4C41),
            secondary = Color(0xFF8D6E63),
            tertiary = Color(0xFFA1887F),
            background = Color(0xFFEFEBE9),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xD7CCC8),
            outgoingBubble = Color(0xFF6D4C41),
            incomingText = Color(0xFF3E2723),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 20. Lavender Dream
        CustomThemePalette(
            id = "lavender_dream",
            name = "رؤیای اسطوخودوس (Lavender)",
            primary = Color(0xFF8E24AA),
            secondary = Color(0xFFAB47BC),
            tertiary = Color(0xFFCE93D8),
            background = Color(0xFFF3E5F5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE1BEE7),
            outgoingBubble = Color(0xFF8E24AA),
            incomingText = Color(0xFF4A148C),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 21. Electric Cyan
        CustomThemePalette(
            id = "electric_cyan",
            name = "سیان الکتریک (Electric Cyan)",
            primary = Color(0xFF06B6D4),
            secondary = Color(0xFF22D3EE),
            tertiary = Color(0xFF67E8F9),
            background = Color(0xFFECFEFF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFCFFAFE),
            outgoingBubble = Color(0xFF0891B2),
            incomingText = Color(0xFF164E63),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 22. Ruby Velvet
        CustomThemePalette(
            id = "ruby_velvet",
            name = "مخمل یاقوتی (Ruby Velvet)",
            primary = Color(0xFF880E4F),
            secondary = Color(0xFFAD1457),
            tertiary = Color(0xFFC2185B),
            background = Color(0xFFFCE4EC),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFF8BBD0),
            outgoingBubble = Color(0xFF880E4F),
            incomingText = Color(0xFF4A148C),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 23. Forest Pine
        CustomThemePalette(
            id = "forest_pine",
            name = "کاج جنگلی (Forest Pine)",
            primary = Color(0xFF1B4332),
            secondary = Color(0xFF2D6A4F),
            tertiary = Color(0xFF40916C),
            background = Color(0xFFE8F5E9),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFC8E6C9),
            outgoingBubble = Color(0xFF1B4332),
            incomingText = Color(0xFF081C15),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 24. Amber Sunset
        CustomThemePalette(
            id = "amber_sunset",
            name = "کهربایی آفتاب (Amber Sunset)",
            primary = Color(0xFFFF8F00),
            secondary = Color(0xFFFFA000),
            tertiary = Color(0xFFFFB300),
            background = Color(0xFFFFF8E1),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFECB3),
            outgoingBubble = Color(0xFFFF8F00),
            incomingText = Color(0xFFFF6F00),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 25. Midnight Blue
        CustomThemePalette(
            id = "midnight_blue",
            name = "نیلی نیمه‌شب (Midnight Blue)",
            primary = Color(0xFF38BDF8),
            secondary = Color(0xFF818CF8),
            tertiary = Color(0xFFC084FC),
            background = Color(0xFF020617),
            surface = Color(0xFF0F172A),
            incomingBubble = Color(0xFF1E293B),
            outgoingBubble = Color(0xFF0284C7),
            incomingText = Color(0xFFF1F5F9),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 26. Iris Purple
        CustomThemePalette(
            id = "iris_purple",
            name = "زنبقی (Iris Purple)",
            primary = Color(0xFF5D3FD3),
            secondary = Color(0xFF7B68EE),
            tertiary = Color(0xFF9370DB),
            background = Color(0xFFF4F0FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE6E0FF),
            outgoingBubble = Color(0xFF5D3FD3),
            incomingText = Color(0xFF2A1B69),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 27. Emerald Dark
        CustomThemePalette(
            id = "emerald_dark",
            name = "زمرد تیره (Emerald Dark)",
            primary = Color(0xFF34D399),
            secondary = Color(0xFF6EE7B7),
            tertiary = Color(0xFFA7F3D0),
            background = Color(0xFF064E3B),
            surface = Color(0xFF022C22),
            incomingBubble = Color(0xFF065F46),
            outgoingBubble = Color(0xFF059669),
            incomingText = Color(0xFFECFDF5),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 28. Monochrome Minimal
        CustomThemePalette(
            id = "monochrome_minimal",
            name = "تک‌رنگ مینیمال (Monochrome)",
            primary = Color(0xFF18181B),
            secondary = Color(0xFF3F3F46),
            tertiary = Color(0xFF71717A),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFF4F4F5),
            outgoingBubble = Color(0xFF18181B),
            incomingText = Color(0xFF09090B),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 29. Pastel Dreams
        CustomThemePalette(
            id = "pastel_dreams",
            name = "پاستلی مینیاتور (Pastel Dreams)",
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFF472B6),
            tertiary = Color(0xFFFBCFE8),
            background = Color(0xFFFFF1F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFE4E6),
            outgoingBubble = Color(0xFFDB2777),
            incomingText = Color(0xFF831843),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 30. Golden Olive
        CustomThemePalette(
            id = "golden_olive",
            name = "زیتونی طلایی (Golden Olive)",
            primary = Color(0xFF556B2F),
            secondary = Color(0xFF6B8E23),
            tertiary = Color(0xFF8FBC8F),
            background = Color(0xFFF5F5DC),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE6E8FA),
            outgoingBubble = Color(0xFF556B2F),
            incomingText = Color(0xFF2E3B14),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 31. Corporate Navy
        CustomThemePalette(
            id = "corporate_navy",
            name = "سرمه‌ای شرکتی (Corporate Navy)",
            primary = Color(0xFF1E3A8A),
            secondary = Color(0xFF1D4ED8),
            tertiary = Color(0xFF3B82F6),
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFEFF6FF),
            outgoingBubble = Color(0xFF1E3A8A),
            incomingText = Color(0xFF1E293B),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 32. Persian Turquoise
        CustomThemePalette(
            id = "persian_turquoise",
            name = "فیروزه‌ای ایرانی (Persian Turquoise)",
            primary = Color(0xFF00A896),
            secondary = Color(0xFF028090),
            tertiary = Color(0xFF05668D),
            background = Color(0xFFF0FDF4),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE0F2FE),
            outgoingBubble = Color(0xFF00A896),
            incomingText = Color(0xFF023E8A),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 33. Neon Matrix
        CustomThemePalette(
            id = "neon_matrix",
            name = "ماتریس نئون (Neon Matrix)",
            primary = Color(0xFF22C55E),
            secondary = Color(0xFF16A34A),
            tertiary = Color(0xFF15803D),
            background = Color(0xFF050505),
            surface = Color(0xFF111111),
            incomingBubble = Color(0xFF1A1A1A),
            outgoingBubble = Color(0xFF16A34A),
            incomingText = Color(0xFF22C55E),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 34. Solar Flare
        CustomThemePalette(
            id = "solar_flare",
            name = "شعله خورشیدی (Solar Flare)",
            primary = Color(0xFFEA580C),
            secondary = Color(0xFFC2410C),
            tertiary = Color(0xFF9A3412),
            background = Color(0xFFFFF7ED),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFEDD5),
            outgoingBubble = Color(0xFFEA580C),
            incomingText = Color(0xFF431407),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 35. Cyberpunk Synth
        CustomThemePalette(
            id = "cyberpunk_synth",
            name = "سایبرپانک (Cyberpunk Synth)",
            primary = Color(0xFFEC4899),
            secondary = Color(0xFF8B5CF6),
            tertiary = Color(0xFF06B6D4),
            background = Color(0xFF0F0728),
            surface = Color(0xFF1D0E4C),
            incomingBubble = Color(0xFF2D1670),
            outgoingBubble = Color(0xFFD946EF),
            incomingText = Color(0xFFF472B6),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 36. Velvet Wine
        CustomThemePalette(
            id = "velvet_wine",
            name = "شرابی مخملی (Velvet Wine)",
            primary = Color(0xFF881337),
            secondary = Color(0xFF9F1239),
            tertiary = Color(0xFFBE123C),
            background = Color(0xFFFFF1F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFE4E6),
            outgoingBubble = Color(0xFF881337),
            incomingText = Color(0xFF4C0519),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 37. Deep Space Obsidian
        CustomThemePalette(
            id = "deep_space",
            name = "فضای عمیق (Deep Space)",
            primary = Color(0xFF6366F1),
            secondary = Color(0xFF4F46E5),
            tertiary = Color(0xFF4338CA),
            background = Color(0xFF030712),
            surface = Color(0xFF111827),
            incomingBubble = Color(0xFF1F2937),
            outgoingBubble = Color(0xFF4F46E5),
            incomingText = Color(0xFFF9FAFB),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 38. Emerald Royal
        CustomThemePalette(
            id = "emerald_royal",
            name = "زمرد سلطنتی (Emerald Royal)",
            primary = Color(0xFF047857),
            secondary = Color(0xFF065F46),
            tertiary = Color(0xFF064E3B),
            background = Color(0xFFECFDF5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xD1FAE5),
            outgoingBubble = Color(0xFF047857),
            incomingText = Color(0xFF022C22),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 39. Vintage Sepia
        CustomThemePalette(
            id = "vintage_sepia",
            name = "سپیا نوستالژیک (Vintage Sepia)",
            primary = Color(0xFF78350F),
            secondary = Color(0xFF92400E),
            tertiary = Color(0xFFB45309),
            background = Color(0xFFFEF3C7),
            surface = Color(0xFFFFFBEB),
            incomingBubble = Color(0xFFFDE68A),
            outgoingBubble = Color(0xFF78350F),
            incomingText = Color(0xFF451A03),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 40. Metallic Silver
        CustomThemePalette(
            id = "metallic_silver",
            name = "نقره‌ای متالیک (Metallic Silver)",
            primary = Color(0xFF475569),
            secondary = Color(0xFF334155),
            tertiary = Color(0xFF1E293B),
            background = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE2E8F0),
            outgoingBubble = Color(0xFF475569),
            incomingText = Color(0xFF0F172A),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 41. Warm Terracotta
        CustomThemePalette(
            id = "warm_terracotta",
            name = "تراکوتا گرم (Warm Terracotta)",
            primary = Color(0xFF9A3412),
            secondary = Color(0xFFC2410C),
            tertiary = Color(0xFFEA580C),
            background = Color(0xFFFFF7ED),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFEDD5),
            outgoingBubble = Color(0xFF9A3412),
            incomingText = Color(0xFF431407),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 42. Cobalt Enterprise
        CustomThemePalette(
            id = "cobalt_enterprise",
            name = "کبالت سازمانی (Cobalt Enterprise)",
            primary = Color(0xFF1D4ED8),
            secondary = Color(0xFF1E40AF),
            tertiary = Color(0xFF1E3A8A),
            background = Color(0xFFEFF6FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDBEAFE),
            outgoingBubble = Color(0xFF1D4ED8),
            incomingText = Color(0xFF172554),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 43. Pearl White Light
        CustomThemePalette(
            id = "pearl_white",
            name = "مرواریدی روشن (Pearl White)",
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF3B82F6),
            tertiary = Color(0xFF60A5FA),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF8FAFC),
            incomingBubble = Color(0xFFF1F5F9),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFF0F172A),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 44. Onyx Stealth Dark
        CustomThemePalette(
            id = "onyx_stealth",
            name = "عقیق مشکی (Onyx Stealth)",
            primary = Color(0xFFE2E8F0),
            secondary = Color(0xFFCBD5E1),
            tertiary = Color(0xFF94A3B8),
            background = Color(0xFF09090B),
            surface = Color(0xFF18181B),
            incomingBubble = Color(0xFF27272A),
            outgoingBubble = Color(0xFF3F3F46),
            incomingText = Color(0xFFFAFAFA),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 45. Plum Elegance
        CustomThemePalette(
            id = "plum_elegance",
            name = "شاه‌بلوطی و آلو (Plum Elegance)",
            primary = Color(0xFF581C87),
            secondary = Color(0xFF6B21A8),
            tertiary = Color(0xFF7E22CE),
            background = Color(0xFFFAF5FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFF3E8FF),
            outgoingBubble = Color(0xFF581C87),
            incomingText = Color(0xFF3B0764),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 46. Electric Violet
        CustomThemePalette(
            id = "electric_violet",
            name = "بنفش الکتریک (Electric Violet)",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF8B5CF6),
            tertiary = Color(0xFFA78BFA),
            background = Color(0xFFF5F3FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFEDE9FE),
            outgoingBubble = Color(0xFF7C3AED),
            incomingText = Color(0xFF4C1D95),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 47. Forest Camo
        CustomThemePalette(
            id = "forest_camo",
            name = "زیتونی جنگلی (Forest Camo)",
            primary = Color(0xFF3F6212),
            secondary = Color(0xFF4C1D95),
            tertiary = Color(0xFF65A30D),
            background = Color(0xFFF7FEE7),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFECFCCB),
            outgoingBubble = Color(0xFF3F6212),
            incomingText = Color(0xFF1A2E05),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 48. Sunset Blossom
        CustomThemePalette(
            id = "sunset_blossom",
            name = "شکوفه غروب (Sunset Blossom)",
            primary = Color(0xFFBE185D),
            secondary = Color(0xFFDB2777),
            tertiary = Color(0xFFE11D48),
            background = Color(0xFFFFF1F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFFE4E6),
            outgoingBubble = Color(0xFFBE185D),
            incomingText = Color(0xFF881337),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 49. Arctic Ice
        CustomThemePalette(
            id = "arctic_ice",
            name = "یخ قطبی (Arctic Ice)",
            primary = Color(0xFF0284C7),
            secondary = Color(0xFF0369A1),
            tertiary = Color(0xFF075985),
            background = Color(0xFFF0F9FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFE0F2FE),
            outgoingBubble = Color(0xFF0284C7),
            incomingText = Color(0xFF0C4A6E),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 50. Midnight Forest Dark
        CustomThemePalette(
            id = "midnight_forest",
            name = "جنگل نیمه‌شب (Midnight Forest)",
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            tertiary = Color(0xFF047857),
            background = Color(0xFF022C22),
            surface = Color(0xFF064E3B),
            incomingBubble = Color(0xFF065F46),
            outgoingBubble = Color(0xFF10B981),
            incomingText = Color(0xFFECFDF5),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true
        ),
        // 51. Copper Bronze
        CustomThemePalette(
            id = "copper_bronze",
            name = "مسی برنزی (Copper Bronze)",
            primary = Color(0xFF92400E),
            secondary = Color(0xFFB45309),
            tertiary = Color(0xFFD97706),
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEF3C7),
            outgoingBubble = Color(0xFF92400E),
            incomingText = Color(0xFF78350F),
            outgoingText = Color(0xFFFFFFFF)
        ),
        // 52. Obsidian Emerald
        CustomThemePalette(
            id = "obsidian_emerald",
            name = "عقیق زمردی (Obsidian Emerald)",
            primary = Color(0xFF34D399),
            secondary = Color(0xFF10B981),
            tertiary = Color(0xFF059669),
            background = Color(0xFF000000),
            surface = Color(0xFF111827),
            incomingBubble = Color(0xFF1F2937),
            outgoingBubble = Color(0xFF059669),
            incomingText = Color(0xFFF9FAFB),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 53. فیروزه‌ای اصفهان
        CustomThemePalette(
            id = "persian_trad_1",
            name = "فیروزه‌ای اصفهان (سنتی ایرانی)",
            primary = Color(0xFF00A896),
            secondary = Color(0xFF028090),
            tertiary = Color(0xFF05668D),
            background = Color(0xFFF0FDF4),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDCFCE7),
            outgoingBubble = Color(0xFF00A896),
            incomingText = Color(0xFF14532D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 54. کاشی شیراز
        CustomThemePalette(
            id = "persian_trad_2",
            name = "کاشی شیراز (سنتی ایرانی)",
            primary = Color(0xFFD97706),
            secondary = Color(0xFFB45309),
            tertiary = Color(0xFF78350F),
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEF3C7),
            outgoingBubble = Color(0xFFD97706),
            incomingText = Color(0xFF451A03),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 55. عقیق یزد
        CustomThemePalette(
            id = "persian_trad_3",
            name = "عقیق یزد (سنتی ایرانی)",
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF1D4ED8),
            tertiary = Color(0xFF1E40AF),
            background = Color(0xFFEFF6FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDBEAFE),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFF1E3A8A),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 56. قالی تبریز
        CustomThemePalette(
            id = "persian_trad_4",
            name = "قالی تبریز (سنتی ایرانی)",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF6D28D9),
            tertiary = Color(0xFF5B21B6),
            background = Color(0xFFF5F3FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFEDE9FE),
            outgoingBubble = Color(0xFF7C3AED),
            incomingText = Color(0xFF4C1D95),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 57. ترمه کرمان
        CustomThemePalette(
            id = "persian_trad_5",
            name = "ترمه کرمان (سنتی ایرانی)",
            primary = Color(0xFF059669),
            secondary = Color(0xFF047857),
            tertiary = Color(0xFF065F46),
            background = Color(0xFFECFDF5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFD1FAE5),
            outgoingBubble = Color(0xFF059669),
            incomingText = Color(0xFF064E3B),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 58. زعفرانی خراسان
        CustomThemePalette(
            id = "persian_trad_6",
            name = "زعفرانی خراسان (سنتی ایرانی)",
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFB91C1C),
            tertiary = Color(0xFF991B1B),
            background = Color(0xFFFEF2F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEE2E2),
            outgoingBubble = Color(0xFFDC2626),
            incomingText = Color(0xFF7F1D1D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 59. بانک ملی خلیج
        CustomThemePalette(
            id = "banking_pro_1",
            name = "بانک ملی خلیج (بانکی حرفه‌ای)",
            primary = Color(0xFF00F5D4),
            secondary = Color(0xFF00BBF9),
            tertiary = Color(0xFF7B2CBF),
            background = Color(0xFF0A0A12),
            surface = Color(0xFF121220),
            incomingBubble = Color(0xFF1A1A30),
            outgoingBubble = Color(0xFF00F5D4),
            incomingText = Color(0xFFE0F2FE),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = false
        ),
        // 60. ملت طلایی
        CustomThemePalette(
            id = "banking_pro_2",
            name = "ملت طلایی (بانکی حرفه‌ای)",
            primary = Color(0xFFF72585),
            secondary = Color(0xFF7209B7),
            tertiary = Color(0xFF3F37C9),
            background = Color(0xFF0F051D),
            surface = Color(0xFF1A0A2A),
            incomingBubble = Color(0xFF2A1040),
            outgoingBubble = Color(0xFFF72585),
            incomingText = Color(0xFFFDE8F4),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = false
        ),
        // 61. سامان آبی
        CustomThemePalette(
            id = "banking_pro_3",
            name = "سامان آبی (بانکی حرفه‌ای)",
            primary = Color(0xFF3A86FF),
            secondary = Color(0xFF8338EC),
            tertiary = Color(0xFFFF006E),
            background = Color(0xFF000000),
            surface = Color(0xFF111111),
            incomingBubble = Color(0xFF222222),
            outgoingBubble = Color(0xFF3A86FF),
            incomingText = Color(0xFFFFFFFF),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 62. پاسارگاد سرمه‌ای
        CustomThemePalette(
            id = "banking_pro_4",
            name = "پاسارگاد سرمه‌ای (بانکی حرفه‌ای)",
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            tertiary = Color(0xFF047857),
            background = Color(0xFF000000),
            surface = Color(0xFF0A140F),
            incomingBubble = Color(0xFF14281E),
            outgoingBubble = Color(0xFF10B981),
            incomingText = Color(0xFFE6FFFA),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 63. تجارت فیروزه‌ای
        CustomThemePalette(
            id = "banking_pro_5",
            name = "تجارت فیروزه‌ای (بانکی حرفه‌ای)",
            primary = Color(0xFFEAB308),
            secondary = Color(0xFFCA8A04),
            tertiary = Color(0xFFA16207),
            background = Color(0xFF000000),
            surface = Color(0xFF14120A),
            incomingBubble = Color(0xFF282414),
            outgoingBubble = Color(0xFFEAB308),
            incomingText = Color(0xFFFEFCE8),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = true
        ),
        // 64. پارسیان ارغوانی
        CustomThemePalette(
            id = "banking_pro_6",
            name = "پارسیان ارغوانی (بانکی حرفه‌ای)",
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFDB2777),
            tertiary = Color(0xFFBE185D),
            background = Color(0xFF000000),
            surface = Color(0xFF140A10),
            incomingBubble = Color(0xFF281420),
            outgoingBubble = Color(0xFFEC4899),
            incomingText = Color(0xFFFDF2F8),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 65. ماتریس سبز
        CustomThemePalette(
            id = "cyber_neon_1",
            name = "ماتریس سبز (سایبر نئون)",
            primary = Color(0xFF00A896),
            secondary = Color(0xFF028090),
            tertiary = Color(0xFF05668D),
            background = Color(0xFFF0FDF4),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDCFCE7),
            outgoingBubble = Color(0xFF00A896),
            incomingText = Color(0xFF14532D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 66. نئون بنفش
        CustomThemePalette(
            id = "cyber_neon_2",
            name = "نئون بنفش (سایبر نئون)",
            primary = Color(0xFFD97706),
            secondary = Color(0xFFB45309),
            tertiary = Color(0xFF78350F),
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEF3C7),
            outgoingBubble = Color(0xFFD97706),
            incomingText = Color(0xFF451A03),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 67. سایبرپانک قرمز
        CustomThemePalette(
            id = "cyber_neon_3",
            name = "سایبرپانک قرمز (سایبر نئون)",
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF1D4ED8),
            tertiary = Color(0xFF1E40AF),
            background = Color(0xFFEFF6FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDBEAFE),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFF1E3A8A),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 68. تک شوک آبی
        CustomThemePalette(
            id = "cyber_neon_4",
            name = "تک شوک آبی (سایبر نئون)",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF6D28D9),
            tertiary = Color(0xFF5B21B6),
            background = Color(0xFFF5F3FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFEDE9FE),
            outgoingBubble = Color(0xFF7C3AED),
            incomingText = Color(0xFF4C1D95),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 69. سینث‌ویو زرد
        CustomThemePalette(
            id = "cyber_neon_5",
            name = "سینث‌ویو زرد (سایبر نئون)",
            primary = Color(0xFF059669),
            secondary = Color(0xFF047857),
            tertiary = Color(0xFF065F46),
            background = Color(0xFFECFDF5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFD1FAE5),
            outgoingBubble = Color(0xFF059669),
            incomingText = Color(0xFF064E3B),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 70. هایپردایو فیروزه‌ای
        CustomThemePalette(
            id = "cyber_neon_6",
            name = "هایپردایو فیروزه‌ای (سایبر نئون)",
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFB91C1C),
            tertiary = Color(0xFF991B1B),
            background = Color(0xFFFEF2F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEE2E2),
            outgoingBubble = Color(0xFFDC2626),
            incomingText = Color(0xFF7F1D1D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 71. آمولد الماس
        CustomThemePalette(
            id = "amoled_pro_1",
            name = "آمولد الماس (آمولد حرفه‌ای)",
            primary = Color(0xFF00F5D4),
            secondary = Color(0xFF00BBF9),
            tertiary = Color(0xFF7B2CBF),
            background = Color(0xFF0A0A12),
            surface = Color(0xFF121220),
            incomingBubble = Color(0xFF1A1A30),
            outgoingBubble = Color(0xFF00F5D4),
            incomingText = Color(0xFFE0F2FE),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = false
        ),
        // 72. آمولد طلا
        CustomThemePalette(
            id = "amoled_pro_2",
            name = "آمولد طلا (آمولد حرفه‌ای)",
            primary = Color(0xFFF72585),
            secondary = Color(0xFF7209B7),
            tertiary = Color(0xFF3F37C9),
            background = Color(0xFF0F051D),
            surface = Color(0xFF1A0A2A),
            incomingBubble = Color(0xFF2A1040),
            outgoingBubble = Color(0xFFF72585),
            incomingText = Color(0xFFFDE8F4),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = false
        ),
        // 73. آمولد نئون
        CustomThemePalette(
            id = "amoled_pro_3",
            name = "آمولد نئون (آمولد حرفه‌ای)",
            primary = Color(0xFF3A86FF),
            secondary = Color(0xFF8338EC),
            tertiary = Color(0xFFFF006E),
            background = Color(0xFF000000),
            surface = Color(0xFF111111),
            incomingBubble = Color(0xFF222222),
            outgoingBubble = Color(0xFF3A86FF),
            incomingText = Color(0xFFFFFFFF),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 74. آمولد زمرد
        CustomThemePalette(
            id = "amoled_pro_4",
            name = "آمولد زمرد (آمولد حرفه‌ای)",
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            tertiary = Color(0xFF047857),
            background = Color(0xFF000000),
            surface = Color(0xFF0A140F),
            incomingBubble = Color(0xFF14281E),
            outgoingBubble = Color(0xFF10B981),
            incomingText = Color(0xFFE6FFFA),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 75. آمولد یاقوت
        CustomThemePalette(
            id = "amoled_pro_5",
            name = "آمولد یاقوت (آمولد حرفه‌ای)",
            primary = Color(0xFFEAB308),
            secondary = Color(0xFFCA8A04),
            tertiary = Color(0xFFA16207),
            background = Color(0xFF000000),
            surface = Color(0xFF14120A),
            incomingBubble = Color(0xFF282414),
            outgoingBubble = Color(0xFFEAB308),
            incomingText = Color(0xFFFEFCE8),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = true
        ),
        // 76. آمولد نقره
        CustomThemePalette(
            id = "amoled_pro_6",
            name = "آمولد نقره (آمولد حرفه‌ای)",
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFDB2777),
            tertiary = Color(0xFFBE185D),
            background = Color(0xFF000000),
            surface = Color(0xFF140A10),
            incomingBubble = Color(0xFF281420),
            outgoingBubble = Color(0xFFEC4899),
            incomingText = Color(0xFFFDF2F8),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 77. تجارتی خاکستری
        CustomThemePalette(
            id = "business_pro_1",
            name = "تجارتی خاکستری (کسب‌وکار و تجارت)",
            primary = Color(0xFF00A896),
            secondary = Color(0xFF028090),
            tertiary = Color(0xFF05668D),
            background = Color(0xFFF0FDF4),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDCFCE7),
            outgoingBubble = Color(0xFF00A896),
            incomingText = Color(0xFF14532D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 78. مدیریتی قهوه‌ای
        CustomThemePalette(
            id = "business_pro_2",
            name = "مدیریتی قهوه‌ای (کسب‌وکار و تجارت)",
            primary = Color(0xFFD97706),
            secondary = Color(0xFFB45309),
            tertiary = Color(0xFF78350F),
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEF3C7),
            outgoingBubble = Color(0xFFD97706),
            incomingText = Color(0xFF451A03),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 79. شرکتی سرمه‌ای
        CustomThemePalette(
            id = "business_pro_3",
            name = "شرکتی سرمه‌ای (کسب‌وکار و تجارت)",
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF1D4ED8),
            tertiary = Color(0xFF1E40AF),
            background = Color(0xFFEFF6FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDBEAFE),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFF1E3A8A),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 80. استارتاپ نارنجی
        CustomThemePalette(
            id = "business_pro_4",
            name = "استارتاپ نارنجی (کسب‌وکار و تجارت)",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF6D28D9),
            tertiary = Color(0xFF5B21B6),
            background = Color(0xFFF5F3FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFEDE9FE),
            outgoingBubble = Color(0xFF7C3AED),
            incomingText = Color(0xFF4C1D95),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 81. مشاوره‌ای سبز
        CustomThemePalette(
            id = "business_pro_5",
            name = "مشاوره‌ای سبز (کسب‌وکار و تجارت)",
            primary = Color(0xFF059669),
            secondary = Color(0xFF047857),
            tertiary = Color(0xFF065F46),
            background = Color(0xFFECFDF5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFD1FAE5),
            outgoingBubble = Color(0xFF059669),
            incomingText = Color(0xFF064E3B),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 82. سرمایه‌گذاری بنفش
        CustomThemePalette(
            id = "business_pro_6",
            name = "سرمایه‌گذاری بنفش (کسب‌وکار و تجارت)",
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFB91C1C),
            tertiary = Color(0xFF991B1B),
            background = Color(0xFFFEF2F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEE2E2),
            outgoingBubble = Color(0xFFDC2626),
            incomingText = Color(0xFF7F1D1D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 83. شب تیره
        CustomThemePalette(
            id = "dark_luxe_1",
            name = "شب تیره (تاریک لوکس)",
            primary = Color(0xFF00F5D4),
            secondary = Color(0xFF00BBF9),
            tertiary = Color(0xFF7B2CBF),
            background = Color(0xFF0A0A12),
            surface = Color(0xFF121220),
            incomingBubble = Color(0xFF1A1A30),
            outgoingBubble = Color(0xFF00F5D4),
            incomingText = Color(0xFFE0F2FE),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = false
        ),
        // 84. مشکی مخملی
        CustomThemePalette(
            id = "dark_luxe_2",
            name = "مشکی مخملی (تاریک لوکس)",
            primary = Color(0xFFF72585),
            secondary = Color(0xFF7209B7),
            tertiary = Color(0xFF3F37C9),
            background = Color(0xFF0F051D),
            surface = Color(0xFF1A0A2A),
            incomingBubble = Color(0xFF2A1040),
            outgoingBubble = Color(0xFFF72585),
            incomingText = Color(0xFFFDE8F4),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = false
        ),
        // 85. سایه خاکستری
        CustomThemePalette(
            id = "dark_luxe_3",
            name = "سایه خاکستری (تاریک لوکس)",
            primary = Color(0xFF3A86FF),
            secondary = Color(0xFF8338EC),
            tertiary = Color(0xFFFF006E),
            background = Color(0xFF000000),
            surface = Color(0xFF111111),
            incomingBubble = Color(0xFF222222),
            outgoingBubble = Color(0xFF3A86FF),
            incomingText = Color(0xFFFFFFFF),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 86. بنفش تاریک
        CustomThemePalette(
            id = "dark_luxe_4",
            name = "بنفش تاریک (تاریک لوکس)",
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            tertiary = Color(0xFF047857),
            background = Color(0xFF000000),
            surface = Color(0xFF0A140F),
            incomingBubble = Color(0xFF14281E),
            outgoingBubble = Color(0xFF10B981),
            incomingText = Color(0xFFE6FFFA),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 87. آبی عمیق
        CustomThemePalette(
            id = "dark_luxe_5",
            name = "آبی عمیق (تاریک لوکس)",
            primary = Color(0xFFEAB308),
            secondary = Color(0xFFCA8A04),
            tertiary = Color(0xFFA16207),
            background = Color(0xFF000000),
            surface = Color(0xFF14120A),
            incomingBubble = Color(0xFF282414),
            outgoingBubble = Color(0xFFEAB308),
            incomingText = Color(0xFFFEFCE8),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = true
        ),
        // 88. سبز زیتونی تاریک
        CustomThemePalette(
            id = "dark_luxe_6",
            name = "سبز زیتونی تاریک (تاریک لوکس)",
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFDB2777),
            tertiary = Color(0xFFBE185D),
            background = Color(0xFF000000),
            surface = Color(0xFF140A10),
            incomingBubble = Color(0xFF281420),
            outgoingBubble = Color(0xFFEC4899),
            incomingText = Color(0xFFFDF2F8),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 89. سفید برفی
        CustomThemePalette(
            id = "light_clean_1",
            name = "سفید برفی (روشن و مینی‌مال)",
            primary = Color(0xFF00A896),
            secondary = Color(0xFF028090),
            tertiary = Color(0xFF05668D),
            background = Color(0xFFF0FDF4),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDCFCE7),
            outgoingBubble = Color(0xFF00A896),
            incomingText = Color(0xFF14532D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 90. کرمی گرم
        CustomThemePalette(
            id = "light_clean_2",
            name = "کرمی گرم (روشن و مینی‌مال)",
            primary = Color(0xFFD97706),
            secondary = Color(0xFFB45309),
            tertiary = Color(0xFF78350F),
            background = Color(0xFFFFFBEB),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEF3C7),
            outgoingBubble = Color(0xFFD97706),
            incomingText = Color(0xFF451A03),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 91. یاسی روشن
        CustomThemePalette(
            id = "light_clean_3",
            name = "یاسی روشن (روشن و مینی‌مال)",
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF1D4ED8),
            tertiary = Color(0xFF1E40AF),
            background = Color(0xFFEFF6FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFDBEAFE),
            outgoingBubble = Color(0xFF2563EB),
            incomingText = Color(0xFF1E3A8A),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 92. پسته‌ای روشن
        CustomThemePalette(
            id = "light_clean_4",
            name = "پسته‌ای روشن (روشن و مینی‌مال)",
            primary = Color(0xFF7C3AED),
            secondary = Color(0xFF6D28D9),
            tertiary = Color(0xFF5B21B6),
            background = Color(0xFFF5F3FF),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFEDE9FE),
            outgoingBubble = Color(0xFF7C3AED),
            incomingText = Color(0xFF4C1D95),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 93. آبی آسمانی
        CustomThemePalette(
            id = "light_clean_5",
            name = "آبی آسمانی (روشن و مینی‌مال)",
            primary = Color(0xFF059669),
            secondary = Color(0xFF047857),
            tertiary = Color(0xFF065F46),
            background = Color(0xFFECFDF5),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFD1FAE5),
            outgoingBubble = Color(0xFF059669),
            incomingText = Color(0xFF064E3B),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 94. هلویی نرم
        CustomThemePalette(
            id = "light_clean_6",
            name = "هلویی نرم (روشن و مینی‌مال)",
            primary = Color(0xFFDC2626),
            secondary = Color(0xFFB91C1C),
            tertiary = Color(0xFF991B1B),
            background = Color(0xFFFEF2F2),
            surface = Color(0xFFFFFFFF),
            incomingBubble = Color(0xFFFEE2E2),
            outgoingBubble = Color(0xFFDC2626),
            incomingText = Color(0xFF7F1D1D),
            outgoingText = Color(0xFFFFFFFF),
            isDark = false,
            isAmoled = false
        ),
        // 95. متریال زرد
        CustomThemePalette(
            id = "pro_material_1",
            name = "متریال زرد (متریال پرو)",
            primary = Color(0xFF00F5D4),
            secondary = Color(0xFF00BBF9),
            tertiary = Color(0xFF7B2CBF),
            background = Color(0xFF0A0A12),
            surface = Color(0xFF121220),
            incomingBubble = Color(0xFF1A1A30),
            outgoingBubble = Color(0xFF00F5D4),
            incomingText = Color(0xFFE0F2FE),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = false
        ),
        // 96. متریال نارنجی
        CustomThemePalette(
            id = "pro_material_2",
            name = "متریال نارنجی (متریال پرو)",
            primary = Color(0xFFF72585),
            secondary = Color(0xFF7209B7),
            tertiary = Color(0xFF3F37C9),
            background = Color(0xFF0F051D),
            surface = Color(0xFF1A0A2A),
            incomingBubble = Color(0xFF2A1040),
            outgoingBubble = Color(0xFFF72585),
            incomingText = Color(0xFFFDE8F4),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = false
        ),
        // 97. متریال قرمز
        CustomThemePalette(
            id = "pro_material_3",
            name = "متریال قرمز (متریال پرو)",
            primary = Color(0xFF3A86FF),
            secondary = Color(0xFF8338EC),
            tertiary = Color(0xFFFF006E),
            background = Color(0xFF000000),
            surface = Color(0xFF111111),
            incomingBubble = Color(0xFF222222),
            outgoingBubble = Color(0xFF3A86FF),
            incomingText = Color(0xFFFFFFFF),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 98. متریال فیروزه‌ای
        CustomThemePalette(
            id = "pro_material_4",
            name = "متریال فیروزه‌ای (متریال پرو)",
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            tertiary = Color(0xFF047857),
            background = Color(0xFF000000),
            surface = Color(0xFF0A140F),
            incomingBubble = Color(0xFF14281E),
            outgoingBubble = Color(0xFF10B981),
            incomingText = Color(0xFFE6FFFA),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        ),
        // 99. متریال آبی
        CustomThemePalette(
            id = "pro_material_5",
            name = "متریال آبی (متریال پرو)",
            primary = Color(0xFFEAB308),
            secondary = Color(0xFFCA8A04),
            tertiary = Color(0xFFA16207),
            background = Color(0xFF000000),
            surface = Color(0xFF14120A),
            incomingBubble = Color(0xFF282414),
            outgoingBubble = Color(0xFFEAB308),
            incomingText = Color(0xFFFEFCE8),
            outgoingText = Color(0xFF000000),
            isDark = true,
            isAmoled = true
        ),
        // 100. متریال نیلی
        CustomThemePalette(
            id = "pro_material_6",
            name = "متریال نیلی (متریال پرو)",
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFDB2777),
            tertiary = Color(0xFFBE185D),
            background = Color(0xFF000000),
            surface = Color(0xFF140A10),
            incomingBubble = Color(0xFF281420),
            outgoingBubble = Color(0xFFEC4899),
            incomingText = Color(0xFFFDF2F8),
            outgoingText = Color(0xFFFFFFFF),
            isDark = true,
            isAmoled = true
        )
    )

    fun getById(id: String): CustomThemePalette {
        return palettes.find { it.id == id } ?: palettes.first()
    }
}
