#!/bin/bash

echo "--- CHECK 1: Default SMS Handler ---"
cat app/src/main/AndroidManifest.xml | grep -E "SMS_DELIVER|WAP_PUSH_DELIVER|RESPOND_VIA_MESSAGE|SENDTO" -A 2 -B 1

echo "--- CHECK 2: Historical SMS Import ---"
grep -rn "content://sms" sms-engine/ core/ database/ || grep -rn "Telephony.Sms" sms-engine/ core/ database/ || true

echo "--- CHECK 3: Contact Integration ---"
grep -rn "ContactManager" ui/ core/ || true

echo "--- CHECK 4: UI Systems ---"
ls -la ui/src/main/java/com/global/sms/ui/classic/
ls -la ui/src/main/java/com/global/sms/ui/smart/
ls -la ui/src/main/java/com/global/sms/ui/enterprise/

echo "--- CHECK 5: Database & Room Migrations ---"
grep -rn "Migration" database/ || true

echo "--- CHECK 6: Security ---"
grep -rn "KeyStore" security/ || true
grep -rn "Biometric" security/ || true

echo "--- CHECK 7: Build System ---"
ls -la .github/workflows/ci.yml || true
grep -rn "google-services" app/ build.gradle.kts settings.gradle.kts || true

