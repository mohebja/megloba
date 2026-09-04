# Global SMS - Database Documentation

## Database Overview
Global SMS uses **Room Database** (`GlobalSmsDatabase`) with local persistence and encrypted fields for sensitive data.

### Tables Schema

#### 1. `messages`
- `id`: Long (PK, Auto-Increment)
- `threadId`: Long (Indexed)
- `address`: String
- `body`: String
- `timestamp`: Long
- `type`: Int (1=INBOX, 2=SENT, 3=DRAFT, 4=OUTBOX)
- `simSlot`: Int (0=SIM 1, 1=SIM 2)
- `isRead`: Boolean
- `isHidden`: Boolean
- `category`: String (PERSONAL, BANK, WORK, IMPORTANT, SPAM, PRIVATE)
- `isPinned`: Boolean
- `isEncrypted`: Boolean
- `otpCode`: String (Nullable)

#### 2. `conversations`
- `threadId`: Long (PK)
- `address`: String
- `contactName`: String (Nullable)
- `lastMessage`: String
- `lastTimestamp`: Long
- `unreadCount`: Int
- `category`: String
- `isPinned`: Boolean
- `isHidden`: Boolean
- `avatarUri`: String (Nullable)

#### 3. `scheduled_messages`
- `id`: Long (PK, Auto-Increment)
- `address`: String
- `body`: String
- `scheduledTimestamp`: Long
- `simSlot`: Int
- `status`: String (PENDING, SENT, CANCELLED, FAILED)

#### 4. `quick_replies`
- `id`: Long (PK, Auto-Increment)
- `title`: String
- `content`: String
