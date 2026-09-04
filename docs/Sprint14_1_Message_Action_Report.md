# Sprint 14.1 — Message Interaction & Context Actions Validation

## 1. Interaction System Architecture
Long-pressing any message item activates the `MessageContextActionsBottomSheet` / `ActionModeTopBar` which receives the exact `messageId` and `threadId`.

## 2. Action Verification Matrix
| Context Action | Tested Target | Target ID Integrity | Functional Execution | Result |
|---|---|---|---|---|
| **Copy Text** | General SMS | Exact message text copied to `ClipboardManager` | System clipboard holds exact payload | PASS |
| **Copy OTP Code** | Banking / OTP SMS | Extracts numeric OTP directly without fluff text | OTP copied instantly; toast feedback shown | PASS |
| **Reply in Context** | Any SMS | Quotes selected message in Compose input bar | Quote bubble rendered above text field | PASS |
| **Forward Message** | Any SMS | Opens contact picker / composer with prefilled body | Correct text forwarded to new recipient | PASS |
| **Delete Message** | Single / Multi | Room DB delete + system provider delete (if default) | Message removed with undo snackbar | PASS |
| **Archive Thread** | Any Conversation | Marks `isArchived = true` in Room DB | Hidden from main inbox; accessible in Archive | PASS |
| **Move to Private Vault** | Sensitive SMS | AES-256-GCM encryption + moves to Vault table | Removed from normal queries and notifications | PASS |
| **Pin Message / Star** | Important SMS | Sets `isPinned = true` / `isStarred = true` | Anchors to top / highlighted in Starred tab | PASS |
| **Add Note / Tag** | Business SMS | Attaches custom metadata tag | Tag rendered on message bubble | PASS |
| **Export / Share** | Any SMS | Standard Android `Intent.ACTION_SEND` share sheet | Opens system share sheet cleanly | PASS |
| **Add to Contacts** | Unknown Sender | `ContactsContract.Intents.Insert.ACTION` intent | Opens native Android contact creator | PASS |
| **Block & Report Spam** | Spam SMS | Adds number to blacklist table + AI spam trainer | Subsequent messages blocked silently | PASS |
| **Multi-Selection** | Batch of 15 msgs | Selected set tracked via `Set<Long>` message IDs | Batch delete / batch archive executed 100% | PASS |
