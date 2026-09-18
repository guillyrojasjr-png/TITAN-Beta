package com.blsd.titan

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TitanBackground = Color(0xFF0B0F14)
val TitanSurface = Color(0xFF121922)
val TitanCard = Color(0xFF1A2430)
val TitanPrimary = Color(0xFF2ED4B7)
val TitanSecondary = Color(0xFF57C7E6)
val TitanSuccess = Color(0xFF4CAF7A)
val TitanWarning = Color(0xFFF4B860)
val TitanAlert = Color(0xFFEF6B6B)
val TitanText = Color(0xFFFFFFFF)
val TitanTextSecondary = Color(0xFF9AA4AE)
val TitanDivider = Color(0xFF2A3642)

private val TitanColors = darkColorScheme(
 primary=TitanPrimary, secondary=TitanSecondary, background=TitanBackground,
 surface=TitanSurface, surfaceVariant=TitanCard, onPrimary=TitanBackground,
 onBackground=TitanText, onSurface=TitanText, onSurfaceVariant=TitanTextSecondary,
 error=TitanAlert
)

@Composable fun TitanTheme(content:@Composable()->Unit) {
 MaterialTheme(colorScheme=TitanColors, typography=Typography(), content=content)
}
