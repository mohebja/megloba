package com.global.sms.core.ai.emotion

enum class EmotionState {
    POSITIVE,
    NEGATIVE,
    URGENT,
    ANGRY,
    SATISFIED,
    CONCERNED,
    NEUTRAL
}

data class EmotionAnalysisResult(
    val primaryEmotion: EmotionState,
    val intensityScore: Int, // 0 to 100
    val priorityBoost: Int, // Priority adjustment value
    val localizedDescription: String
)

object EmotionAnalysisEngine {

    fun analyzeMessage(text: String): EmotionAnalysisResult {
        val clean = text.lowercase()

        return when {
            clean.contains("شکایت") || clean.contains("خراب") || clean.contains("مزخرف") || clean.contains("افتضاح") -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.ANGRY,
                    intensityScore = 90,
                    priorityBoost = 40,
                    localizedDescription = "پیام شامل عصبانیت و نارضایتی شدید خریدار/مخاطب است"
                )
            }
            clean.contains("فوری") || clean.contains("سریع") || clean.contains("عاجل") || clean.contains("کمک") -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.URGENT,
                    intensityScore = 95,
                    priorityBoost = 50,
                    localizedDescription = "پیام دارای اولویت فوری است و نیاز به پاسخ سریع دارد"
                )
            }
            clean.contains("نگران") || clean.contains("چی شد") || clean.contains("تاخیر") || clean.contains("پیگیری") -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.CONCERNED,
                    intensityScore = 75,
                    priorityBoost = 25,
                    localizedDescription = "مخاطب نگران وضعیت سفارش یا درخواست خود است"
                )
            }
            clean.contains("عالی") || clean.contains("دستت درد نکنه") || clean.contains("فوق‌العاده") || clean.contains("راضی") -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.SATISFIED,
                    intensityScore = 85,
                    priorityBoost = 10,
                    localizedDescription = "مخاطب رضایت کامل خود را ابراز نموده است"
                )
            }
            clean.contains("ممنون") || clean.contains("مرسی") || clean.contains("خوب") || clean.contains("سپاس") -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.POSITIVE,
                    intensityScore = 70,
                    priorityBoost = 5,
                    localizedDescription = "لحن پیام مثبت و محترمانه است"
                )
            }
            clean.contains("بد") || clean.contains("متاسف") || clean.contains("نمیشه") -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.NEGATIVE,
                    intensityScore = 65,
                    priorityBoost = 15,
                    localizedDescription = "لحن پیام منفی یا عدم موافقت است"
                )
            }
            else -> {
                EmotionAnalysisResult(
                    primaryEmotion = EmotionState.NEUTRAL,
                    intensityScore = 50,
                    priorityBoost = 0,
                    localizedDescription = "لحن پیام خنثی یا عادی است"
                )
            }
        }
    }
}
