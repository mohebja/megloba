# 📢 Global SMS - Release Notes v8.0.0 (یادداشت‌های انتشار نسخه ۸.۰.۰)

---

### 🇮🇷 نسخه ۸.۰.۰ (انتشار پایدار در گوگل پلی و بازار)
خوش آمدید به نسخه ۸.۰.۰ برنامه **Global SMS** - اکوسیستم جامع، فوق‌العاده سریع و امن مدیریت پیامک‌ها با پشتیبانی بی‌نظیر از زبان فارسی، پردازش هوش مصنوعی درون‌دستگاهی و ابزارهای سازمانی.

#### ✨ مهم‌ترین دستاوردها و قابلیت‌های نسخه ۸.۰.۰:
1. **داشبورد هوش مالی و استخراج اکسل/CSV:**
   - تشخیص و پردازش خودکار پیامک‌های تمامی بانک‌های کشور (ملت، ملی، سپه، پاسارگاد، سامان و...).
   - محاسبه خودکار مانده‌حساب پس از تراکنش (`balanceAfter`)، واریزی‌ها و هزینه‌ها.
   - امکان خروجی فایل اکسل و CSV سازگار با فرمت UTF-8 BOM بدون به‌هم‌ریختگی حروف فارسی.
2. **سطل بازیافت هوشمند (Recycle Bin):**
   - پیشگیری از حذف ناخواسته پیام‌ها با نگهداری امن ۳۰ روزه در سطل زباله.
   - بازیابی سریع پیامک‌ها با یک لمس یا پاکسازی دائمی امن.
3. **موتور هوش مصنوعی بدون نیاز به اینترنت (100% Offline AI):**
   - دسته‌بندی خودکار در دسته‌های *بانکی*، *رمز پویا/OTP*، *شخصی*، *اطلاع‌رسانی* و *اسپم*.
   - استخراج هوشمند کدهای پیگیری مرسولات پستی و مبالغ ریالی.
   - پیشنهاد هوشمند پاسخ‌های سریع متناسب با لحن پیام.
4. **صندوقچه خصوصی با ایزولاسیون سخت‌افزاری (Private Vault):**
   - رمزنگاری ۲۵۶ بیتی پیشرفته AES-GCM تحت پشتیبانی Android KeyStore و ماژول StrongBox.
   - بازگشایی امن با اثر انگشت یا رمز عبور رمزنگاری‌شده با الگوریتم PBKDF2 (۲۱,۰۰۰ دور هشینگ).
5. **نسخه پشتیبان سازمانی رمزگذاری‌شده (GSMS Encrypted Backup):**
   - فرمت اختصاصی پشتیبان‌گیری رمزنگاری‌شده بدون هیچ‌گونه امکان دسترسی غیرمجاز.
   - پشتیبان‌گیری خودکار دوره‌ای در پس‌زمینه بدون مصرف غیرعادی باتری.
6. **پایداری حداکثری و قبولی ۱۰۰٪ در تست‌های کیفی:**
   - قبولی در تمامی ۲۲۷ آزمون خودکار نرم‌افزاری و رگرسیون سراسری.
   - سازگاری کامل با اندروید ۱۵ و ۱۶ (Target SDK 36).

---

### 🇬🇧 Version 8.0.0 (Stable Release)
Welcome to version 8.0.0 of **Global SMS** — The intelligent, high-speed, and secure SMS messaging platform engineered with Jetpack Compose and modern Android architecture.

#### ✨ Core Highlights & Features:
1. **Financial Intelligence Dashboard & Excel/CSV Export:**
   - Automatic recognition and parsing of banking transaction messages with balance calculation.
   - Single-tap export to Microsoft Excel-compatible CSV with UTF-8 BOM encoding.
2. **Smart Recycle Bin (Trash System):**
   - 30-day safe retention for deleted messages with instant one-tap restoration.
3. **100% On-Device AI Classification & Copilot:**
   - Zero-latency local categorization (Banking, OTP, Personal, Spam, Automation).
   - Automated extraction of tracking codes, amounts, and dates.
4. **Hardware-Backed Private Vault:**
   - Secure message isolation utilizing AES-256-GCM via Android KeyStore / TEE StrongBox.
   - Protected by Biometric Authentication and PBKDF2 password derivation.
5. **Encrypted Enterprise Backup (GSMS Container):**
   - Cryptographically authenticated backup format with background auto-backup scheduling.
6. **Zero-Defect Quality Assurance:**
   - 100% pass rate across all 227 JVM unit and Robolectric test suites.
   - Fully optimized for Android 15 & 16 (Target SDK 36).
