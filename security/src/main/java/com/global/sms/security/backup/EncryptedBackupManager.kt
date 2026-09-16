    fun createEncryptedBackupWithMasterKey(
        context: Context,
        model: EnterpriseBackupModel,
        masterKey: ByteArray,
        targetFile: File? = null
    ): File {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val destFile = targetFile ?: File(
            backupDir,
            "autobackup_${System.currentTimeMillis()}.gsms"
        )

        val jsonPayload = model.toJson()
        val provider = BackupProvider(masterKey = masterKey)
        provider.createEncryptedBackup(destFile, jsonPayload, password = null)
        return destFile
    }

    fun restoreEncryptedBackupWithMasterKey(
        backupFile: File,
        masterKey: ByteArray
    ): EnterpriseBackupModel {
        val provider = BackupProvider(masterKey = masterKey)
        val jsonPayload = provider.restoreEncryptedBackup(backupFile, password = null)
        return EnterpriseBackupModel.fromJson(jsonPayload)
    }
