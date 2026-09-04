# Sprint 5.2 Dynamic Typography & Pinch-to-Zoom Scaling Fix

## Issue Description
Real device testing on Poco X3 NFC revealed that when zooming in on message bubbles via pinch-to-zoom gestures, multi-line text lines collided and overlapped vertically.

## Root Cause Analysis
In `MessageThreadScreen.kt`, `Text` composable was supplied only `fontSize = (settings.messageTextSizeSp * fontScale).sp` without setting `lineHeight`. As font size increased beyond standard dimensions, Compose defaulted to fixed unscaled line spacing, causing multi-line text collision.

## Applied Fixes
1. **DynamicTypography Engine**:
   - Implemented `DynamicTypography.kt` in `ui/src/main/java/com/global/sms/ui/theme/`.
   - Computes dynamic proportional `lineHeight = (fontSize * 1.45f).sp` mapped directly to scale multipliers.
2. **Message Bubble Integration**:
   - Updated `MessageThreadScreen.kt` `MessageBubble` composable to compute dynamic `lineHeight` using `DynamicTypography`.
   - Result: Multi-line message text maintains vertical line spacing regardless of font scale factor (from 0.7x to 2.2x zoom).
