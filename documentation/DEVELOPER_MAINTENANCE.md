# Global SMS — Developer Maintenance Guide

## Maintainability & Architecture Summary
This document serves as the primary maintenance handbook for core developers and DevOps engineers.

### Build & Maintenance Tasks
- Core Module Updates: Maintain strict separation between `:database` and `:sms-engine`.
- Permission Handling: Always wrap Telephony and SIM SDK calls with runtime permission checks.
- WorkManager Tasks: Manage background SMS workers with `ExistingPeriodicWorkPolicy.KEEP` to ensure zero task duplication.
