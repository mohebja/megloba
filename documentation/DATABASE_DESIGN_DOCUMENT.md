# Global SMS — Database Design Document

## 1. ER Schema Overview
The database consists of 4 main tables:
- `sms_messages` (Stores SMS & MMS message metadata and contents)
- `conversations` (Stores aggregated thread statistics, unread count, pin/archive state)
- `contacts` (Stores cached contact information, E.164 normalized numbers, Persian display names)
- `vault_messages` (Stores AES-256 encrypted private messages)

## 2. Indexing Strategy
- `sms_messages`: Indexes on `threadId`, `timestamp`, `address`.
- `contacts`: Index on `normalizedNumber` for O(1) caller lookup.
