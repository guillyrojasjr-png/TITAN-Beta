package com.blsd.titan

import android.content.Context

data class TitanSavedState(
 val configured:Boolean,
 val sex:Sex,
 val age:Int,
 val heightCm:Double,
 val weightKg:Double,
 val maintenance:Int,
 val strategy:Strategy
)

class TitanStore(context:Context){
 private val p=context.getSharedPreferences("titan_beta",Context.MODE_PRIVATE)
 fun load():TitanSavedState?{
  if(!p.getBoolean("configured",false))return null
  return TitanSavedState(true,
   runCatching{Sex.valueOf(p.getString("sex","MALE")!!)}.getOrDefault(Sex.MALE),
   p.getInt("age",30),p.getFloat("height",170f).toDouble(),p.getFloat("weight",70f).toDouble(),
   p.getInt("maintenance",0),
   runCatching{Strategy.valueOf(p.getString("strategy","MODERATE")!!)}.getOrDefault(Strategy.MODERATE))
 }
 fun save(profile:UserProfile,maintenance:Int,strategy:Strategy){
  p.edit().putBoolean("configured",true).putString("sex",profile.sex.name).putInt("age",profile.age)
   .putFloat("height",profile.heightCm.toFloat()).putFloat("weight",profile.weightKg.toFloat())
   .putInt("maintenance",maintenance).putString("strategy",strategy.name).apply()
 }
 fun clear(){p.edit().clear().apply()}
}
