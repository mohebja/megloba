# 9. Known Issues & Operational Considerations — Global SMS

---

## 1. Non-Blocking Considerations
1. **Device-Specific Dual SIM Carrier Naming:** On select legacy custom ROMs (Android 7.0/8.0), carrier SIM names default to "SIM 1" and "SIM 2" if subscription display names are restricted by OS carrier privileges.
2. **System MMS APN Settings:** MMS attachments require active cellular APN data enabled on device SIM.

---

## 2. Mitigations
- Both considerations are fully managed by graceful fallbacks and user Toast notifications in the UI.
