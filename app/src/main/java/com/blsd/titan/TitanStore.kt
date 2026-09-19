package com.blsd.titan

import android.content.Context
import java.time.LocalDate

data class TitanSavedState(val configured:Boolean,val sex:Sex,val age:Int,val heightCm:Double,val weightKg:Double,val maintenance:Int,val strategy:Strategy,val mealCount:Int)
data class SessionDay(val date:String,val meals:List<MealSlot>,val createdNew:Boolean)
data class BodyEntry(val date:String,val weightKg:Double,val waistCm:Double?)
data class TargetChange(val date:String,val previousTarget:Int,val newTarget:Int,val reason:String)
data class DayRecord(val date:String,val target:Int,val tolerance:Int,val consumed:Int,val excess:Int,val confirmed:Int,val skipped:Int,val closed:Boolean)

class TitanStore(context:Context){
 private val p=context.getSharedPreferences("titan_beta",Context.MODE_PRIVATE)
 fun load():TitanSavedState?{
  if(!p.getBoolean("configured",false))return null
  val maintenance=p.getInt("maintenance",0)
  val age=p.getInt("age",30)
  val height=p.getFloat("height",170f).toDouble()
  val weight=p.getFloat("weight",70f).toDouble()
  // Beta migrations / corrupt or incomplete legacy state: restart onboarding safely.
  if(maintenance<=0 || age<=0 || height<=0.0 || weight<=0.0){
   p.edit().putBoolean("configured",false).remove("active_target").apply()
   return null
  }
  return TitanSavedState(true,runCatching{Sex.valueOf(p.getString("sex","MALE")!!)}.getOrDefault(Sex.MALE),age,height,weight,maintenance,runCatching{Strategy.valueOf(p.getString("strategy","MODERATE")!!)}.getOrDefault(Strategy.MODERATE),p.getInt("meal_count",4).coerceIn(2,6))
 }
 fun save(profile:UserProfile,maintenance:Int,strategy:Strategy,mealCount:Int=4){p.edit().putBoolean("configured",true).putString("sex",profile.sex.name).putInt("age",profile.age).putFloat("height",profile.heightCm.toFloat()).putFloat("weight",profile.weightKg.toFloat()).putInt("maintenance",maintenance).putString("strategy",strategy.name).putInt("meal_count",mealCount.coerceIn(2,6)).apply()}
 private fun dayKey(date:String)= "day_"+date
 fun saveDay(meals:List<MealSlot>,plan:CaloriePlan,date:String=LocalDate.now().toString(),closed:Boolean=false){
  val encoded=meals.joinToString("~"){listOf(it.id,it.name.replace("|"," "),it.plannedKcal,it.consumedKcal,it.status.name,it.proteinG,it.carbsG,it.fatG).joinToString("|")}
  val r=TitanEngine.closeDay(plan,meals)
  p.edit().putString(dayKey(date)+"_meals",encoded).putInt(dayKey(date)+"_target",plan.target).putInt(dayKey(date)+"_tolerance",plan.toleranceCeiling).putInt(dayKey(date)+"_consumed",r.consumed).putInt(dayKey(date)+"_excess",r.excessToRecalibrate).putInt(dayKey(date)+"_confirmed",r.completedMeals).putInt(dayKey(date)+"_skipped",r.skippedMeals).putBoolean(dayKey(date)+"_closed",closed).apply()
 }
 private fun mealNames(count:Int):List<String> = when(count.coerceIn(2,6)){2->listOf("Comida","Cena");3->listOf("Desayuno","Comida","Cena");4->listOf("Desayuno","Comida","Merienda","Cena");5->listOf("Desayuno","Media mañana","Comida","Merienda","Cena");else->listOf("Desayuno","Media mañana","Comida","Merienda","Cena","Recena")}
 fun ensureToday(plan:CaloriePlan,date:LocalDate=LocalDate.now()):SessionDay{
  val key="active_date";val previous=p.getString(key,null);val today=date.toString()
  if(previous!=today){
   val fresh=TitanEngine.distribute(plan.target,mealNames(p.getInt("meal_count",4)))
   p.edit().putString(key,today).apply();saveDay(fresh,plan,today,false)
   return SessionDay(today,fresh,true)
  }
  val loaded=loadMeals(today)
  if(loaded!=null)return SessionDay(today,loaded,false)
  val fresh=TitanEngine.distribute(plan.target,mealNames(p.getInt("meal_count",4)));saveDay(fresh,plan,today,false)
  return SessionDay(today,fresh,true)
 }
 fun remainingDaysInWeek(date:LocalDate=LocalDate.now())=(7-date.dayOfWeek.value).coerceAtLeast(0)
 fun previousWeekHistory(today:LocalDate=LocalDate.now()):List<DayRecord> = weekHistory(today.minusWeeks(1))
 fun loadMeals(date:String=LocalDate.now().toString()):List<MealSlot>?{
  val raw=p.getString(dayKey(date)+"_meals",null)?:return null
  return raw.split("~").mapNotNull{x->val a=x.split("|");if(a.size<5)null else runCatching{MealSlot(a[0],a[1],a[2].toInt(),a[3].toInt(),MealStatus.valueOf(a[4]),a.getOrNull(5)?.toIntOrNull()?:0,a.getOrNull(6)?.toIntOrNull()?:0,a.getOrNull(7)?.toIntOrNull()?:0)}.getOrNull()}
 }
 fun weekHistory(today:LocalDate=LocalDate.now()):List<DayRecord>{
  val monday=today.minusDays((today.dayOfWeek.value-1).toLong())
  return (0L..6L).mapNotNull{n->val d=monday.plusDays(n).toString();val k=dayKey(d);if(!p.contains(k+"_target"))null else DayRecord(d,p.getInt(k+"_target",0),p.getInt(k+"_tolerance",0),p.getInt(k+"_consumed",0),p.getInt(k+"_excess",0),p.getInt(k+"_confirmed",0),p.getInt(k+"_skipped",0),p.getBoolean(k+"_closed",false))}
 }
 fun activeTarget(defaultTarget:Int):Int{
  val saved=p.getInt("active_target",defaultTarget)
  if(saved>0)return saved
  // Legacy beta builds could persist 0 here. Never let invalid target reach the engine.
  p.edit().remove("active_target").apply()
  return defaultTarget.coerceAtLeast(1)
 }
 fun applyWeeklyAdjustment(adjustment:WeeklyAdjustment,date:String=LocalDate.now().toString()){
  if(adjustment.nextTarget<=0)return
  val history=p.getString("target_history","").orEmpty()
  val safeReason=adjustment.reason.replace("|"," ").replace("~"," ")
  val row=listOf(date,adjustment.currentTarget,adjustment.nextTarget,safeReason).joinToString("|")
  p.edit().putInt("active_target",adjustment.nextTarget).putString("target_history",if(history.isBlank())row else history+"~"+row).apply()
 }
 fun targetHistory():List<TargetChange>{
  val raw=p.getString("target_history","").orEmpty()
  if(raw.isBlank())return emptyList()
  return raw.split("~").mapNotNull{row->val a=row.split("|");if(a.size<4)null else runCatching{TargetChange(a[0],a[1].toInt(),a[2].toInt(),a.drop(3).joinToString(" "))}.getOrNull()}
 }
 fun saveBodyEntry(weightKg:Double,waistCm:Double?,date:String=LocalDate.now().toString()){
  val history=p.getString("body_history","").orEmpty()
  val row=listOf(date,weightKg,waistCm?:"").joinToString("|")
  val rows=history.split("~").filter{it.isNotBlank()&&!it.startsWith(date+"|")}+row
  p.edit().putString("body_history",rows.joinToString("~")).apply()
 }
 fun bodyHistory():List<BodyEntry>{
  val raw=p.getString("body_history","").orEmpty();if(raw.isBlank())return emptyList()
  return raw.split("~").mapNotNull{row->val a=row.split("|");if(a.size<2)null else runCatching{BodyEntry(a[0],a[1].toDouble(),a.getOrNull(2)?.toDoubleOrNull())}.getOrNull()}.sortedBy{it.date}
 }
 fun clear(){p.edit().clear().apply()}
}
