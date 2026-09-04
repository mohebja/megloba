# Sprint 17 — Post-Release Production Monitoring Plan

## 1. Privacy-Preserving Production Diagnostics
In strict compliance with our Zero-Cloud, Zero-Tracking privacy guarantee, monitoring is conducted on-device without telemetry spying:
* **Local Error Ring Buffer:** Stores up to 100 sanitized diagnostic event entries (capped at 5 MB max disk usage).
* **Exportable User Diagnostics:** Users can voluntarily generate and share a redacted diagnostics ZIP (`Settings -> Diagnostics -> Export Support Bundle`) when requesting customer support.
* **Redaction Policy:** All phone numbers, message bodies, OTP codes, banking amounts, and contact names are scrubbed before writing to the diagnostic buffer.
* **Failure Counters:** Tracks internal metrics locally (e.g., SMS dispatch retry counts, MMS APN timeout events, Room database query latency).
