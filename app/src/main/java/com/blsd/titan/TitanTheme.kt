package com.blsd.titan

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// TITÁN visual master — dark graphite + electric cyan/blue.
val TitanBackground = Color(0xFF071016)
val TitanSurface = Color(0xFF0D171E)
val TitanCard = Color(0xFF141F27)
val TitanCardRaised = Color(0xFF19252E)
val TitanPrimary = Color(0xFF38BDF2)
val TitanSecondary = Color(0xFF7DDCFF)
val TitanSuccess = Color(0xFF69E5C2)
val TitanWarning = Color(0xFFF0B95B)
val TitanAlert = Color(0xFFEF6B6B)
val TitanText = Color(0xFFF7F9FA)
val TitanTextSecondary = Color(0xFF9BA8B2)
val TitanDivider = Color(0xFF2B3943)
val TitanStroke = Color(0xFF33444F)
val TitanMuted = Color(0xFF6E7D87)

object TitanDimens {
 val ScreenHorizontal = 20.dp
 val ScreenTop = 18.dp
 val Section = 20.dp
 val ItemGap = 10.dp
 val CardRadius = 12.dp
 val ButtonRadius = 18.dp
 val CardPadding = 14.dp
 val BottomBarHeight = 58.dp
}

val TitanTypography = Typography(
 displayLarge=TextStyle(fontSize=34.sp,lineHeight=38.sp,fontWeight=FontWeight.Bold),
 headlineLarge=TextStyle(fontSize=27.sp,lineHeight=31.sp,fontWeight=FontWeight.Bold),
 headlineMedium=TextStyle(fontSize=22.sp,lineHeight=26.sp,fontWeight=FontWeight.Bold),
 titleLarge=TextStyle(fontSize=18.sp,lineHeight=22.sp,fontWeight=FontWeight.SemiBold),
 titleMedium=TextStyle(fontSize=15.sp,lineHeight=19.sp,fontWeight=FontWeight.SemiBold),
 bodyLarge=TextStyle(fontSize=15.sp,lineHeight=20.sp,fontWeight=FontWeight.Normal),
 bodyMedium=TextStyle(fontSize=13.sp,lineHeight=18.sp,fontWeight=FontWeight.Normal),
 bodySmall=TextStyle(fontSize=11.sp,lineHeight=15.sp,fontWeight=FontWeight.Normal),
 labelLarge=TextStyle(fontSize=13.sp,lineHeight=16.sp,fontWeight=FontWeight.SemiBold),
 labelMedium=TextStyle(fontSize=11.sp,lineHeight=14.sp,fontWeight=FontWeight.SemiBold)
)

private val TitanColors = darkColorScheme(
 primary=TitanPrimary,
 secondary=TitanSecondary,
 tertiary=TitanSuccess,
 background=TitanBackground,
 surface=TitanSurface,
 surfaceVariant=TitanCard,
 outline=TitanStroke,
 outlineVariant=TitanDivider,
 onPrimary=Color(0xFF061016),
 onSecondary=Color(0xFF061016),
 onBackground=TitanText,
 onSurface=TitanText,
 onSurfaceVariant=TitanTextSecondary,
 error=TitanAlert
)

@Composable fun TitanTheme(content: @Composable () -> Unit) {
 MaterialTheme(colorScheme=TitanColors,typography=TitanTypography,content=content)
}
