# Contact Picker Integration Audit Report

**Project:** Global SMS (`com.global.sms`)  
**Component:** Contact Management & Message Composition Subsystem  
**Status:** Audit Completed & Architectural Gap Identified  
**Date:** August 2026  

---

## 1. Executive Summary

A comprehensive flow trace was conducted on the SMS message composition subsystem in Global SMS. The analysis revealed that while the underlying contact repository (`ContactRepositoryImpl`) and database layer were fully implemented, the user interface layer lacked a direct connection between the message composition screen and a dedicated contact selection UI. Tapping the "New Message" action opened a thread composer without allowing the user to browse or select recipients from their Android contacts.

This document outlines the discovery audit, component mapping, and the architectural design required to bridge this gap.

---

## 2. Component Mapping

| Subsystem Component | Class / Location | Responsibility |
|---|---|---|
| **Compose Screen** | `MultiContactComposeScreen.kt` | User interface for multi-recipient SMS composition |
| **Contact Picker UI** | `ContactPickerScreen.kt` | Dedicated full-screen contact selection UI with Persian search |
| **Navigation Route** | `MainActivity.kt` (`"multi_compose"`, `"contact_picker"`) | Route mapping and backstack management |
| **Primary State Holder** | `ContactViewModel.kt` | Manages `SelectedRecipientsState`, search query, and permission state |
| **Global State Holder** | `GlobalSmsViewModel.kt` | Manages active thread selection, groups, and dual-SIM dispatch |
| **Recipient Input UI** | `FlowRow` Chip Container + Manual Input | Displays recipient chips with removal actions |
| **SMS Engine Dispatch** | `SmsSender.kt` / `GlobalSmsViewModel.sendGroupSms()` | Dispatches outgoing SMS messages via Android Telephony Manager |

---

## 3. Flow Analysis

### Current (Defective) Flow

```
User Taps "New Message" (FAB)
      ↓
Navigates to Empty Message Thread (`thread/$newThreadId`)
      ↓
[?] Missing Recipient Selector / Contact Picker Button
      ↓
User cannot select existing contacts from device address book
      ↓
SMS Sender has no valid recipient target
```

### Required (Production) Flow

```
User Taps "New Message" (FAB)
      ↓
Opens `MultiContactComposeScreen`
      ↓
User Clicks [ انتخاب مخاطب ] Button
      ↓
Navigates to `ContactPickerScreen` (`"contact_picker"`)
      ↓
Persian / English / Phone Search & Selection (Checkboxes / Multi-select)
      ↓
User Taps "تایید" (Confirm Selection)
      ↓
Updates `SelectedRecipientsState` (StateFlow in ViewModel)
      ↓
Renders Recipient Chips `[ Ali X ]` `[ Mohammad X ]` in Composer
      ↓
User Enters Message Body & Selects SIM Slot
      ↓
Dispatches via `SmsSender.sendSms()` / `sendGroupSms()`
```

---

## 4. Remediation Plan Executed

1. **`ContactModels.kt`**: Added immutable `SelectedRecipientsState` supporting selected `ContactInfo` and custom direct phone numbers.
2. **`ContactViewModel.kt`**: Integrated `selectedRecipientsState` `StateFlow` with methods (`toggleContactRecipient`, `addCustomNumberRecipient`, `removeContactRecipient`, `clearAllRecipients`).
3. **`ContactPickerScreen.kt`**: Created a dedicated, full-screen Material 3 contact picker supporting Persian search, contact avatars, group chips, and multi-selection.
4. **`MultiContactComposeScreen.kt`**: Redesigned recipient area with prominent `[ انتخاب مخاطب ]` button, recipient chips with remove buttons, manual phone number input, and dual-SIM group sending.
5. **`MainActivity.kt`**: Configured routes `"multi_compose"` and `"contact_picker"` and wired FAB/Compose triggers across Classic and Smart UI modes.
