package com.global.sms.core.device

import android.os.Build
import java.util.Locale

enum class OemManufacturer {
    XIAOMI,
    SAMSUNG,
    GOOGLE_PIXEL,
    ONEPLUS,
    GENERIC
}

data class DeviceOptimizationProfile(
    val manufacturer: OemManufacturer,
    val brandName: String,
    val osVersion: String,
    val requiresAutoStartGuide: Boolean,
    val requiresBatteryExemptionGuide: Boolean,
    val persianGuideTitle: String,
    val persianInstructions: List<String>
)

class DeviceCompatibilityManager {

    fun detectDeviceProfile(): DeviceOptimizationProfile {
        val manufacturerStr = Build.MANUFACTURER.lowercase(Locale.ROOT)
        val brandStr = Build.BRAND.lowercase(Locale.ROOT)

        val oem = when {
            manufacturerStr.contains("xiaomi") || brandStr.contains("poco") || brandStr.contains("redmi") -> OemManufacturer.XIAOMI
            manufacturerStr.contains("samsung") -> OemManufacturer.SAMSUNG
            manufacturerStr.contains("google") -> OemManufacturer.GOOGLE_PIXEL
            manufacturerStr.contains("oneplus") -> OemManufacturer.ONEPLUS
            else -> OemManufacturer.GENERIC
        }

        return when (oem) {
            OemManufacturer.XIAOMI -> DeviceOptimizationProfile(
                manufacturer = OemManufacturer.XIAOMI,
                brandName = "Xiaomi / Poco / MIUI / HyperOS",
                osVersion = "Android ${Build.VERSION.RELEASE}",
                requiresAutoStartGuide = true,
                requiresBatteryExemptionGuide = true,
                persianGuideTitle = "راهنمای بهینه‌سازی شیائومی و پوکو (MIUI / HyperOS)",
                persianInstructions = listOf(
                    "۱. وارد تنظیمات دستگاه (Settings) شوید.",
                    "۲. بخش برنامه (Apps) -> مدیریت برنامه‌ها (Manage Apps) را باز کنید.",
                    "۳. برنامه Global SMS را پیدا کنید.",
                    "۴. گزینه Autostart (شروع خودکار) را فعال کنید.",
                    "۵. در بخش Battery saver، حالت No restrictions را انتخاب کنید."
                )
            )
            OemManufacturer.SAMSUNG -> DeviceOptimizationProfile(
                manufacturer = OemManufacturer.SAMSUNG,
                brandName = "Samsung One UI",
                osVersion = "Android ${Build.VERSION.RELEASE}",
                requiresAutoStartGuide = false,
                requiresBatteryExemptionGuide = true,
                persianGuideTitle = "راهنمای بهینه‌سازی سامسونگ (One UI)",
                persianInstructions = listOf(
                    "۱. وارد تنظیمات (Settings) -> مراقبت از دستگاه (Device Care) شوید.",
                    "۲. به بخش باتری (Battery) -> محدودیت‌های پس‌زمینه بروید.",
                    "۳. برنامه Global SMS را به لیست Never sleeping apps اضافه کنید."
                )
            )
            OemManufacturer.ONEPLUS -> DeviceOptimizationProfile(
                manufacturer = OemManufacturer.ONEPLUS,
                brandName = "OnePlus OxygenOS",
                osVersion = "Android ${Build.VERSION.RELEASE}",
                requiresAutoStartGuide = true,
                requiresBatteryExemptionGuide = true,
                persianGuideTitle = "راهنمای بهینه‌سازی وان‌پلاس (OxygenOS)",
                persianInstructions = listOf(
                    "۱. وارد تنظیمات (Settings) -> باتری (Battery) شوید.",
                    "۲. به بخش بهینه‌سازی باتری (Battery Optimization) بروید.",
                    "۳. برنامه Global SMS را روی حالت Don't Optimize تنظیم کنید."
                )
            )
            else -> DeviceOptimizationProfile(
                manufacturer = oem,
                brandName = Build.MANUFACTURER.uppercase(Locale.ROOT),
                osVersion = "Android ${Build.VERSION.RELEASE}",
                requiresAutoStartGuide = false,
                requiresBatteryExemptionGuide = true,
                persianGuideTitle = "راهنمای عمومی بهینه‌سازی اندروید",
                persianInstructions = listOf(
                    "۱. وارد تنظیمات دستگاه و بخش برنامه Global SMS شوید.",
                    "۲. گزینه بهینه‌سازی باتری را روی Unrestricted یا Not Optimized تنظیم کنید."
                )
            )
        }
    }
}
