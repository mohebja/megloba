# Sprint 14.2 — Notification Privacy & Channels Audit

## 1. Notification Architecture
Global SMS defines dedicated Notification Channels:
* `channel_critical_sms`: High-priority channel for urgent SMS and verification codes with heads-up display.
* `channel_normal_sms`: Default messaging alerts with vibration and sound.
* `channel_silent_sync`: Low-priority channel for background operations and backup notifications.

## 2. Privacy Masking Modes
* **Normal Mode:** Displays sender name, contact avatar, and message body snippet.
* **Privacy / Masked Mode:** Masks sensitive content on lock screens and system notifications as `"پیام امن جدید"` without exposing the sender or body text.
* **Private Vault Notifications:** Never display sender or body; only generic notifications are generated.
