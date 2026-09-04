# Global SMS — Brand Identity & Visual System Specification

**Package Name:** `com.global.sms`  
**Application Name:** Global SMS  
**Design Standard:** Material Design 3 (M3) Enterprise Edition  
**Brand Concept:** Secure, Intelligent, Professional Messaging Platform with AI & Banking Intelligence  

---

## 1. Executive Visual Analysis

The official visual identity of **Global SMS** represents a convergence of three essential technology pillars:

1. **SMS & MMS Messaging:** Crisp, modern messaging bubbles and dual SIM connectivity indicators.
2. **Advanced Security & Private Vault:** Robust metallic security shield symbolizing end-to-end local encryption, biometric privacy protection, and spam defense.
3. **Artificial Intelligence & Banking Intelligence:** Glowing purple neural circuit nodes for automated classification and vibrant emerald green accents representing secure bank transaction detection.

```
       ┌────────────────────────────────────────────────────────┐
       │                 GLOBAL SMS SHIELD LOGO                  │
       │                                                        │
       │      ┌──────────────────────────────────────────┐      │
       │      │  [Shield] - Encryption & Private Vault   │      │
       │      │  [Chat]   - SMS/MMS Messaging            │      │
       │      │  [Nodes]  - AI Automated Categorization  │      │
       │      │  [Check]  - Bank Transaction Security    │      │
       │      └──────────────────────────────────────────┘      │
       │                                                        │
       └────────────────────────────────────────────────────────┘
```

---

## 2. Master Color Palette

| Token Name | Hex Code | Purpose / Usage | Color Role |
|---|---|---|---|
| **Messaging Blue** | `#1A73E8` | Primary branding, outgoing SMS bubbles, primary CTA buttons | `md_theme_light_primary` |
| **Dark Messaging Blue** | `#0B57D0` | Dark mode primary accent, active tab indicators | `md_theme_dark_primary` |
| **Security Obsidian Dark** | `#0D1117` | Premium dark canvas background, launcher icon base | `md_theme_dark_background` |
| **Deep Surface Dark** | `#161F30` | Dark surface containers, dialog cards, drawer header | `md_theme_dark_surface` |
| **Banking Green** | `#00C853` | Bank OTP notifications, financial transaction highlights | `Bank_Tag_Color` |
| **AI Purple Accent** | `#A855F7` | AI Smart Reply suggestions, auto-categorization chips | `AI_Tag_Color` |
| **Metallic Silver** | `#E2E8F0` | Security shield outlines, high-contrast borders | `Border_Stroke_Color` |
| **Error Red** | `#D32F2F` | Spam alerts, blocked contact warnings | `md_theme_error` |

---

## 3. Typography Recommendations

- **Primary Latin Font:** *Plus Jakarta Sans* / *Roboto* (Clean geometric sans-serif for high readability in dense conversation logs).
- **Primary RTL / Persian Font:** *Vazirmatn* (High legibility Persian font optimized for mobile viewports).
- **Type Scale Rules:**
  - **Display Large:** 32sp Bold (Headers & About Page Title)
  - **Title Medium:** 16sp Medium (Conversation contact names, Settings section headers)
  - **Body Medium:** 14sp Regular (Message snippets, dialog body text)
  - **Label Small:** 11sp Bold (Category tags: BANK, OTP, PERSONAL, SPAM)

---

## 4. Icon Usage Rules & Guidelines

1. **Launcher Icon (`@mipmap/ic_launcher`):**
   - Adaptive icon consisting of a dark gradient background (`ic_launcher_background`) and a centered, safe-zone foreground (`ic_launcher_foreground`).
   - Must strictly maintain the 66dp inner safe circle inside the 108dp adaptive canvas.
2. **Notification Icons:**
   - Must be white monochrome vector drawables with transparent backgrounds according to Android Status Bar specifications.
   - `ic_sms_notification.xml`: Standard incoming message notification.
   - `ic_security_notification.xml`: Private Vault & Spam alert notifications.
   - `ic_ai_notification.xml`: AI summary & smart categorization notifications.
3. **In-App Logos & Header Banners:**
   - Use high-contrast brand icons in drawer headers, About application dialogs, settings cards, and backup/restore screens.
