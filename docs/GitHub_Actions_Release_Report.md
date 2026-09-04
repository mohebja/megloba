# GitHub Actions Release Pipeline Report

## 1. Workflow Architecture
The CI/CD pipeline is orchestrated via `.github/workflows/release.yml` for production releases and `.github/workflows/ci.yml` for regular branch verification.

## 2. Release Workflow Stages
1. **Source Checkout:** Fetches tagged commit or branch trigger.
2. **JDK 17 Environment:** Configures Eclipse Temurin JDK 17 with Gradle dependency caching.
3. **Secret Validation:** Fails immediately if `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, or `KEY_PASSWORD` are missing.
4. **Zero-Dummy Security Check:** Scans the workspace to ensure zero placeholder configurations exist.
5. **Quality Gate:** Executes `testDebugUnitTest` and `lint`.
6. **Production Artifact Build:** Generates release APK and AAB with environment-injected signing credentials.
7. **SHA-256 Checksum Generation:** Outputs deterministic cryptographic hashes for all generated release binaries.
8. **Artifact Upload:** Uploads binaries to GitHub Actions artifact storage for release distribution.
9. **Keystore Cleanup:** Automatically removes decoded keystore files from the runner disk in an `always()` cleanup step.

## 3. GitHub Secrets Configuration Guide

| Secret Name | Description | Sensitivity |
|---|---|---|
| `KEYSTORE_BASE64` | Base64-encoded release `.jks` / `.keystore` | High (Masked) |
| `KEYSTORE_PASSWORD` | Keystore password | High (Masked) |
| `KEY_ALIAS` | Private key alias name | Medium (Masked) |
| `KEY_PASSWORD` | Key alias password | High (Masked) |

## 4. Verdict
* **Pipeline Status:** **READY / HARDENED**
