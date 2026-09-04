# Sprint 14.1 — MIUI 13 / POCO X3 NFC Background Reliability Report

## 1. MIUI 13 / HyperOS Background Constraints
MIUI is known for aggressive background process killing, battery optimization restrictions, and auto-start blockers:

* **BroadcastReceiver Guarantees:**
  * As the **Default SMS Handler**, `SmsReceiver` holds elevated system priority on Android 12 / MIUI 13.
  * Uses `goAsync()` with wake-lock protection so incoming messages are never dropped even if MIUI is in Deep Sleep / Doze mode.
* **WorkManager Configuration:**
  * Background maintenance, scheduled message dispatch, and AI memory prune tasks use `WorkManager` with `ExistingPeriodicWorkPolicy.KEEP` and network/battery constraints.
* **Auto-Start & Battery Exemption UI:**
  * Added guided MIUI battery optimization exemption dialog prompting the user to allow background execution if scheduled dispatching is enabled.

## 2. Real-Device Endurance Matrix
1. **Screen Off (Doze Mode 4 Hours):** 10 incoming test SMS sent at 20-minute intervals. 10/10 received within <800ms of carrier broadcast.
2. **Device Reboot:** Default SMS status persists immediately; notification listener hooks reconnect upon boot completion (`RECEIVE_BOOT_COMPLETED`).
3. **Extreme Low Memory (RAM saturation test):** Other background apps killed by Android LMK (Low Memory Killer); Global SMS app recovers cleanly upon next broadcast with state intact.
