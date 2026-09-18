package com.blsd.titan

import kotlin.math.roundToInt

data class MacroTarget(val proteinG:Int,val carbsG:Int,val fatG:Int)
data class MacroBalance(val target:MacroTarget,val proteinG:Int,val carbsG:Int,val fatG:Int){
 val proteinRemaining get()=(target.proteinG-proteinG).coerceAtLeast(0)
 val carbsRemaining get()=(target.carbsG-carbsG).coerceAtLeast(0)
 val fatRemaining get()=(target.fatG-fatG).coerceAtLeast(0)
}
enum class IngredientGroup { PROTEIN, CARB, FAT, VEGETABLE, OTHER }
data class Ingredient(
 val id:String,val name:String,val grams:Int,val kcalPer100:Int,val proteinPer100:Double,val carbsPer100:Double,val fatPer100:Double,val group:IngredientGroup
){
 val kcal get()=(kcalPer100*grams/100.0).roundToInt()
 val proteinG get()=(proteinPer100*grams/100.0).roundToInt()
 val carbsG get()=(carbsPer100*grams/100.0).roundToInt()
 val fatG get()=(fatPer100*grams/100.0).roundToInt()
 fun withGrams(value:Int)=copy(grams=value.coerceIn(1,1000))
}
data class Dish(val id:String,val name:String,val ingredients:List<Ingredient>){
 val kcal get()=ingredients.sumOf{it.kcal};val proteinG get()=ingredients.sumOf{it.proteinG};val carbsG get()=ingredients.sumOf{it.carbsG};val fatG get()=ingredients.sumOf{it.fatG}
}
data class FoodItem(val id:String,val name:String,val kcalPer100:Int,val proteinPer100:Double,val carbsPer100:Double,val fatPer100:Double)
enum class SocialAmount(val factor:Double){ LITTLE(0.75), NORMAL(1.0), A_LOT(1.35) }
data class SocialMeal(val id:String,val name:String,val baseKcal:Int)

object MealEngine {
 private fun i(id:String,n:String,g:Int,k:Int,p:Double,c:Double,f:Double,group:IngredientGroup)=Ingredient(id,n,g,k,p,c,f,group)
 private val chicken=i("chicken","Pechuga de pollo",180,120,23.0,0.0,2.5,IngredientGroup.PROTEIN)
 private val turkey=i("turkey","Pavo",180,115,24.0,0.0,1.5,IngredientGroup.PROTEIN)
 private val tuna=i("tuna","Atún al natural",160,116,26.0,0.0,1.0,IngredientGroup.PROTEIN)
 private val beef=i("beef","Ternera magra",170,155,26.0,0.0,5.0,IngredientGroup.PROTEIN)
 private val salmon=i("salmon","Salmón",160,208,20.0,0.0,13.0,IngredientGroup.PROTEIN)
 private val rice=i("rice","Arroz cocido",220,130,2.7,28.0,0.3,IngredientGroup.CARB)
 private val potato=i("potato","Patata asada",300,93,2.5,21.0,0.1,IngredientGroup.CARB)
 private val pasta=i("pasta","Pasta cocida",220,131,5.0,25.0,1.1,IngredientGroup.CARB)
 private val veg=i("veg","Verduras variadas",180,45,2.0,8.0,0.5,IngredientGroup.VEGETABLE)
 private val oil=i("oil","Aceite de oliva",10,884,0.0,0.0,100.0,IngredientGroup.FAT)
 val foods=listOf(
  FoodItem("chicken","Pechuga de pollo",120,23.0,0.0,2.5),FoodItem("turkey","Pavo",115,24.0,0.0,1.5),
  FoodItem("tuna","Atún al natural",116,26.0,0.0,1.0),FoodItem("beef","Ternera magra",155,26.0,0.0,5.0),
  FoodItem("salmon","Salmón",208,20.0,0.0,13.0),FoodItem("rice","Arroz cocido",130,2.7,28.0,0.3),
  FoodItem("potato","Patata asada",93,2.5,21.0,0.1),FoodItem("pasta","Pasta cocida",131,5.0,25.0,1.1),
  FoodItem("veg","Verduras variadas",45,2.0,8.0,0.5),FoodItem("oil","Aceite de oliva",884,0.0,0.0,100.0)
 )
 val socialMeals=listOf(SocialMeal("pizza","Pizza",850),SocialMeal("burger","Hamburguesa",900),SocialMeal("sushi","Sushi",700),SocialMeal("bbq","Barbacoa",950),SocialMeal("fried-chicken","Pollo frito",900))
 fun macroTarget(weightKg:Double,targetKcal:Int):MacroTarget{
  val protein=(weightKg*1.8).roundToInt().coerceAtLeast(80)
  val fat=(weightKg*0.8).roundToInt().coerceAtLeast(45)
  val carbs=((targetKcal-protein*4-fat*9).coerceAtLeast(0)/4.0).roundToInt()
  return MacroTarget(protein,carbs,fat)
 }
 fun macroBalance(target:MacroTarget,meals:List<MealSlot>):MacroBalance{
  val p=meals.sumOf{it.proteinG};val carbs=meals.sumOf{it.carbsG};val fat=meals.sumOf{it.fatG}
  return MacroBalance(target,p,carbs,fat)
 }
 fun searchFoods(query:String)=foods.filter{query.isBlank()||it.name.contains(query,true)}
 fun foodKcal(food:FoodItem,grams:Int)=(food.kcalPer100*grams.coerceAtLeast(1)/100.0).roundToInt()
 fun socialKcal(meal:SocialMeal,amount:SocialAmount)=(meal.baseKcal*amount.factor).roundToInt()

