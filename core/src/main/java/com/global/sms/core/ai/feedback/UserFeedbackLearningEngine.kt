package com.global.sms.core.ai.feedback

import com.global.sms.data.entity.AiFeedbackEntity
import com.global.sms.data.entity.ClassificationRuleEntity
import java.util.Locale

object UserFeedbackLearningEngine {

    /**
     * Converts user reclassification feedback into a localized priority rule.
     */
    fun createRuleFromFeedback(feedback: AiFeedbackEntity): ClassificationRuleEntity {
        val cleanSender = feedback.senderPattern.trim().lowercase(Locale.ROOT)
        return ClassificationRuleEntity(
            name = "یادگیری ترجیحات کاربر: ${feedback.userSelectedCategory}",
            targetCategory = feedback.userSelectedCategory,
            keywords = feedback.keywordSnippet,
            senderPattern = cleanSender,
            ruleType = if (cleanSender.isNotBlank()) "SENDER" else "KEYWORD",
            priority = 110, // Higher priority than system default rules
            isEnabled = true,
            createdTimestamp = System.currentTimeMillis()
        )
    }
}
