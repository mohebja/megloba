package com.global.sms.core.sim

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

data class SimCardInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String
)

sealed class DualSimResult {
    data class Success(val simCards: List<SimCardInfo>) : DualSimResult()
    data class PermissionDenied(val message: String) : DualSimResult()
    data class Unavailable(val message: String) : DualSimResult()
}

object DualSimManager {

    /**
     * Retrieves the list of active SIM cards safely.
     * Guaranteed never to crash across Android 12, 13, 14, and 15.
     */
    @androidx.annotation.RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getActiveSimCards(context: Context): List<SimCardInfo> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }
        val result = getActiveSimCardsDetailed(context)
        return when (result) {
            is DualSimResult.Success -> result.simCards
            is DualSimResult.PermissionDenied -> emptyList()
            is DualSimResult.Unavailable -> emptyList()
        }
    }

    /**
     * Detailed query method returning a [DualSimResult] with explicit status messages.
     */
    @androidx.annotation.RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getActiveSimCardsDetailed(context: Context): DualSimResult {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return DualSimResult.PermissionDenied(
                SimPermissionManager.getPermissionStatusMessage(context)
            )
        }

        return try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            if (subscriptionManager == null) {
                return DualSimResult.Unavailable("Telephony Subscription service is unavailable on this device.")
            }

            val activeList: List<SubscriptionInfo>? = subscriptionManager.activeSubscriptionInfoList
            if (activeList.isNullOrEmpty()) {
                return DualSimResult.Success(emptyList())
            }

            val simCards = activeList.map { info ->
                SimCardInfo(
                    subscriptionId = info.subscriptionId,
                    slotIndex = info.simSlotIndex,
                    displayName = info.displayName?.toString()?.takeIf { it.isNotBlank() }
                        ?: "SIM ${info.simSlotIndex + 1}",
                    carrierName = info.carrierName?.toString()?.takeIf { it.isNotBlank() }
                        ?: "Cellular"
                )
            }
            DualSimResult.Success(simCards)
        } catch (e: SecurityException) {
            DualSimResult.PermissionDenied("Permission denied when querying SIM cards: ${e.message ?: "SecurityException"}")
        } catch (e: Exception) {
            DualSimResult.Unavailable("Unable to retrieve SIM information: ${e.message ?: "Unknown error"}")
        }
    }

    @androidx.annotation.RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getSimStatusUserMessage(context: Context): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return SimPermissionManager.getPermissionStatusMessage(context)
        }
        return when (val result = getActiveSimCardsDetailed(context)) {
            is DualSimResult.Success -> {
                if (result.simCards.isEmpty()) {
                    "No active SIM cards detected on this device."
                } else {
                    "${result.simCards.size} SIM card(s) active."
                }
            }
            is DualSimResult.PermissionDenied -> result.message
            is DualSimResult.Unavailable -> result.message
        }
    }

    fun getSmsManagerForSubId(context: Context, subId: Int): SmsManager {
        return getSmsManagerForSubscription(context, subId)
    }

    fun getSmsManagerForSubscription(context: Context, subId: Int): SmsManager {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val base = context.getSystemService(SmsManager::class.java)
                if (subId >= 0 && base != null) {
                    base.createForSubscriptionId(subId)
                } else {
                    base ?: @Suppress("DEPRECATION") SmsManager.getDefault()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (subId >= 0) {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }
}
