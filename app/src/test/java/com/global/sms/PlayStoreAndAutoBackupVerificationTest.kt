package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.release.PlayStoreComplianceStatus
import com.global.sms.core.release.PlayStoreReleaseManager
import com.global.sms.security.backup.AutoBackupManager
import com.global.sms.security.prefs.SecurePreferencesManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlayStoreAndAutoBackupVerificationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testPlayStoreReleaseManager_realNonDefaultSmsCheck_returnsActionRequired() {
        val releaseManager = PlayStoreReleaseManager()

        // When the app is not registered as the default SMS handler in the system
        val audit = releaseManager.runPlayStoreReadinessCheck(context = context)

        assertEquals(context.applicationInfo.targetSdkVersion, audit.targetSdk)
        assertTrue(audit.targetSdk >= 34)
        // In clean test environment, test package is NOT default SMS handler
        assertFalse("App should not be default SMS handler in test environment", audit.isDefaultSmsHandlerCompliant)
        // overallStatus must reflect reality (ACTION_REQUIRED because SMS role not granted)
        assertEquals(PlayStoreComplianceStatus.ACTION_REQUIRED, audit.overallStatus)
        // Self-declared items should be explicitly labeled
        assertTrue(audit.permissionsSelfDeclared)
        assertTrue(audit.dataSafetySelfDeclared)
        assertTrue(audit.verificationNotes.any { it.contains("default SMS handler") })
    }

    @Test
    fun testPlayStoreReleaseManager_whenDefaultSmsRoleGranted_returnsCompliant() {
        val releaseManager = PlayStoreReleaseManager()

        // When default SMS handler is held / overridden
        val audit = releaseManager.runPlayStoreReadinessCheck(context = context, overrideDefaultSms = true)

        assertTrue(audit.isDefaultSmsHandlerCompliant)
        assertEquals(PlayStoreComplianceStatus.COMPLIANT, audit.overallStatus)
        assertTrue(audit.verificationNotes.any { it.contains("self-declared") })
    }

    @Test
    fun testPlayStoreReleaseManager_dataSafetyAndPermissionsDocumented() {
        val releaseManager = PlayStoreReleaseManager()
        val dataSafety = releaseManager.getDataSafetyDeclarationSummary()

        assertEquals(false, dataSafety["dataCollection"])
        assertEquals(true, dataSafety["dataEncryption"])
        assertEquals(false, dataSafety["dataSharing"])
        assertEquals(true, dataSafety["localStorageOnly"])
        assertEquals(true, dataSafety["zeroTracking"])

        assertTrue(releaseManager.declaredPermissionsJustifications.containsKey("android.permission.RECEIVE_SMS"))
        assertTrue(releaseManager.declaredPermissionsJustifications.containsKey("android.permission.SEND_SMS"))
    }

    @Test
    fun testAutoBackupManager_toggleAndScheduling() {
        val securePrefs = SecurePreferencesManager(context)

        // Verify initial toggle state
        AutoBackupManager.setAutoBackupEnabled(context, false)
        assertFalse(AutoBackupManager.isAutoBackupEnabled(context))
        assertFalse(securePrefs.isAutoBackupEnabled)

        // Enable auto backup with custom interval
        AutoBackupManager.setAutoBackupEnabled(context, true, 48L)
        assertTrue(AutoBackupManager.isAutoBackupEnabled(context))
        assertTrue(securePrefs.isAutoBackupEnabled)
        assertEquals(48L, AutoBackupManager.getAutoBackupIntervalHours(context))
        assertEquals(48L, securePrefs.autoBackupIntervalHours)

        // Disable auto backup
        AutoBackupManager.setAutoBackupEnabled(context, false)
        assertFalse(AutoBackupManager.isAutoBackupEnabled(context))
        assertFalse(securePrefs.isAutoBackupEnabled)

        // Calling init should not crash
        AutoBackupManager.init(context)
    }

    @Test
    fun testAutoBackup_hardwareKeyEncryptionAndRestoration() {
        val masterKey1 = com.global.sms.security.keystore.KeyStoreManager.getOrCreateAutoBackupMasterKeyBytes(context)
        assertNotNull(masterKey1)
        assertEquals(32, masterKey1.size)

        // Must be persistent across invocations
        val masterKey2 = com.global.sms.security.keystore.KeyStoreManager.getOrCreateAutoBackupMasterKeyBytes(context)
        assertArrayEquals(masterKey1, masterKey2)

        val sampleModel = com.global.sms.security.backup.EnterpriseBackupModel(
            version = 1,
            timestamp = System.currentTimeMillis(),
            messages = listOf(
                com.global.sms.security.backup.BackupMessageItem(
                    id = 101L,
                    threadId = 1L,
                    address = "+1987654321",
                    body = "Secret auto-backup message payload",
                    date = System.currentTimeMillis(),
                    type = 1,
                    read = 1,
                    status = -1
                )
            )
        )

        val backupFile = com.global.sms.security.backup.EncryptedBackupManager.createEncryptedBackupWithMasterKey(
            context = context,
            model = sampleModel,
            masterKey = masterKey1
        )
        assertTrue(backupFile.exists())
        assertTrue(backupFile.length() > 0)

        val restoredModel = com.global.sms.security.backup.EncryptedBackupManager.restoreEncryptedBackupWithMasterKey(
            backupFile = backupFile,
            masterKey = masterKey1
        )
        assertEquals(1, restoredModel.messages.size)
        assertEquals("Secret auto-backup message payload", restoredModel.messages[0].body)
        assertEquals("+1987654321", restoredModel.messages[0].address)
    }
}
