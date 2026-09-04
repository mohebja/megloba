package com.global.sms.core.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ApiKeyProfile(
    val keyId: String,
    val secretHash: String,
    val clientName: String,
    val allowedPermissions: List<String>,
    val maxRequestsPerMinute: Int = 60,
    val isActive: Boolean = true
)

data class ApiGatewayRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val apiKeyId: String,
    val endpoint: String,
    val httpMethod: String,
    val payloadJson: String,
    val signature: String,
    val clientIp: String = "127.0.0.1",
    val timestamp: Long = System.currentTimeMillis()
)

data class ApiGatewayResponse(
    val requestId: String,
    val statusCode: Int,
    val responseBody: String,
    val signature: String,
    val timestamp: Long = System.currentTimeMillis()
)

class InternalApiGateway {

    private val registeredApiKeys = mutableMapOf<String, ApiKeyProfile>()
    private val requestCounts = mutableMapOf<String, Int>()

    private val _accessLogs = MutableStateFlow<List<ApiGatewayRequest>>(emptyList())
    val accessLogs: StateFlow<List<ApiGatewayRequest>> = _accessLogs.asStateFlow()

    fun registerApiKey(profile: ApiKeyProfile) {
        registeredApiKeys[profile.keyId] = profile
    }

    fun handleRequest(request: ApiGatewayRequest): ApiGatewayResponse {
        _accessLogs.value = _accessLogs.value + request

        val profile = registeredApiKeys[request.apiKeyId]
            ?: return createErrorResponse(request.requestId, 401, "INVALID_API_KEY")

        if (!profile.isActive) {
            return createErrorResponse(request.requestId, 403, "API_KEY_REVOKED")
        }

        // Rate limiting check
        val currentCount = requestCounts.getOrDefault(request.apiKeyId, 0)
        if (currentCount >= profile.maxRequestsPerMinute) {
            return createErrorResponse(request.requestId, 429, "RATE_LIMIT_EXCEEDED")
        }
        requestCounts[request.apiKeyId] = currentCount + 1

        // Endpoint RBAC permission check
        val requiredPermission = determineRequiredPermission(request.endpoint, request.httpMethod)
        if (!profile.allowedPermissions.contains(requiredPermission) && !profile.allowedPermissions.contains("*")) {
            return createErrorResponse(request.requestId, 403, "PERMISSION_DENIED")
        }

        val successBody = when (request.endpoint) {
            "/api/v1/sms/send" -> """{"status":"QUEUED","messageId":"MSG-${UUID.randomUUID().toString().take(6)}"}"""
            "/api/v1/crm/contacts" -> """{"contacts":[{"id":"C1","name":"Enterprise Client"}]}"""
            "/api/v1/agent/status" -> """{"agentState":"ONLINE","activeWorkflows":3}"""
            else -> """{"status":"SUCCESS","data":"Payload processed"}"""
        }

        return ApiGatewayResponse(
            requestId = request.requestId,
            statusCode = 200,
            responseBody = successBody,
            signature = "HMAC_SHA256_SIG[${request.requestId}]"
        )
    }

    private fun determineRequiredPermission(endpoint: String, method: String): String {
        return when {
            endpoint.startsWith("/api/v1/sms/send") -> "SMS_SEND"
            endpoint.startsWith("/api/v1/crm") -> "CRM_ACCESS"
            endpoint.startsWith("/api/v1/agent") -> "AI_AGENT_ADMIN"
            else -> "GENERAL_READ"
        }
    }

    private fun createErrorResponse(requestId: String, status: Int, errorMsg: String): ApiGatewayResponse {
        return ApiGatewayResponse(
            requestId = requestId,
            statusCode = status,
            responseBody = """{"error":"$errorMsg"}""",
            signature = "HMAC_SHA256_ERR"
        )
    }
}
