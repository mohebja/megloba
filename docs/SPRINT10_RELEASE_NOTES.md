# Global SMS — Sprint 10 Release Notes
## Business Edition & Enterprise Ecosystem Expansion (v9.0.0-ENT / Build 900)

### Highlights
1. **Enterprise Organization Model & Hierarchy**:
   - Organization, Department, and Employee entities.
   - Dynamic role management and organization structure.

2. **Role-Based Access Control (RBAC)**:
   - Built-in roles: OWNER, ADMIN, MANAGER, EMPLOYEE, VIEW_ONLY.
   - Granular permissions for SMS sending, CRM management, backup/export, and automation.
   - Interactive `PermissionManagementScreen.kt` for live role switching and permission audit.

3. **Enterprise Executive Dashboard Upgrade**:
   - Organization overview header with total employees and active campaigns.
   - Department-level analytics and employee communication statistics.
   - Security score and AI insights overview.

4. **Business Messaging Workspace**:
   - Campaign preview and live variable substitution (`{name}`, `{invoice}`, `{date}`, `{code}`).
   - Approval workflows requiring manager sign-off prior to dispatch.
   - Scheduled message dispatch.

5. **Customer 360 & Advanced CRM 2.0**:
   - Lifecycle stage tracking (Lead, Customer, VIP, Inactive).
   - 360-degree interactive timeline capturing SMS, Calls, Tasks, Notes, and AI Insights.

6. **Enterprise Automation Engine**:
   - New triggers: New Customer Message, Payment Reminder, Keyword/Complaint Detected, VIP Message.
   - Extended actions: Create Task, Notify Manager, Generate Reply Suggestion, Categorize Customer.

7. **Multi-Device Sync Engine Foundation**:
   - End-to-End Encrypted (E2EE) sync across Phone, Tablet, Foldable, and Desktop companion devices.
   - Support for Settings, Contacts, Groups, Templates, CRM Metadata, and Tasks.

8. **Adaptive Workspace for Tablets & Foldables**:
   - Single-pane (Phone), Two-pane (Tablet), and Three-pane (Foldable) responsive layouts.

9. **Wear OS Companion Engine**:
   - Notification mirroring, quick replies, and AI summaries for smartwatches.

10. **Enterprise Backup & Selective Restore**:
    - Full organization and department-scoped backups.
    - Selective restore capabilities with immutable audit trail.

11. **Database Upgrade (Room v24 → v25)**:
    - Added `organizations`, `departments`, `employees`, `permissions`, `sync_logs`, and `audit_trail` tables.
    - Executed clean `MIGRATION_24_25`.

12. **Enterprise Security & Compliance Audit**:
    - Full RBAC, export policy, data isolation, and AES-256-GCM verification.
