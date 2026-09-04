package com.global.sms.core.plugin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class PluginCategory {
    BANKING,
    CRM,
    SECURITY,
    AUTOMATION
}

data class EnterprisePluginManifest(
    val pluginId: String,
    val name: String,
    val developer: String,
    val version: String,
    val category: PluginCategory,
    val description: String,
    val requiredPermissions: List<String>,
    val isInstalled: Boolean = false,
    val isSandboxEnabled: Boolean = true,
    val iconResName: String = "ic_plugin_default"
)

data class PluginExecutionResult(
    val executionId: String = UUID.randomUUID().toString(),
    val pluginId: String,
    val status: String, // "SUCCESS", "BLOCKED_BY_PERMISSIONS", "SANDBOX_VIOLATION"
    val outputData: String,
    val timestamp: Long = System.currentTimeMillis()
)

class PluginEngine {

    private val _availablePlugins = MutableStateFlow<List<EnterprisePluginManifest>>(
        listOf(
            EnterprisePluginManifest(
                pluginId = "plugin_bank_fintech_v1",
                name = "Banking & OTP Financial Sentinel",
                developer = "Global SMS Fintech Lab",
                version = "1.4.0",
                category = PluginCategory.BANKING,
                description = "استخراج و تحلیل خودکار تراکنش‌های بانکی، موجودی و پیامک‌های رمز پویا با امنیت بالا.",
                requiredPermissions = listOf("READ_SMS", "PARSE_BANK_FORMAT")
            ),
            EnterprisePluginManifest(
                pluginId = "plugin_crm_lead_v2",
                name = "CRM Lead & Customer 360 Collector",
                developer = "Enterprise Dynamics",
                version = "2.0.1",
                category = PluginCategory.CRM,
                description = "ثبت هوشمند سرنخ‌های فروش و همگام‌سازی کانتکت‌های تجاری در دیتابیس محلی CRM.",
                requiredPermissions = listOf("READ_SMS", "WRITE_CONTACTS", "CRM_LOCAL_DB")
            ),
            EnterprisePluginManifest(
                pluginId = "plugin_security_shield_v3",
                name = "Zero-Trust Anti-Phishing Shield",
                developer = "Global SMS CyberSec",
                version = "3.1.0",
                category = PluginCategory.SECURITY,
                description = "اسکن پیشرفته لینک‌های آلوده، شناساگر بدافزارها و جلوگیری از حملات Smishing.",
                requiredPermissions = listOf("SECURITY_AUDIT", "URL_INSPECTOR")
            ),
            EnterprisePluginManifest(
                pluginId = "plugin_automation_flow_v1",
                name = "Automated Workflow Synthesizer",
                developer = "Workflow Engine Lab",
                version = "1.1.2",
                category = PluginCategory.AUTOMATION,
                description = "ساخت و اجرای سناریوهای خودکارسازی بر اساس کلیدواژه‌ها و فرستنده‌های سازمانی.",
                requiredPermissions = listOf("EXECUTE_WORKFLOW", "SEND_SMS")
            )
        )
    )
    val availablePlugins: StateFlow<List<EnterprisePluginManifest>> = _availablePlugins.asStateFlow()

    private val _executionLogs = MutableStateFlow<List<PluginExecutionResult>>(emptyList())
    val executionLogs: StateFlow<List<PluginExecutionResult>> = _executionLogs.asStateFlow()

    fun installPlugin(pluginId: String): Boolean {
        _availablePlugins.value = _availablePlugins.value.map {
            if (it.pluginId == pluginId) it.copy(isInstalled = true) else it
        }
        return true
    }

    fun uninstallPlugin(pluginId: String): Boolean {
        _availablePlugins.value = _availablePlugins.value.map {
            if (it.pluginId == pluginId) it.copy(isInstalled = false) else it
        }
        return true
    }

    fun executeInSandbox(
        pluginId: String,
        inputPayload: String,
        grantedPermissions: List<String>
    ): PluginExecutionResult {
        val plugin = _availablePlugins.value.find { it.pluginId == pluginId }
            ?: return PluginExecutionResult(
                pluginId = pluginId,
                status = "PLUGIN_NOT_FOUND",
                outputData = "افزونه یافت نشد."
            )

        if (!plugin.isInstalled) {
            return PluginExecutionResult(
                pluginId = pluginId,
                status = "NOT_INSTALLED",
                outputData = "افزونه هنوز نصب نشده است."
            )
        }

        // Verify permissions
        val missingPermissions = plugin.requiredPermissions.filter { !grantedPermissions.contains(it) }
        if (missingPermissions.isNotEmpty()) {
            val result = PluginExecutionResult(
                pluginId = pluginId,
                status = "BLOCKED_BY_PERMISSIONS",
                outputData = "دسترسی‌های لازم داده نشده است: $missingPermissions"
            )
            _executionLogs.value = listOf(result) + _executionLogs.value
            return result
        }

        val output = when (plugin.category) {
            PluginCategory.BANKING -> "سندیکای مالی: تراکنش $inputPayload تحلیل و دسته‌بندی شد."
            PluginCategory.CRM -> "سیستم CRM: سرنخ مشتری جدید برای $inputPayload ثبت گردید."
            PluginCategory.SECURITY -> "سپر امنیتی: اسکن لینک کامل شد. صفر تهدید شناساگر شد."
            PluginCategory.AUTOMATION -> "موتور خودکارسازی: سناریوی مربوط به $inputPayload اجرا گردید."
        }

        val result = PluginExecutionResult(
            pluginId = pluginId,
            status = "SUCCESS",
            outputData = output
        )
        _executionLogs.value = listOf(result) + _executionLogs.value
        return result
    }
}
