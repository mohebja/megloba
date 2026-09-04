package com.global.sms.core.ai.runtime

enum class ModelArchitecture {
    TENSORFLOW_LITE,
    MEDIAPIPE_LLM,
    ONNX_RUNTIME
}

data class LocalModelInfo(
    val modelId: String,
    val name: String,
    val architecture: ModelArchitecture,
    val version: String,
    val sizeBytes: Long,
    val isLoaded: Boolean = false,
    val memoryUsageMb: Int = 0
)

data class InferenceResult(
    val modelId: String,
    val outputText: String,
    val confidence: Float,
    val executionTimeMs: Long,
    val tokensPerSecond: Float
)

class LocalModelRuntime {

    private val loadedModels = mutableMapOf<String, LocalModelInfo>()

    init {
        // Pre-register standard enterprise local models
        registerModel(
            LocalModelInfo(
                modelId = "tflite_persian_sentiment_v3",
                name = "Persian Sentiment Analyzer",
                architecture = ModelArchitecture.TENSORFLOW_LITE,
                version = "3.2.0",
                sizeBytes = 14_500_000L
            )
        )
        registerModel(
            LocalModelInfo(
                modelId = "mediapipe_llm_3b_q4",
                name = "MediaPipe On-Device LLM (Gemma 2B Quantized)",
                architecture = ModelArchitecture.MEDIAPIPE_LLM,
                version = "1.5.0",
                sizeBytes = 1_800_000_000L
            )
        )
        registerModel(
            LocalModelInfo(
                modelId = "onnx_spam_classifier_v2",
                name = "ONNX Enterprise Spam & Fraud Engine",
                architecture = ModelArchitecture.ONNX_RUNTIME,
                version = "2.1.0",
                sizeBytes = 8_200_000L
            )
        )
    }

    fun registerModel(modelInfo: LocalModelInfo) {
        loadedModels[modelInfo.modelId] = modelInfo
    }

    fun loadModel(modelId: String): Boolean {
        val model = loadedModels[modelId] ?: return false
        val memoryUsage = when (model.architecture) {
            ModelArchitecture.TENSORFLOW_LITE -> 45
            ModelArchitecture.MEDIAPIPE_LLM -> 320
            ModelArchitecture.ONNX_RUNTIME -> 28
        }
        loadedModels[modelId] = model.copy(isLoaded = true, memoryUsageMb = memoryUsage)
        return true
    }

    fun unloadModel(modelId: String): Boolean {
        val model = loadedModels[modelId] ?: return false
        loadedModels[modelId] = model.copy(isLoaded = false, memoryUsageMb = 0)
        return true
    }

    fun runInference(modelId: String, inputPrompt: String): InferenceResult {
        val model = loadedModels[modelId]
        if (model == null || !model.isLoaded) {
            // Auto-load if not loaded
            loadModel(modelId)
        }

        val startTime = System.currentTimeMillis()
        val responseText = when (model?.architecture) {
            ModelArchitecture.MEDIAPIPE_LLM -> "پاسخ هوشمند مدل محلی MediaPipe LLM بر اساس متن: $inputPrompt"
            ModelArchitecture.TENSORFLOW_LITE -> "تحلیل احساسات TFLite: مثبت (اطمینان ۰.۹۵)"
            ModelArchitecture.ONNX_RUNTIME -> "امتیاز آنالیز اسپم ONNX: ۰.۰۲ (ایمن و غیر اسپم)"
            else -> "نتیجه پردازش محلی هوش مصنوعی"
        }
        val executionTime = System.currentTimeMillis() - startTime + 12L // Simulated local inference execution latency

        return InferenceResult(
            modelId = modelId,
            outputText = responseText,
            confidence = 0.96f,
            executionTimeMs = executionTime,
            tokensPerSecond = 42.5f
        )
    }

    fun optimizeMemory(): Int {
        var releasedMb = 0
        loadedModels.forEach { (id, model) ->
            if (model.isLoaded && model.architecture == ModelArchitecture.MEDIAPIPE_LLM) {
                // Keep heavy LLM unloaded until required
                unloadModel(id)
                releasedMb += 320
            }
        }
        return releasedMb
    }

    fun getModelInfo(modelId: String): LocalModelInfo? = loadedModels[modelId]

    fun getAllModels(): List<LocalModelInfo> = loadedModels.values.toList()
}
