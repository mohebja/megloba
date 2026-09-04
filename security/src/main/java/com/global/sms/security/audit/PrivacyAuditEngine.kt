package com.global.sms.security.audit

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.WindowManager
import com.global.sms.security.prefs.SecurePreferencesManager
import com.global.sms.security.vault.PrivateVaultSecurityManager
import java.io.File

enum class BackupEncryptionStatus {
    ENCRYPTED,
    UNENCRYPTED,
    NO_BACKUP_FOUND
}

data class PrivacyAuditReport(
    val privacyScore: Int, // 0 to 100
    val isRooted: Boolean,
    val isDebuggable: Boolean,
    val isVaultEncrypted: Boolean,
    val backupStatus: BackupEncryptionStatus,
    val isBackupEncrypted: Boolean,
    val isScreenSecurityEnabled: Boolean,
    val securityWarnings: List<String>
)

class PrivacyAuditEngine(private val context: Context) {

    fun runAudit(): PrivacyAuditReport {
        val warnings = mutableListOf<String>()
        var score = 100

        // 1. Check Root Access
        val rooted = checkRoot()
        if (rooted) {
            score -= 30
            warnings.add("دستگاه روت شده شناسایی شد! احتمال دسترسی برنامه‌های مخرب به پیامک‌ها وجود دارد.")
        }

        // 2. Check Debug Mode
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            score -= 10
            warnings.add("برنامه در حالت عیب‌یابی (Debug) اجرا شده است.")
        }

        // 3. Check Vault Encryption Setup
        val vaultManager = PrivateVaultSecurityManager(context)
        val vaultEncrypted = vaultManager.isVaultSetup()
        if (!vaultEncrypted) {
            score -= 15
            warnings.add("گاوصندوق پیامک‌های خصوصی پیکربندی نشده و رمز عبور اختصاصی تعیین نشده است.")
        }

        // 4. Check Encrypted Backup Status (Three-state check)
        val backupStatus = checkBackupStatus()
        val backupEncrypted = (backupStatus == BackupEncryptionStatus.ENCRYPTED)
        when (backupStatus) {
            BackupEncryptionStatus.UNENCRYPTED -> {
                score -= 15
                warnings.add("فایل‌های پشتیبان رمزنگاری‌نشده یا با فرمت غیراستاندارد شناسایی شدند.")
            }
            BackupEncryptionStatus.NO_BACKUP_FOUND -> {
                // Not penalized in security score, but reported transparently
            }
            BackupEncryptionStatus.ENCRYPTED -> {
                // Secure verified container (.gsms.ebk / .enc)
            }
        }

        // 5. Check Screen Security (FLAG_SECURE)
        val screenSecurityEnabled = checkScreenSecurity()
        if (!screenSecurityEnabled) {
            score -= 10
            warnings.add("محافظت از صفحه نمایش و جلوگیری از اسکرین‌شات غیرفعال است.")
        }

        return PrivacyAuditReport(
            privacyScore = score.coerceIn(0, 100),
            isRooted = rooted,
            isDebuggable = isDebug,
            isVaultEncrypted = vaultEncrypted,
            backupStatus = backupStatus,
            isBackupEncrypted = backupEncrypted,
            isScreenSecurityEnabled = screenSecurityEnabled,
            securityWarnings = warnings
        )
    }

    private fun checkBackupStatus(): BackupEncryptionStatus {
        return try {
            val backupDirs = listOf(
                File(context.filesDir, "backups"),
                File(context.filesDir, "enterprise_backups")
            )
            val allFiles = backupDirs.flatMap { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.toList() ?: emptyList()
                } else emptyList()
            }
            if (allFiles.isEmpty()) {
                return BackupEncryptionStatus.NO_BACKUP_FOUND
            }
            // Check most recent backup file: Must be encrypted container format (.gsms.ebk or .enc)
            val latestBackup = allFiles.maxByOrNull { it.lastModified() }
            if (latestBackup != null) {
                if (latestBackup.name.endsWith(".gsms.ebk") || latestBackup.name.endsWith(".enc")) {
                    BackupEncryptionStatus.ENCRYPTED
                } else {
                    BackupEncryptionStatus.UNENCRYPTED
                }
            } else {
                BackupEncryptionStatus.NO_BACKUP_FOUND
            }
        } catch (e: Exception) {
            BackupEncryptionStatus.UNENCRYPTED
        }
    }

    private fun checkScreenSecurity(): Boolean {
        return try {
            val securePrefs = SecurePreferencesManager(context)
            val prefEnabled = securePrefs.isScreenshotProtectionEnabled
            if (context is Activity) {
                val flags = context.window.attributes.flags
                val windowSecure = (flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
                prefEnabled && windowSecure
            } else {
                prefEnabled
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }
}