 private val dishes=listOf(
  Dish("chicken-rice","Pollo con arroz y verduras",listOf(chicken,rice,veg,oil)),
  Dish("salmon-potato","Salmón con patata y ensalada",listOf(salmon,potato,veg)),
  Dish("turkey-pasta","Pasta con pavo y tomate",listOf(turkey,pasta,veg,oil)),
  Dish("beef-rice","Ternera magra con arroz",listOf(beef,rice,veg)),
  Dish("tuna-potato","Atún con patata y verduras",listOf(tuna,potato,veg,oil))
 )
 fun propose(targetKcal:Int,macro:MacroTarget?=null,exclude:Set<String> = emptySet())=alternatives(targetKcal,macro,exclude,1).firstOrNull() ?: dishes.first()
 fun alternatives(targetKcal:Int,macro:MacroTarget?=null,exclude:Set<String> = emptySet(),limit:Int=4)=dishes.filterNot{it.id in exclude}.sortedBy{d->
  kotlin.math.abs(d.kcal-targetKcal)+(macro?.let{kotlin.math.abs(d.proteinG-it.proteinG)*2.0+kotlin.math.abs(d.carbsG-it.carbsG)*0.5}?:0.0)
 }.take(limit)
 fun ingredientAlternatives(dish:Dish,index:Int,limit:Int=4):List<Ingredient>{
  val current=dish.ingredients.getOrNull(index) ?: return emptyList()
  val pool=listOf(chicken,turkey,tuna,beef,salmon,rice,potato,pasta,veg,oil)
  return pool.filter{it.group==current.group&&it.id!=current.id}.sortedBy{kotlin.math.abs(it.kcal-current.kcal)}.take(limit)
 }
 fun replaceIngredient(dish:Dish,index:Int,replacement:Ingredient):Dish{
  val current=dish.ingredients.getOrNull(index) ?: return dish
  val grams=(current.kcal*100.0/replacement.kcalPer100).roundToInt().coerceAtLeast(1)
  return dish.copy(ingredients=dish.ingredients.mapIndexed{i,x->if(i==index)replacement.withGrams(grams)else x})
 }
 fun resizeIngredient(dish:Dish,index:Int,grams:Int)=if(index !in dish.ingredients.indices) dish else dish.copy(ingredients=dish.ingredients.mapIndexed{i,x->if(i==index)x.withGrams(grams)else x})
}
