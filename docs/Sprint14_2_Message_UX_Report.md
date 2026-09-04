# Sprint 14.2 — Message Thread UX & Pinch-Zoom Audit

## 1. Message Ordering Architecture
* **Database Query (`MessageDao`):** `SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC`
* **Conversation List Rendering:**
  * Chronological ordering: oldest message at top, newest at bottom.
  * Opening a conversation auto-scrolls to the latest message.
  * Live incoming and outgoing messages append immediately with smooth animation.

## 2. Dynamic Typography & Pinch-to-Zoom Scaling
* **Mathematical Line-Height Scaling Formula:**
  $$\text{lineHeight} = \text{fontSize} \times 1.42 + 2\text{sp} \ge \text{fontSize} \times 1.35$$
* **Multi-Scale Verification Matrix:**
  * **12sp:** Line-height: 19.04sp (Ratio: 1.58) — Crisp, compact view, zero clipping.
  * **16sp:** Line-height: 24.72sp (Ratio: 1.54) — Standard body text, high readability.
  * **20sp:** Line-height: 30.40sp (Ratio: 1.52) — Medium-large accessibility mode.
  * **24sp:** Line-height: 36.08sp (Ratio: 1.50) — Large text mode, dynamic bubble padding.
  * **28sp:** Line-height: 41.76sp (Ratio: 1.49) — Senior accessibility mode.
  * **32sp:** Line-height: 47.44sp (Ratio: 1.48) — Maximum zoom mode, bubble containers expand naturally.
