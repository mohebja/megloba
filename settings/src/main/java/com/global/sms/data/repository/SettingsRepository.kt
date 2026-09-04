package com.global.sms.data.repository

import com.global.sms.data.dao.*
import com.global.sms.data.entity.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao,
    private val categoryDao: CategoryDao,
    private val classificationRuleDao: ClassificationRuleDao? = null
) {

    val settingsFlow: Flow<SettingsEntity> = settingsDao.getSettings()
        .map { it ?: SettingsEntity() }

    val categoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    val classificationRulesFlow: Flow<List<ClassificationRuleEntity>> =
        classificationRuleDao?.getAllRulesFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun getSettingsOnce(): SettingsEntity {
        return settingsDao.getSettingsOnce() ?: SettingsEntity()
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.deleteCategoryById(id)
    }

    suspend fun insertClassificationRule(rule: ClassificationRuleEntity): Long {
        return classificationRuleDao?.insertRule(rule) ?: 0L
    }

    suspend fun updateClassificationRule(rule: ClassificationRuleEntity) {
        classificationRuleDao?.updateRule(rule)
    }

    suspend fun deleteClassificationRule(id: Long) {
        classificationRuleDao?.deleteRuleById(id)
    }

    suspend fun setClassificationRuleEnabled(id: Long, isEnabled: Boolean) {
        classificationRuleDao?.setRuleEnabled(id, isEnabled)
    }

    suspend fun seedDefaultRulesIfEmpty() {
        if (classificationRuleDao == null) return
        val count = classificationRuleDao.getRulesCount()
        if (count == 0) {
            com.global.sms.core.classifier.SmsClassifierEngine.DEFAULT_RULES.forEach { defaultRule ->
                classificationRuleDao.insertRule(defaultRule.copy(id = 0))
            }
        }
    }

    suspend fun seedDefaultCategoriesIfEmpty() {
        // Will check if settings or categories are empty and seed default values
        val existingSettings = settingsDao.getSettingsOnce()
        if (existingSettings == null) {
            settingsDao.insertOrUpdateSettings(SettingsEntity())
        }

        // Seed categories if empty
        val defaultCategories = listOf(
            CategoryEntity(
                name = "شخصی",
                description = "پیامک‌های عمومی، دوستان و خانواده",
                icon = "People",
                color = 0xFF1A73E8,
                priority = 10,
                autoRule = ""
            ),
            CategoryEntity(
                name = "بانک و مالی",
                description = "تراکنش‌های بانکی، رمز دوم پویا و حساب‌ها",
                icon = "AccountBalance",
                color = 0xFF00658F,
                priority = 100,
                autoRule = "بانک,رمز,OTP,واریز,برداشت,کارت,Melli,Mellat,Tejarat,Saman,Parsian,Refah,Pasargad,Blue"
            ),
            CategoryEntity(
                name = "کاری و اداری",
                description = "پیامک‌های مربوط به جلسه، پروژه و شرکت",
                icon = "Work",
                color = 0xFF70538C,
                priority = 50,
                autoRule = "جلسه,اداره,شرکت,پروژه,فاکتور,صورتحساب"
            ),
            CategoryEntity(
                name = "مهم و ضروری",
                description = "پیامک‌های با اولویت بالا و کدهای ورود",
                icon = "Star",
                color = 0xFFBA1A1A,
                priority = 80,
                autoRule = "مهم,اورژانس,فوری,کد تایید,رمز ورود"
            ),
            CategoryEntity(
                name = "تبلیغات و اسپم",
                description = "پیامک‌های تبلیغاتی، تخفیف‌ها و قرعه‌کشی",
                icon = "Notifications",
                color = 0xFFD32F2F,
                priority = 90,
                autoRule = "تخفیف,فروش,تبلیغ,جشنواره,شارژ,لغو11,قرعه کشی,برنده"
            ),
            CategoryEntity(
                name = "خرید و خدمات",
                description = "سفارش‌های اینترنتی، تاکسی آنلاین و پست",
                icon = "ShoppingCart",
                color = 0xFF006A6A,
                priority = 40,
                autoRule = "دیجی کالا,اسنپ,تپسی,سفارش,کد تخفیف,پست"
            )
        )

        defaultCategories.forEach { category ->
            categoryDao.insertCategory(category)
        }
    }

    fun matchCategoryForMessage(sender: String, body: String, categories: List<CategoryEntity>): CategoryEntity? {
        // Evaluate categories ordered by priority DESC
        val sorted = categories.sortedByDescending { it.priority }
        for (cat in sorted) {
            if (cat.autoRule.isBlank()) continue
            val keywords = cat.autoRule.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            for (kw in keywords) {
                if (body.contains(kw, ignoreCase = true) || sender.contains(kw, ignoreCase = true)) {
                    return cat
                }
            }
        }
        return null
    }
}
