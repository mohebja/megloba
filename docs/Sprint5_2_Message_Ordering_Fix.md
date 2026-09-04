# Sprint 5.2 Message Thread Ordering Fix

## Issue Description
Real device testing revealed that opening a chat thread displayed messages starting from the oldest message at the top, requiring manual scrolling down to view recent messages.

## Root Cause Analysis
1. **Database Query Order**: `MessageDao` queried thread messages with `ORDER BY timestamp ASC`, returning the oldest message at index 0.
2. **Compose List Layout**: `LazyColumn` had `reverseLayout = false`. Because Compose LazyColumn defaults to scroll position 0 (the top item), index 0 (oldest message) was displayed first upon opening a thread.

## Applied Fixes
1. **DAO Query Update**:
   - Modified `MessageDao.kt` (`Daos.kt`) queries (`getMessagesForThread`, `getMessagesForThreadPagingSource`, `getMessagesForThreadPaged`) to `ORDER BY timestamp DESC`.
   - Result: Index 0 now represents the most recent message.
2. **Compose Layout Adaptation**:
   - Updated `MessageThreadScreen.kt` and `ClassicMessageThreadScreen.kt` to set `reverseLayout = true` on `LazyColumn`.
   - Result: Index 0 (newest message) is positioned anchored at the bottom of the viewport automatically when opening the screen or receiving a new SMS. Scrolling UP loads older messages seamlessly.
3. **Unit Test Verification**:
   - Added `MessageOrderingTest.kt` verifying descending timestamp sorting logic.
