# Sprint 4 Room Database Upgrade Report

## Database Version
- **Previous Version**: 17
- **Current Version**: 18
- **Migration**: `MIGRATION_17_18` in `GlobalSmsDatabase.kt`

## New Entities
1. `AiConversationInsightEntity` (`ai_conversation_insights`):
   - Stores local AI conversation intelligence (intent, sentiment, satisfaction, urgency, key terms).
2. `ThemeEntity` (`themes`):
   - Stores custom color schemes, AMOLED preferences, and theme marketplace configurations.
3. `NotificationRuleEntity` (`notification_rules`):
   - Stores channel priorities, lock screen privacy, sound, and vibration settings.
4. `VoiceCommandEntity` (`voice_commands`):
   - Records voice interactions and Persian command history.

## New DAOs
- `AiConversationInsightDao`
- `ThemeDao`
- `NotificationRuleDao`
- `VoiceCommandDao`
