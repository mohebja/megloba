# Global SMS — Professional UI/UX Audit & Improvement Report

## Executive assessment

The project is substantially feature-rich and already contains three distinct interaction experiences:

1. Classic — traditional SMS workflow.
2. Smart — AI-assisted messaging workflow.
3. Enterprise — business/CRM-oriented workspace.

The main product risk is not lack of features; it is **visual and interaction fragmentation** caused by the number of screens and feature surfaces. The next phase should consolidate these surfaces around one design system, clearer information hierarchy, and task-oriented navigation.

## Findings

### 1. Design-system consistency — High priority
- A shared Material 3 theme exists, but spacing, corner radii, elevations and component styling are still repeated locally across screens.
- `GlobalSmsShapes` and `GlobalSmsDimens` were added as a single visual source of truth.
- The existing color system is usable, but category colors and custom user colors need a contrast validation layer before being applied to text/background pairs.

### 2. Conversation list UX — High priority
- Classic conversation rows combine swipe handling, long press handling and an additional clickable container. This can create duplicated gesture semantics and accessibility ambiguity.
- The duplicate click/long-click layer was removed from `ClassicConversationsScreen`; the row now has one clear interaction owner.
- Smart mode exposes four top-bar actions (OTP, banking, search, settings). This is feature-rich but visually dense. A future iteration should move secondary actions into a single overflow menu and keep search/compose as primary actions.

### 3. Message composer — High priority
- The composer contains attachments, emoji, quick replies, AI actions, segment information and send controls in a very dense vertical stack.
- Keyboard/IME behavior was not explicitly handled at the composer container level.
- The composer now uses `imePadding()` so the input remains visible above the software keyboard.
- The send action is now disabled when the message is blank and trims whitespace before sending.

### 4. Tablet/foldable UX — High priority
- The adaptive layout already supports compact/medium/expanded concepts.
- The previous tablet list pane used a fixed 380dp width. On smaller medium-width devices this can leave an unnecessarily constrained detail pane.
- The pane is now responsive: approximately 38% of available width, clamped to 320–420dp.

### 5. Empty states — Medium priority
- The tablet detail empty state contained implementation-oriented copy ("two-pane display for tablets...").
- It was replaced with task-oriented user language: select a conversation to begin.

### 6. Typography and Persian/RTL — High priority
- RTL is explicitly supported and message font scaling exists.
- Persian/Arabic typography should be treated as a first-class design requirement, not simply a layout-direction switch.
- Recommended next phase: bundle and test a high-quality Persian-capable font family, establish explicit display/body/label roles, and validate line height at 100%, 125%, 150% and 200% scaling.

### 7. Navigation architecture — High priority
- `MainActivity` currently owns a very large application navigation graph and directly decides between Classic/Enterprise/Adaptive experiences.
- This increases coupling and makes navigation behavior harder to test.
- Recommended next phase: extract root navigation into a dedicated navigation module/package with typed destinations and one source of truth for conversation style.

### 8. Smart-mode data UX — High priority
- Category filtering is currently performed at the UI layer over a Paging stream.
- For large message databases this can produce poor pagination behavior because pages are loaded without considering the selected category.
- Recommended next phase: move category filtering into the repository/DAO query and expose a category-aware Paging flow.

### 9. Enterprise information architecture — Medium priority
- Enterprise contains CRM, analytics, automation, security and customer 360 capabilities.
- These should be grouped into a persistent business navigation model rather than exposing every capability as a peer destination.
- Recommended structure: Workspace / Customers / Messaging / Automation / Analytics / Security / Settings.

### 10. Accessibility — High priority
The project has many content descriptions and test tags, which is a good foundation. A production audit should additionally verify:
- 48dp minimum touch targets.
- TalkBack traversal order in RTL.
- State announcements for unread/read, sent/delivered/failed and private messages.
- Contrast for user-customized colors.
- Dynamic font scaling without clipped controls.
- Reduced-motion behavior for animations.
- Semantic labels for swipe actions.

## Implemented in this revision

- Added `GlobalSmsDesignTokens.kt` with shared spacing, sizing and shape tokens.
- Connected the shared shape system to the Material 3 theme.
- Made the tablet/foldable conversation pane responsive instead of fixed-width.
- Simplified the tablet empty-detail state.
- Removed duplicated click/long-click gesture ownership in Classic conversations.
- Added IME-aware message composer behavior.
- Disabled Send while the composer is empty and trimmed outgoing text.

## Recommended next development sequence

### Phase A — Visual system
- Consolidate all screens onto shared tokens.
- Create reusable `AppTopBar`, `SectionHeader`, `ConversationListItem`, `MessageBubble`, `EmptyState`, `LoadingState`, `ErrorState`, `PermissionBanner` and `SettingsSection` components.
- Define a single spacing scale and elevation scale.
- Add dark/AMOLED contrast tests.

### Phase B — Core messaging UX
- Redesign the conversation list hierarchy.
- Redesign the message composer around a compact primary row plus contextual tool trays.
- Add message status visuals and clearer delivery/error recovery.
- Add a consistent search/filter experience.

### Phase C — Navigation
- Extract typed navigation.
- Add a unified root shell.
- Use bottom navigation only for genuinely primary destinations; keep advanced tools in contextual menus.

### Phase D — Enterprise
- Introduce a persistent enterprise workspace shell.
- Consolidate CRM, automation, analytics and security into task-oriented sections.
- Add desktop/tablet keyboard navigation.

### Phase E — QA
- Screenshot regression tests for Classic, Smart and Enterprise.
- Accessibility tests with large font scale and RTL.
- Performance tests with 10,000+ conversations.
- Paging/filtering tests for category views.
- Device matrix: compact phone, large phone, foldable, tablet, Chromebook.
