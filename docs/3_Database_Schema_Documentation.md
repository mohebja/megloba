# 3. Database Schema Documentation — Global SMS

**Database Name:** `global_sms_database.db`  
**ORM Framework:** Room (`androidx.room:2.6.1`)  

---

## 1. Table Definitions & Fields

### 1.1 `messages` Table
- `id` (LONG, Primary Key, AutoGenerate)
- `thread_id` (LONG, Indexed)
- `address` (TEXT, Indexed)
- `body` (TEXT)
- `timestamp` (LONG, Indexed)
- `type` (INTEGER: 1=Inbox, 2=Sent, 3=Draft)
- `status` (INTEGER: 0=Pending, 1=Sent, 2=Delivered, 3=Failed)
- `sub_id` (INTEGER: SIM slot subscription ID)
- `category_id` (TEXT, Category tag)
- `is_hidden` (BOOLEAN, Indexed, Vault flag)

### 1.2 `conversations` Table
- `thread_id` (LONG, Primary Key)
- `address` (TEXT, Indexed)
- `contact_name` (TEXT)
- `photo_uri` (TEXT)
- `last_message` (TEXT)
- `timestamp` (LONG, Indexed)
- `unread_count` (INTEGER)
- `is_archived` (BOOLEAN)
- `is_pinned` (BOOLEAN)
- `is_hidden` (BOOLEAN, Indexed)

### 1.3 Additional Tables
- `contacts`: Local contact cache & lookup keys.
- `contact_groups`: Enterprise SMS contact groups.
- `categories`: SMS category tags (OTP, Bank, Work, Personal, Spam).
- `scheduled_messages`: Future automated SMS dispatches.
- `classification_rules`: Keyword regex rules for automatic categorization.
