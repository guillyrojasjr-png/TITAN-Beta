package com.blsd.titan

data class MacroTarget(val proteinG:Int,val carbsG:Int,val fatG:Int)
data class Ingredient(val name:String,val kcal:Int,val proteinG:Int,val carbsG:Int,val fatG:Int)
data class Dish(val id:String,val name:String,val kcal:Int,val proteinG:Int,val carbsG:Int,val fatG:Int,val ingredients:List<Ingredient> = emptyList())

object MealEngine {
 private val dishes=listOf(
  Dish("chicken-rice","Pollo con arroz y verduras",610,48,67,16),
  Dish("salmon-potato","Salmón con patata y ensalada",625,42,55,24),
  Dish("turkey-pasta","Pasta con pavo y tomate",590,46,72,12),
  Dish("beef-rice","Ternera magra con arroz",640,45,70,20),
  Dish("eggs-toast","Huevos, tostadas y fruta",510,31,52,20),
  Dish("yogurt-oats","Yogur, avena, fruta y crema de cacahuete",475,30,55,15),
  Dish("tuna-wrap","Wrap de atún y vegetales",525,40,58,15),
  Dish("chicken-potato","Pollo, patata asada y ensalada",575,50,55,14)
 )
 fun propose(targetKcal:Int,macro:MacroTarget?=null,exclude:Set<String> = emptySet()):Dish =
  alternatives(targetKcal,macro,exclude,1).first()
 fun alternatives(targetKcal:Int,macro:MacroTarget?=null,exclude:Set<String> = emptySet(),limit:Int=4):List<Dish>{
  return dishes.filterNot{it.id in exclude}.sortedBy{dish->
   val kcalGap=kotlin.math.abs(dish.kcal-targetKcal)
   if(macro==null) kcalGap.toDouble() else kcalGap + kotlin.math.abs(dish.proteinG-macro.proteinG)*2.0 + kotlin.math.abs(dish.carbsG-macro.carbsG)*0.5
  }.take(limit)
 }
}
