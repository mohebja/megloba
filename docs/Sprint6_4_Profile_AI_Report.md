# Sprint 6.4 — Personal Communication Profile AI Report

## 1. Engine Overview
`CommunicationProfileEngine.kt` analyzes message history per contact to determine communication preferences, style, response latency, and priority scoring.

## 2. Profile Attributes
- **Communication Style**: `FORMAL`, `CASUAL`, `BRIEF`, `URGENT`.
- **Priority Score**: Scale of 0 to 100 based on message frequency, keywords, and urgency markers.
- **Average Response Latency**: Computed average response time in minutes.
- **Work Hours Only**: Auto-detected flag for business vs. personal contacts.

## 3. Privacy Guarantee
Profiles are stored locally in the encrypted `communication_profiles` table in Room Database v23. No profile data ever leaves the local device sandbox.
