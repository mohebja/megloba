# چرخه و ساختار نقش‌های پروژه (Role Framework & Multi-Agent Matrix)

این سند تعیین‌کننده چارچوب چندنقشی (Multi-Disciplinary / Virtual Sub-Agents) برای تمام مراحل توسعه، تست، نگهداری و انتشار برنامه **Global SMS** است. تمامی تصمیمات و توسعه‌های فنی، معماری و محصولی با در نظر گرفتن ابعاد تخصصی زیر صورت می‌پذیرد:

---

## ۱. لایه رهبری محصول و تحلیل استراتژیک (Product & Business Strategy)
- **Product Manager (PM) & Product Owner (PO):** مدیریت اولویت‌ها، نقشه راه (Roadmap)، تعریف ارزش تجاری، مدیریت نیازمندی‌ها و Backlog، و هم‌راستایی نیازمندی‌های کاربر با خروجی فنی.
- **Business Analyst (BA) & System Analyst:** تحلیل نیازمندی‌های کسب‌وکار، مستندسازی رفتار سیستم، مدل‌سازی جریان داده (Data Flow) و فرآیندهای کاربردی، تحلیل سناریوهای Edge-Case و تبدیل اهداف کلان به مشخصات فنی دقیق.
- **Product Analyst & Data Analyst / Data Scientist:** تحلیل رفتار کاربر، شاخص‌های کلیدی عملکرد (KPIs)، الگوهای استفاده از پیام‌رسان و دسته‌بندی هوشمند، تحلیل متریک‌های عملکردی و پایش سلامت تعاملات.
- **Product Operations Manager & Product Marketing Manager:** نظارت بر فرآیندهای عملیاتی محصول، استراتژی رونمایی و معرفی ارزش‌های رقابتی، تحلیل بازخوردها و آماده‌سازی برای ورود به بازار.

---

## ۲. لایه طراحی تجربه، رابط کاربری و تعامل (Design & Research)
- **UX Researcher & Product Designer:** مطالعه رفتار کاربر، درک چالش‌های تعاملی پیام‌رسانی پیشرفته، تحلیل دسترسی‌پذیری و جریان‌های کاربری (User Journeys).
- **UI Designer & Interaction Designer:** طراحی المان‌های مدرن، متریال دیزاین ۳ (M3)، مدیریت ریزتعامل‌ها (Micro-interactions)، انیمیشن‌های نرم، حالت تاریک/روشن و واکنش‌گرایی در انواع نمایشگرها (تلفن، تبلت، دستگاه‌های تاشو).
- **Design System Designer:** پیاده‌سازی سیستم یکپارچه طراحی (رنگ‌ها، تایپوگرافی، فاصله‌ها، کامپوننت‌های قابل‌استفاده مجدد در Jetpack Compose).

---

## ۳. لایه مهندسی و معماری نرم‌افزار (Engineering & Architecture)
- **Technical Lead & Engineering Manager:** هدایت استانداردهای فنی، مدیریت پیچیدگی کد، هدایت فرآیند مهندسی، بررسی انسجام ماژول‌ها و بهره‌وری کلی تیم.
- **Software Architect & Security Architect:** طراحی معماری تمیز (Clean Architecture / MVI-MVVM)، استقلال ماژول‌ها (`:app`, `:core`, `:database`, `:security`, `:sms-engine`, `:settings`, `:ui`)، مدل تهدید (Threat Modeling)، رمزنگاری انتها به انتها و امنیت در حالت سکون و انتقال.
- **Android Developer / Frontend / Full-Stack:** توسعه عمیق اندروید با کاتلین و Jetpack Compose مدرن، مدیریت Lifecycle، StateFlow/Coroutines، کامپوننت‌های Edge-to-Edge و تعامل با APIهای بومی اندروید (Telephony, SMS/MMS, WorkManager).
- **Database Developer & Database Administrator (DBA):** معماری پایگاه داده Room و SQLCipher، ایندکس‌گذاری بهینه، مهاجرت‌های ساختاریافته (Migrations)، جستجوی متنی پیشرفته (FTS4/FTS5)، مدیریت کش و پیشگیری از قفل دیتابیس در عملیات سنگین.
- **AI & Machine Learning Engineer:** سیستم‌های هوشمند دسته‌بندی پیام‌ها (OTP، مالی، شخصی، تبلیغاتی)، شناسایی خودکار فیشینگ و اسپم، پردازش درون‌دستگاهی بدون نقض حریم خصوصی.
- **Integration Developer:** یکپارچه‌سازی ماژول‌های سیستم، وب‌هوک‌ها، خروجی و پشتیبان‌گیری چندگانه، اتصال امن به سرویس‌های هوش مصنوعی و احراز هویت.

