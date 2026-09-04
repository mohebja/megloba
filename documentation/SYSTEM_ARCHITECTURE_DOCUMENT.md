# Global SMS — System Architecture Document

## 1. Architectural Style
Global SMS uses Clean Architecture with MVVM and Unidirectional Data Flow (UDF).

```
+-------------------------------------------------------+
|                       :app                            |
+-------------------------------------------------------+
        |                  |                   |
        v                  v                   v
+---------------+  +---------------+  +---------------+
|     :ui       |  |  :sms-engine  |  |   :security   |
+---------------+  +---------------+  +---------------+
        |                  |                   |
        +------------------+-------------------+
                           |
                           v
                  +-----------------+
                  |    :database    |
                  +-----------------+
                           |
                           v
                  +-----------------+
                  |      :core      |
                  +-----------------+
```

## 2. Data Persistence Strategy
- Room Database (`SmsDatabase`) with WAL journal mode.
- AES-256-GCM encryption for Private Vault items using Android Keystore.
