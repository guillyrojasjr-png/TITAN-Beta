package com.blsd.titan

import android.content.Context
import java.time.LocalDate

data class TitanSavedState(val configured:Boolean,val sex:Sex,val age:Int,val heightCm:Double,val weightKg:Double,val maintenance:Int,val strategy:Strategy)
data class BodyEntry(val date:String,val weightKg:Double,val waistCm:Double?)\ndata class TargetChange(val date:String,val previousTarget:Int,val newTarget:Int,val reason:String)\ndata class DayRecord(val date:String,val target:Int,val tolerance:Int,val consumed:Int,val excess:Int,val confirmed:Int,val skipped:Int,val closed:Boolean)

class TitanStore(context:Context){
 private val p=context.getSharedPreferences("titan_beta",Context.MODE_PRIVATE)
 fun load():TitanSavedState?{if(!p.getBoolean("configured",false))return null;return TitanSavedState(true,runCatching{Sex.valueOf(p.getString("sex","MALE")!!)}.getOrDefault(Sex.MALE),p.getInt("age",30),p.getFloat("height",170f).toDouble(),p.getFloat("weight",70f).toDouble(),p.getInt("maintenance",0),runCatching{Strategy.valueOf(p.getString("strategy","MODERATE")!!)}.getOrDefault(Strategy.MODERATE))}
 fun save(profile:UserProfile,maintenance:Int,strategy:Strategy){p.edit().putBoolean("configured",true).putString("sex",profile.sex.name).putInt("age",profile.age).putFloat("height",profile.heightCm.toFloat()).putFloat("weight",profile.weightKg.toFloat()).putInt("maintenance",maintenance).putString("strategy",strategy.name).apply()}
 private fun dayKey(date:String)= "day_"+date
 fun saveDay(meals:List<MealSlot>,plan:CaloriePlan,date:String=LocalDate.now().toString(),closed:Boolean=false){
  val encoded=meals.joinToString("~"){listOf(it.id,it.name.replace("|"," "),it.plannedKcal,it.consumedKcal,it.status.name,it.proteinG,it.carbsG,it.fatG).joinToString("|")}
  val r=TitanEngine.closeDay(plan,meals)
  p.edit().putString(dayKey(date)+"_meals",encoded).putInt(dayKey(date)+"_target",plan.target).putInt(dayKey(date)+"_tolerance",plan.toleranceCeiling).putInt(dayKey(date)+"_consumed",r.consumed).putInt(dayKey(date)+"_excess",r.excessToRecalibrate).putInt(dayKey(date)+"_confirmed",r.completedMeals).putInt(dayKey(date)+"_skipped",r.skippedMeals).putBoolean(dayKey(date)+"_closed",closed).apply()
 }
 fun loadMeals(date:String=LocalDate.now().toString()):List<MealSlot>?{
  val raw=p.getString(dayKey(date)+"_meals",null)?:return null
  return raw.split("~").mapNotNull{x->val a=x.split("|");if(a.size<5)null else runCatching{MealSlot(a[0],a[1],a[2].toInt(),a[3].toInt(),MealStatus.valueOf(a[4]),a.getOrNull(5)?.toIntOrNull()?:0,a.getOrNull(6)?.toIntOrNull()?:0,a.getOrNull(7)?.toIntOrNull()?:0)}.getOrNull()}
 }
 fun weekHistory(today:LocalDate=LocalDate.now()):List<DayRecord>{
  val monday=today.minusDays((today.dayOfWeek.value-1).toLong())
  return (0L..6L).mapNotNull{n->val d=monday.plusDays(n).toString();val k=dayKey(d);if(!p.contains(k+"_target"))null else DayRecord(d,p.getInt(k+"_target",0),p.getInt(k+"_tolerance",0),p.getInt(k+"_consumed",0),p.getInt(k+"_excess",0),p.getInt(k+"_confirmed",0),p.getInt(k+"_skipped",0),p.getBoolean(k+"_closed",false))}
 }
 fun activeTarget(defaultTarget:Int)=p.getInt("active_target",defaultTarget)
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
