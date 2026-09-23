# Writing to the system SMS provider

As the default SMS app, this app is now responsible for keeping `content://sms` in sync — the database
every other app (backups, "recent messages" surfaces, a future default-app switch) reads. Before this
change nothing was ever written there: incoming and outgoing messages only ever existed in the app's own
(encrypted) database, invisible to the rest of the system. MMS (`content://mms`) is **not** covered — its
PDU-part format is a separate, larger piece of work; see the earlier code-review notes on MMS.

## Where it happens

| Event | Local DB | System provider (`SystemSmsProvider`) |
|---|---|---|
| SMS arrives, this app is default (`SMS_DELIVER_ACTION`) | `MessageDispatcher.onIncomingSms` inserts the row | `insertIncoming()` → `Sms.Inbox`, id saved as `MessageEntity.systemSmsId` |
| SMS arrives, this app is **not** default (`SMS_RECEIVED_ACTION`) | inserted as before | **not written** — the other, default app already wrote it |
| User sends an SMS | `SmsQueueManager.enqueueMessage` inserts an Outbox row | `insertOutbox()` → `Sms.Outbox` |
| `SmsManager` confirms the send | `DeliveryReportReceiver` marks `SENT` | `markSent()` → row moves to `Sms.Sent` |
| Carrier confirms delivery | `DeliveryReportReceiver` marks `DELIVERED` | `markDelivered()` → `STATUS_COMPLETE` |
| Every retry fails | `SmsRetryManager` marks `FAILED` | `markFailed()` → row moves to `Sms.Failed` |
| Message / thread deleted | deleted locally | `SystemSmsProvider.delete()` / `deleteAll()` (ids read from `systemSmsId` before the local delete) |
| Thread marked read | `messageDao.markThreadAsRead` | `markRead()` on every linked row |

Every `SystemSmsProvider` call checks `isDefaultSmsApp()` (or fails soft on `SecurityException`) first: the
user can switch the default SMS app at any time, and the provider rejects writes from anyone else. A failed
mirror write is logged and otherwise ignored — the app's own database is authoritative either way.

## Data model change

`MessageEntity.systemSmsId: Long?` (migration 30→31) links a local row to its `content://sms` row so updates
and deletes touch the same entry instead of creating duplicates. It is `null` for messages written before
this version, or while a different app was the default.

Thread ids are **not** unified: the app's own `MessageEntity.threadId` (currently `address.hashCode()`,
a separate known issue) is untouched. Writes to the system provider resolve their own `thread_id` through
`Telephony.Threads.getOrCreateThreadId()`, the canonical id every other app reading `content://sms` expects.

## Known limitation: Private Vault

Messages moved into the in-app Private Vault are hidden in this app's UI, but nothing here removes their
copy from `content://sms` (or unhides it either) — the system provider row was written in plain text when
the message first arrived, before it was ever moved to the vault. A message "hidden" in-app can therefore
still be visible in the stock Messages app, backups, or anything else reading the system provider. Making
the vault meaningfully private would need the vault to also delete (or the app to never have written) the
system-provider row for a vaulted message; out of scope here — flagging it since it affects the vault's
actual privacy guarantee.
