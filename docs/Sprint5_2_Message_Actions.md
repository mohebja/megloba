# Sprint 5.2 Message Long-Press Contextual Actions Documentation

## Overview
Long-pressing any message bubble in the conversation thread invokes the Material 3 `MessageActionBottomSheet`, providing immediate access to contextual tools.

## Action Capabilities
1. **Copy Text (`action_copy_text`)**: Copies message text (or multi-selected message text) to the system clipboard.
2. **Share Message (`action_share_message`)**: Invokes native Android share intent.
3. **Forward Message (`action_forward_message`)**: Populates the draft composer with the selected message text for re-sending.
4. **Bookmark Message (`action_add_bookmark`)**: Pins/bookmarks message in thread storage.
5. **Export File (`action_export_message`)**: Generates TXT / PDF / Backup export for record keeping.
6. **Add Contact (`action_add_contact`)**: Opens system/app contact creator for unknown senders.
7. **Hide Message (`action_hide_message`)**: Transfers selected message to the encrypted Security Vault.
8. **Archive Conversation (`action_archive_conversation`)**: Archives entire conversation thread.
9. **Block Sender (`action_block_sender`)**: Prompts confirmation and adds sender number to blacklist.
10. **Report Spam (`action_report_spam`)**: Submits sender and message body to anti-spam filter database.
11. **Delete Message / Thread (`action_delete_message`, `action_delete_conversation`)**: Prompts confirmation dialog and performs cascade deletion from Room DB.

## Architecture
- Composable: `ui/src/main/java/com/global/sms/ui/components/MessageActionBottomSheet.kt`
- State Manager: `MessageSelectionManager.kt` and `MessageActionViewModel.kt`
- Invocation: `MessageThreadScreen.kt` using `combinedClickable(onLongClick = ...)`
