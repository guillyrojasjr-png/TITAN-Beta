package com.blsd.titan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cream = Color(0xFFF7F5EF)
private val Ink = Color(0xFF171717)
private val Accent = Color(0xFF8EC5FF)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { TitanApp() }
 }
}

@Composable fun TitanApp() {
 var started by remember { mutableStateOf(false) }
 MaterialTheme(colorScheme = lightColorScheme(primary = Ink, background = Cream, surface = Color.White)) {
  Surface(Modifier.fillMaxSize(), color = Cream) {
   if (!started) Welcome { started = true } else Today()
  }
 }
}

@Composable private fun Welcome(onStart: () -> Unit) {
 Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.SpaceBetween) {
  Column(Modifier.padding(top = 72.dp)) {
   Text("TITÁN", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Ink)
   Spacer(Modifier.height(10.dp))
   Text("Nutrición que se adapta a tu vida.", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
   Spacer(Modifier.height(14.dp))
   Text("Tu objetivo, tus comidas y tu entrenamiento. TITÁN ajusta el camino sin convertirlo en una cárcel.", fontSize = 16.sp, lineHeight = 23.sp, color = Color.DarkGray)
  }
  Button(onClick=onStart, modifier=Modifier.fillMaxWidth().height(58.dp), shape=RoundedCornerShape(18.dp)) { Text("COMENZAR") }
 }
}

@Composable private fun Today() {
 Column(Modifier.fillMaxSize().padding(24.dp)) {
  Spacer(Modifier.height(34.dp))
  Text("HOY", fontSize=13.sp, fontWeight=FontWeight.Bold, color=Color.Gray)
  Text("Tu día en TITÁN", fontSize=30.sp, fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(24.dp))
  Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp)) {
   Column(Modifier.padding(22.dp)) {
    Text("Objetivo diario", color=Color.Gray)
    Text("— kcal", fontSize=34.sp, fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    LinearProgressIndicator(progress={0f}, modifier=Modifier.fillMaxWidth().height(8.dp), color=Accent)
    Spacer(Modifier.height(10.dp))
    Text("La Beta 0.1 ya está viva. El motor nutricional se conectará a este dashboard.", fontSize=14.sp)
   }
  }
  Spacer(Modifier.height(18.dp))
  Button(onClick={}, modifier=Modifier.fillMaxWidth().height(54.dp), shape=RoundedCornerShape(16.dp)) { Text("＋ REGISTRAR COMIDA") }
 }
}
