package com.global.sms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.global.sms.core.contact.ContactCacheManager
import com.global.sms.core.contact.ContactRepositoryImpl
import com.global.sms.core.util.PersianUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ResolvedContact(
    val name: String?,
    val photoUri: String?
)

@Composable
fun rememberContactInfo(phoneNumber: String): ResolvedContact {
    val context = LocalContext.current
    val cacheManager = remember { ContactCacheManager.getInstance() }
    
    // Check O(1) in-memory cache first to avoid unneeded coroutine execution
    val fastCached = remember(phoneNumber) {
        val cachedContact = cacheManager.lookupContact(phoneNumber)
        if (cachedContact != null) {
            ResolvedContact(cachedContact.name, cachedContact.photoUri)
        } else null
    }

    var contactState by remember(phoneNumber) { mutableStateOf(fastCached ?: ResolvedContact(null, null)) }

    if (fastCached == null) {
        LaunchedEffect(phoneNumber) {
            if (phoneNumber.isBlank()) return@LaunchedEffect
            val repository = ContactRepositoryImpl(context)
            val resolved = repository.resolveContact(phoneNumber)
            if (resolved != null) {
                contactState = ResolvedContact(resolved.name, resolved.photoUri)
            }
        }
    }

    return contactState
}

@Composable
fun ContactAvatar(
    photoUri: String?,
    displayName: String,
    size: Dp = 40.dp,
    fallbackColor: Color = MaterialTheme.colorScheme.primaryContainer,
    fallbackTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    if (!photoUri.isNullOrEmpty()) {
        AsyncImage(
            model = photoUri,
            contentDescription = displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        val initial = displayName.trim().take(1).uppercase()
        val bgHexColor = remember(displayName) {
            val colors = listOf(
                Color(0xFF1A73E8), Color(0xFF00658F), Color(0xFF70538C),
                Color(0xFF006A6A), Color(0xFF2E7D32), Color(0xFFC2185B), Color(0xFFE65100)
            )
            val index = kotlin.math.abs(displayName.hashCode()) % colors.size
            colors[index]
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgHexColor),
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNotBlank()) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = (size.value * 0.45f).sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}
