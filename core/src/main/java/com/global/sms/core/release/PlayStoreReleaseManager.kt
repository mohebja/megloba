package com.global.sms.core.release

import android.content.Context
import android.os.Build
import android.provider.Telephony

enum class PlayStoreComplianceStatus {
    COMPLIANT,
    ACTION_REQUIRED,
    NON_COMPLIANT
}

data class PlayStoreAuditReport(
    val targetSdk: Int,
    val isDefaultSmsHandlerCompliant: Boolean,
    val isTargetSdkCompliant: Boolean = targetSdk >= PlayStoreReleaseManager.MIN_TARGET_SDK,
    val permissionsSelfDeclared: Boolean = true,
    val dataSafetySelfDeclared: Boolean = true,
    @Deprecated("Subjective policy judgment, self-declared rather than device-verified", ReplaceWith("permissionsSelfDeclared"))
    val permissionsJustified: Boolean = permissionsSelfDeclared,
    @Deprecated("Play Console submission status is external, self-declared rather than device-verified", ReplaceWith("dataSafetySelfDeclared"))
    val dataSafetyDeclared: Boolean = dataSafetySelfDeclared,
    val verificationNotes: List<String> = emptyList(),
    val overallStatus: PlayStoreComplianceStatus
)

class PlayStoreReleaseManager(private val defaultContext: Context? = null) {

    companion object {
        const val MIN_TARGET_SDK = 35
    }

    val declaredPermissionsJustifications: Map<String, String> = mapOf(
        "android.permission.RECEIVE_SMS" to "Essential core SMS client functionality",
        "android.permission.READ_SMS" to "Required to display conversations and inbox",
        "android.permission.SEND_SMS" to "Required to send text messages",
        "android.permission.RECEIVE_MMS" to "Required for multimedia message reception",
        "android.permission.READ_CONTACTS" to "Required to resolve contact names and display avatars"
    )

    fun checkDefaultSmsHandler(context: Context?): Boolean {
        if (context == null) return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(android.app.role.RoleManager::class.java)
                roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_SMS) == true
            } else {
                Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
            }
        } catch (e: Exception) {
            false
        }
    }

    fun runPlayStoreReadinessCheck(
        context: Context? = defaultContext,
        overrideDefaultSms: Boolean? = null
    ): PlayStoreAuditReport {
        val resolvedTargetSdk = context?.applicationInfo?.targetSdkVersion ?: MIN_TARGET_SDK
        val isTargetSdkOk = resolvedTargetSdk >= MIN_TARGET_SDK

        val isDefaultSms = overrideDefaultSms ?: checkDefaultSmsHandler(context)

        val notes = mutableListOf<String>()
        if (!isTargetSdkOk) {
            notes.add("Target SDK $resolvedTargetSdk is below minimum Play Store requirement ($MIN_TARGET_SDK)")
        }
        if (!isDefaultSms) {
            notes.add("Application is not registered as the default SMS handler; SMS/MMS runtime access requires user role grant")
        }
        notes.add("Permissions justifications: self-declared for Play Store console submission")
        notes.add("Data Safety declarations: self-declared, verify form in Play Console")

        val status = when {
            !isTargetSdkOk -> PlayStoreComplianceStatus.NON_COMPLIANT
            !isDefaultSms -> PlayStoreComplianceStatus.ACTION_REQUIRED
            else -> PlayStoreComplianceStatus.COMPLIANT
        }

        return PlayStoreAuditReport(
            targetSdk = resolvedTargetSdk,
            isDefaultSmsHandlerCompliant = isDefaultSms,
            isTargetSdkCompliant = isTargetSdkOk,
            permissionsSelfDeclared = true,
            dataSafetySelfDeclared = true,
            verificationNotes = notes,
            overallStatus = status
        )
    }

    fun getDataSafetyDeclarationSummary(): Map<String, Any> {
        return mapOf(
            "dataCollection" to false,
            "dataEncryption" to true,
            "dataSharing" to false,
            "localStorageOnly" to true,
            "zeroTracking" to true,
            "sharingPolicy" to "Third-party sharing is disabled by default"
        )
    }
}