---

## ۴. لایه کیفیت، آزمون و اعتبارسنجی (Quality Assurance & Testing)
- **QA Manager & QA Engineer:** استراتژی آزمون جامع، پوشش آزمون‌های عملکردی و غیرعملکردی، مدیریت سناریوهای آزمون سراسری.
- **QA Automation & Software Tester:** توسعه تست‌های خودکار واحد (Unit Tests) و تست‌های ابعادی JVM/Robolectric برای جلوگیری از Regression.
- **Performance Test Engineer:** پایش مصرف حافظه (Memory Leaks)، نرخ فریم (UI Jank / 60-120fps)، مصرف باتری در کارهای پس‌زمینه و سرعت بارگذاری پایگاه داده.
- **Security Test Engineer & Penetration Tester / AppSec:** ارزیابی نفوذپذیری، راستی‌آزمایی تفکیک کلیدهای Keystore، بررسی نشت داده در لاگ‌ها یا پشتیبان‌گیری، امنیت حافظه موقت و کلیپ‌بورد.
- **Compatibility & User Acceptance Tester (UAT):** تطابق برنامه در تبلت‌ها، تاشوها، زبان‌های راست‌به‌چپ (فارسی/عربی) و نسخه‌های گوناگون اندروید (API 24 تا بالاترین نسخه).

---

## ۵. لایه زیرساخت، امنیت عملیاتی و استقرار (DevSecOps, SRE & Release)
- **DevOps & DevSecOps Engineer:** بهینه‌سازی فرآیند ساخت و کامپایل گریدل، بررسی امنیتی وابستگی‌ها، اعمال قوانین ایزوله‌سازی.
- **Site Reliability Engineer (SRE) & Infrastructure:** پایداری تسک‌های پس‌زمینه (Background Reliability)، مدیریت استثناها، گزارش‌گیری امن خطاها و پیشگیری از Crash.
- **Release Manager & Release Engineer:** آماده‌سازی بیلدها، بررسی انطباق با خط‌مشی‌های گوگل پلی (Google Play Developer Policies)، مدیریت مجوزهای حساس (SMS/Call Log) و حفظ امضای دیجیتال (Signing).
- **Configuration & Version Control Manager:** مدیریت نسخه‌گذاری سمانتیک (SemVer)، کنترل تداخلات وابستگی‌ها در `libs.versions.toml` و یکپارچگی فایل‌های پیکربندی.

---

## ۶. لایه ارتباط با کاربر، مستندسازی و پشتیبانی (Operations & Support)
- **Technical Writer:** مستندسازی فنی معماری، راهنماهای کاربری، توضیحات شفاف در مورد رمزنگاری و خط‌مشی حفظ حریم خصوصی.
- **Technical Support Specialist & Customer Success Manager:** پاسخگویی به چالش‌های کاربران نهایی، تحلیل بازخوردها و رفع باگ‌های پرتکرار گزارش‌شده.
- **Community Manager:** تعامل با کاربران، انتشار اخبار قابلیت‌های جدید و گردآوری نیازهای آینده.
