package com.global.sms.engine.dispatcher

import android.content.Context
import android.util.Log
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.AutomationRuleEntity
import com.global.sms.data.entity.SecurityAuditLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enterprise Automation Dispatcher.
 * Evaluates custom automation rules and executes corresponding automated actions
 * (Auto-reply, Forwarding, Audit logging, and Status marking) when new SMS arrives.
 */
object EnterpriseAutomationDispatcher {

    private const val TAG = "EnterpriseAutomation"

    suspend fun evaluateAndExecute(
        context: Context,
        address: String,
        body: String,
        simSlot: Int = 0
    ) = withContext(Dispatchers.IO) {
        try {
            val db = GlobalSmsDatabase.getInstance(context)
            val activeRules = db.automationRuleDao().getEnabledRules()
            if (activeRules.isEmpty()) return@withContext

            val isSavedContact = db.contactDao().getContactByPhone(address) != null
            val isCrmCustomer = db.crmCustomerDao().getCustomerByPhone(address) != null

            for (rule in activeRules) {
                // 1. Verify Sender Type Condition
                val matchesSenderCondition = when (rule.conditionSenderType.uppercase()) {
                    "CRM_ONLY" -> isCrmCustomer
                    "UNKNOWN" -> !isSavedContact
                    else -> true // "ALL"
                }

                if (!matchesSenderCondition) continue

                // 2. Verify Trigger Keyword
                val matchesKeyword = rule.triggerKeyword.isBlank() ||
                    body.contains(rule.triggerKeyword, ignoreCase = true)

                if (!matchesKeyword) continue

                // 3. Execute Action
                Log.i(TAG, "Executing rule '${rule.name}' for sender $address")

                when (rule.actionType.uppercase()) {
                    "AUTO_REPLY" -> {
                        val replyText = rule.actionValue.trim()
                        if (replyText.isNotEmpty()) {
                            MessageDispatcher.dispatchSendMessage(
                                context = context,
                                address = address,
                                body = replyText,
                                simSlot = simSlot
                            )
                        }
                    }
                    "FORWARD_SMS" -> {
                        val forwardTarget = rule.actionValue.trim()
                        if (forwardTarget.isNotEmpty()) {
                            val forwardBody = "[انتقال خودکار از $address]:\n$body"
                            MessageDispatcher.dispatchSendMessage(
                                context = context,
                                address = forwardTarget,
                                body = forwardBody,
                                simSlot = simSlot
                            )
                        }
                    }
                }

                // 4. Record Audit Log
                try {
                    db.securityAuditLogDao().insertLog(
                        SecurityAuditLogEntity(
                            eventType = "AUTOMATION_TRIGGER",
                            description = "اجرای خودکار قانون '${rule.name}' (${rule.actionType}) برای فرستنده $address",
                            operatorName = "موتور اتوماسیون",
                            ipOrDeviceId = "DEVICE_LOCAL"
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Could not insert audit log for rule: ${rule.name}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in EnterpriseAutomationDispatcher", e)
        }
    }
}
