# Sprint 9 — Advanced Message Analytics Report

**Project:** Global SMS (`com.global.sms`)  
**Components:** `MessageAnalyticsEngine.kt`, `AnalyticsDashboardScreen.kt`  

---

## 1. Local Analytics Capabilities
All analytics are computed 100% on-device directly from the Room database:

- **Volume Metrics:** Total incoming, outgoing, and spam-blocked message counts.
- **Communication Trends:** Peak message hours (e.g., 18:00 - 19:00).
- **Response Metrics:** Average user response latency in minutes.
- **Hourly Distribution Chart:** Interactive RTL Material 3 progress bars showing hourly messaging volume.
