# Sprint 5.2 Advanced Theme Engine & Color Palette System

## Overview
Global SMS incorporates a complete Material Design 3 theme customizer with 52 hand-crafted color palettes, dark/light mode toggling, AMOLED pitch black mode, and granular message bubble color tuning.

## Theme Engine Architecture
1. **Repository**: `ui/src/main/java/com/global/sms/ui/theme/ColorPaletteRepository.kt`
   - Defines 52 distinct palettes spanning Light, Dark, AMOLED Pitch Black, Cyberpunk, Velvet, Neon, Emerald, Cobalt, Corporate Navy, and Persian Turquoise themes.
2. **Persistence**:
   - Palette selection saved to Room Database `settings` table via `SettingsRepository.kt`.
   - Incoming bubble color, outgoing bubble color, text colors, and font styles stored persistently across app launches.
3. **Screen**:
   - `ui/src/main/java/com/global/sms/ui/screens/ThemeCustomizerScreen.kt`
   - Real-time interactive message bubble preview card reflecting palette selection immediately before applying.
