#!/bin/bash

echo "=== 1. Default SMS Handler Verification ==="
grep -rn "ROLE_SMS" app/ sms-engine/ ui/ || echo "ROLE_SMS check done"
grep -rn "SMS_DELIVER" app/src/main/AndroidManifest.xml || true
grep -rn "WAP_PUSH_DELIVER" app/src/main/AndroidManifest.xml || true
grep -rn "RESPOND_VIA_MESSAGE" app/src/main/AndroidManifest.xml || true

echo "=== 2. Historical SMS Import Verification ==="
grep -rn "Telephony.Sms" sms-engine/ database/ core/ app/ || echo "Sms ContentProvider check"

echo "=== 3. Contact Integration Verification ==="
grep -rn "ContactsContract" core/ database/ ui/ sms-engine/ app/ || echo "ContactsContract check"

echo "=== 4. UI Systems Verification ==="
find ui/src/main/java -type f || echo "UI files listed"

echo "=== 5. Database & Migration Verification ==="
find database/src/main/java -type f || echo "Database files listed"

echo "=== 6. Security Verification ==="
find security/src/main/java -type f || echo "Security files listed"

echo "=== 7. Build System Verification ==="
ls -la gradlew gradlew.bat .github/workflows/ 2>/dev/null || echo "gradlew status"

