package com.blsd.titan

import android.content.Context
import java.time.LocalDate

data class TitanSavedState(val configured:Boolean,val sex:Sex,val age:Int,val heightCm:Double,val weightKg:Double,val maintenance:Int,val strategy:Strategy)
data class DayRecord(val date:String,val target:Int,val tolerance:Int,val consumed:Int,val excess:Int,val confirmed:Int,val skipped:Int,val closed:Boolean)

class TitanStore(context:Context){
 private val p=context.getSharedPreferences("titan_beta",Context.MODE_PRIVATE)
 fun load():TitanSavedState?{if(!p.getBoolean("configured",false))return null;return TitanSavedState(true,runCatching{Sex.valueOf(p.getString("sex","MALE")!!)}.getOrDefault(Sex.MALE),p.getInt("age",30),p.getFloat("height",170f).toDouble(),p.getFloat("weight",70f).toDouble(),p.getInt("maintenance",0),runCatching{Strategy.valueOf(p.getString("strategy","MODERATE")!!)}.getOrDefault(Strategy.MODERATE))}
 fun save(profile:UserProfile,maintenance:Int,strategy:Strategy){p.edit().putBoolean("configured",true).putString("sex",profile.sex.name).putInt("age",profile.age).putFloat("height",profile.heightCm.toFloat()).putFloat("weight",profile.weightKg.toFloat()).putInt("maintenance",maintenance).putString("strategy",strategy.name).apply()}
 private fun dayKey(date:String)= "day_"+date
 fun saveDay(meals:List<MealSlot>,plan:CaloriePlan,date:String=LocalDate.now().toString(),closed:Boolean=false){
  val encoded=meals.joinToString("~"){listOf(it.id,it.name.replace("|"," "),it.plannedKcal,it.consumedKcal,it.status.name).joinToString("|")}
  val r=TitanEngine.closeDay(plan,meals)
  p.edit().putString(dayKey(date)+"_meals",encoded).putInt(dayKey(date)+"_target",plan.target).putInt(dayKey(date)+"_tolerance",plan.toleranceCeiling).putInt(dayKey(date)+"_consumed",r.consumed).putInt(dayKey(date)+"_excess",r.excessToRecalibrate).putInt(dayKey(date)+"_confirmed",r.completedMeals).putInt(dayKey(date)+"_skipped",r.skippedMeals).putBoolean(dayKey(date)+"_closed",closed).apply()
 }
 fun loadMeals(date:String=LocalDate.now().toString()):List<MealSlot>?{
  val raw=p.getString(dayKey(date)+"_meals",null)?:return null
  return raw.split("~").mapNotNull{x->val a=x.split("|");if(a.size<5)null else runCatching{MealSlot(a[0],a[1],a[2].toInt(),a[3].toInt(),MealStatus.valueOf(a[4]))}.getOrNull()}
 }
 fun weekHistory(today:LocalDate=LocalDate.now()):List<DayRecord>{
  val monday=today.minusDays((today.dayOfWeek.value-1).toLong())
  return (0L..6L).mapNotNull{n->val d=monday.plusDays(n).toString();val k=dayKey(d);if(!p.contains(k+"_target"))null else DayRecord(d,p.getInt(k+"_target",0),p.getInt(k+"_tolerance",0),p.getInt(k+"_consumed",0),p.getInt(k+"_excess",0),p.getInt(k+"_confirmed",0),p.getInt(k+"_skipped",0),p.getBoolean(k+"_closed",false))}
 }
 fun clear(){p.edit().clear().apply()}
}
