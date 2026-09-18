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

private val Cream = TitanBackground
private val Ink = TitanText
private val Accent = TitanPrimary

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent { TitanApp() }
 }
}

@Composable fun TitanApp() {
 var started by remember { mutableStateOf(false) }
 var meals by remember { mutableStateOf(TitanEngine.distribute(2500, listOf("Desayuno","Comida","Merienda","Cena"))) }
 var showAdd by remember { mutableStateOf(false) }
 TitanTheme {
  Surface(Modifier.fillMaxSize(), color = TitanBackground) {
   val plan=CaloriePlan(Strategy.MODERATE,2500,2750)
   val consumed=meals.sumOf { it.consumedKcal }
   if(!started) Welcome{started=true} else if(showAdd) AddMeal(onAdd={n,k-> val i=meals.indexOfFirst{it.status==MealStatus.PENDING}; meals=if(i>=0) meals.mapIndexed{x,m->if(x==i)m.copy(name=n,consumedKcal=k,status=MealStatus.CONFIRMED)else m}.let(TitanEngine::redistribute) else meals+MealSlot("extra-"+meals.size,n,0,k,MealStatus.CONFIRMED);showAdd=false},onBack={showAdd=false}) else Today(plan,TitanEngine.balance(plan,consumed),meals,{showAdd=true},{id->meals=meals.map{if(it.id==id)it.copy(status=MealStatus.SKIPPED,consumedKcal=0)else it}.let(TitanEngine::redistribute)})
  }
 }
}

@Composable private fun Welcome(onStart: () -> Unit) {
 Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.SpaceBetween) {
  Column(Modifier.padding(top = 72.dp)) {
   Text("TITÁN", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Ink)
   Spacer(Modifier.height(10.dp))
   Text("DATOS QUE TE LLEVAN MÁS LEJOS", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TitanTextSecondary)
   Spacer(Modifier.height(14.dp))
   Text("Tu objetivo, tus comidas y tu entrenamiento. TITÁN ajusta el camino sin convertirlo en una cárcel.", fontSize = 16.sp, lineHeight = 23.sp, color = TitanTextSecondary)
  }
  Button(onClick=onStart, modifier=Modifier.fillMaxWidth().height(58.dp), shape=RoundedCornerShape(18.dp)) { Text("COMENZAR") }
 }
}

@Composable private fun Today(plan:CaloriePlan,balance:DailyBalance,meals:List<MealSlot>,addMeal:()->Unit,skipMeal:(String)->Unit) {
 Column(Modifier.fillMaxSize().padding(24.dp)) {
  Spacer(Modifier.height(34.dp))
  Text("HOY",fontSize=13.sp,fontWeight=FontWeight.Bold,color=TitanTextSecondary)
  Text("Tu día en TITÁN",fontSize=30.sp,fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(24.dp))
  Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=TitanCard)){Column(Modifier.padding(22.dp)){
   Text("Objetivo diario · MODERADO",color=TitanTextSecondary)
   Text("${balance.target} kcal",fontSize=34.sp,fontWeight=FontWeight.Bold)
   Spacer(Modifier.height(12.dp))
   LinearProgressIndicator(progress={(balance.consumed/balance.target.toFloat()).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(8.dp),color=Accent)
   Spacer(Modifier.height(10.dp))
   Text("${balance.consumed} consumidas · ${balance.availableToTarget} disponibles",fontSize=14.sp)
   Text("Margen TITÁN hasta ${balance.toleranceCeiling} kcal",fontSize=13.sp,color=TitanTextSecondary)
   if(balance.excessOverTolerance>0) Text("Recalibración: ${balance.excessOverTolerance} kcal sobre el margen",fontWeight=FontWeight.Bold)
  }}
  Spacer(Modifier.height(18.dp))
  Button(onClick=addMeal,modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp)){Text("＋ REGISTRAR COMIDA")}
  Spacer(Modifier.height(18.dp))
  Text("COMIDAS DE HOY",fontSize=13.sp,fontWeight=FontWeight.Bold,color=TitanTextSecondary)
  meals.forEach{meal->Card(Modifier.fillMaxWidth().padding(vertical=4.dp),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=TitanSurface)){Column(Modifier.padding(12.dp)){
   Text(meal.name,fontWeight=FontWeight.SemiBold)
   Text(when(meal.status){MealStatus.PENDING->"Pendiente · objetivo ${meal.plannedKcal} kcal";MealStatus.CONFIRMED->"Confirmada · ${meal.consumedKcal} kcal";MealStatus.SKIPPED->"Omitida"},color=TitanTextSecondary)
   if(meal.status==MealStatus.PENDING) TextButton(onClick={skipMeal(meal.id)}){Text("OMITIR")}
  }}}
 }
}

@Composable private fun AddMeal(onAdd:(String,Int)->Unit,onBack:()->Unit) {
 var name by remember { mutableStateOf("") }
 var kcal by remember { mutableStateOf("") }
 Column(Modifier.fillMaxSize().padding(24.dp)) {
  Spacer(Modifier.height(34.dp))
  Text("REGISTRO RÁPIDO", fontSize=13.sp, fontWeight=FontWeight.Bold, color=TitanTextSecondary)
  Text("Añadir comida", fontSize=30.sp, fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(20.dp))
  OutlinedTextField(value=name,onValueChange={name=it},label={Text("Alimento o plato")},modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(12.dp))
  OutlinedTextField(value=kcal,onValueChange={kcal=it.filter(Char::isDigit)},label={Text("Calorías")},modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(18.dp))
  Button(onClick={val k=kcal.toIntOrNull();if(name.isNotBlank()&&k!=null&&k>0)onAdd(name,k)},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp)){Text("AÑADIR")}
  TextButton(onClick=onBack,modifier=Modifier.fillMaxWidth()){Text("CANCELAR")}
 }
}
