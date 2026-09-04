# Global SMS — Contact System Report

## Contact Integration & Synchronization
- **System Contacts Integration:** Real-time query of Android `ContactsContract.CommonDataKinds.Phone` using ContentObserver.
- **Normalization Engine:** Normalizes phone numbers using E.164 standard formatting to prevent duplicate entries across local and international number variants.
- **Script Handling:** Full support for Persian/Arabic character searching, string normalization, and alphabetical sorting.
- **Contact Cache:** High-performance in-memory LRU cache backed by Room database (`ContactEntity`), providing instantaneous caller lookup during incoming SMS arrival.
