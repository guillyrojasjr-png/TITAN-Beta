package com.blsd.titan

import kotlin.math.roundToInt

enum class Sex { MALE, FEMALE }
enum class Strategy { LOW, MODERATE, RIGOROUS }
enum class MealStatus { PENDING, CONFIRMED, SKIPPED }

data class UserProfile(
    val sex: Sex,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val activityFactor: Double = 1.2,
    val dailyActivityKcal: Int = 0,
    val trainingKcal: Int = 0
)

data class EnergyEstimate(
    val bmr: Int,
    val baseWithActivity: Int,
    val dailyActivityKcal: Int,
    val trainingKcal: Int,
    val maintenance: Int
)

data class CaloriePlan(
    val strategy: Strategy,
    val target: Int,
    val toleranceCeiling: Int
)

data class MealSlot(
    val id: String,
    val name: String,
    val plannedKcal: Int,
    val consumedKcal: Int = 0,
    val status: MealStatus = MealStatus.PENDING
)

data class DailyBalance(
    val target: Int,
    val toleranceCeiling: Int,
    val consumed: Int,
    val availableToTarget: Int,
    val excessOverTolerance: Int
)

data class DayCloseResult(val consumed:Int,val target:Int,val toleranceCeiling:Int,val excessToRecalibrate:Int,val completedMeals:Int,val skippedMeals:Int)\n\ndata class BodyTrend(val entries:Int,val weightChangeKg:Double,val waistChangeCm:Double?,val weeklyWeightRateKg:Double,val message:String)
data class WeeklyFeedback(val hunger:Int,val energy:Int,val recovery:Int,val performance:Int,val stress:Int,val satisfaction:Int)
data class WeeklyReview(val days:Int,val averageKcal:Int,val targetAverage:Int,val adherencePercent:Int,val excessOverTolerance:Int,val completedMeals:Int,val skippedMeals:Int,val feedback:WeeklyFeedback)
data class WeeklyAdjustment(val currentTarget:Int,val nextTarget:Int,val delta:Int,val reason:String)

data class WeeklyRecalibration(
    val excessKcal: Int,
    val remainingDays: Int,
    val adjustmentPerDay: Int
)

object TitanEngine {
    fun estimateEnergy(profile: UserProfile): EnergyEstimate {
        require(profile.age > 0 && profile.heightCm > 0 && profile.weightKg > 0)
        val sexConstant = if (profile.sex == Sex.MALE) 5.0 else -161.0
        val bmr = (10.0 * profile.weightKg + 6.25 * profile.heightCm - 5.0 * profile.age + sexConstant).roundToInt()
        val base = (bmr * profile.activityFactor).roundToInt()
        return EnergyEstimate(
            bmr = bmr,
            baseWithActivity = base,
            dailyActivityKcal = profile.dailyActivityKcal,
            trainingKcal = profile.trainingKcal,
            maintenance = base + profile.dailyActivityKcal + profile.trainingKcal
        )
    }

    fun plans(maintenance: Int): List<CaloriePlan> {
        require(maintenance > 0)
        val low = (maintenance * 0.90).roundToInt()
        val moderate = (maintenance * 0.82).roundToInt()
        val rigorous = (maintenance * 0.75).roundToInt()
        return listOf(
            CaloriePlan(Strategy.LOW, low, maintenance),
            CaloriePlan(Strategy.MODERATE, moderate, low),
            CaloriePlan(Strategy.RIGOROUS, rigorous, moderate)
        )
    }

    fun distribute(target: Int, mealNames: List<String>): List<MealSlot> {
        require(target > 0 && mealNames.isNotEmpty())
        val base = target / mealNames.size
        var remainder = target - base * mealNames.size
        return mealNames.mapIndexed { index, name ->
            val extra = if (remainder > 0) { remainder--; 1 } else 0
            MealSlot(id = "meal-$index", name = name, plannedKcal = base + extra)
        }
    }

    fun redistribute(meals: List<MealSlot>): List<MealSlot> {
        val pending = meals.filter { it.status == MealStatus.PENDING }
        if (pending.isEmpty()) return meals
        val totalPlan = meals.sumOf { it.plannedKcal }
        val locked = meals.filter { it.status != MealStatus.PENDING }.sumOf {
            if (it.status == MealStatus.CONFIRMED) it.consumedKcal else 0
        }
        val remaining = (totalPlan - locked).coerceAtLeast(0)
        val base = remaining / pending.size
        var remainder = remaining - base * pending.size
        return meals.map { meal ->
            if (meal.status != MealStatus.PENDING) meal
            else {
                val extra = if (remainder > 0) { remainder--; 1 } else 0
                meal.copy(plannedKcal = base + extra)
            }
        }
    }

    fun balance(plan: CaloriePlan, consumed: Int): DailyBalance {
        val safeConsumed = consumed.coerceAtLeast(0)
        val excess = (safeConsumed - plan.toleranceCeiling).coerceAtLeast(0)
        return DailyBalance(
            target = plan.target,
            toleranceCeiling = plan.toleranceCeiling,
            consumed = safeConsumed,
            availableToTarget = (plan.target - safeConsumed).coerceAtLeast(0),
            excessOverTolerance = excess
        )
    }

    fun closeDay(plan:CaloriePlan,meals:List<MealSlot>):DayCloseResult {
        val b=balance(plan,meals.sumOf{it.consumedKcal})
        return DayCloseResult(b.consumed,b.target,b.toleranceCeiling,b.excessOverTolerance,meals.count{it.status==MealStatus.CONFIRMED},meals.count{it.status==MealStatus.SKIPPED})
    }

    fun bodyTrend(entries:List<BodyEntry>):BodyTrend{
        if(entries.size<2)return BodyTrend(entries.size,0.0,null,0.0,"Necesitamos al menos dos registros para mostrar una tendencia.")
        val first=entries.first();val last=entries.last()
        val days=runCatching{java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.parse(first.date),java.time.LocalDate.parse(last.date)).toInt()}.getOrDefault(0).coerceAtLeast(1)
        val change=last.weightKg-first.weightKg
        val rate=change*7.0/days
        val waist=if(first.waistCm!=null&&last.waistCm!=null)last.waistCm-first.waistCm else null
        val msg=when{days<7->"Tendencia inicial: aún necesitamos más días.";kotlin.math.abs(rate)<0.1->"Peso bastante estable en el periodo registrado.";rate<0->"Tendencia de peso descendente.";else->"Tendencia de peso ascendente."}
        return BodyTrend(entries.size,change,waist,rate,msg)
    }

    fun weeklyReview(days:List<DayRecord>,feedback:WeeklyFeedback):WeeklyReview{
        if(days.isEmpty())return WeeklyReview(0,0,0,0,0,0,0,feedback)
        val avg=days.map{it.consumed}.average().roundToInt()
        val target=days.map{it.target}.average().roundToInt()
        val adherence=(100.0-(kotlin.math.abs(avg-target)*100.0/target.coerceAtLeast(1))).roundToInt().coerceIn(0,100)
        return WeeklyReview(days.size,avg,target,adherence,days.sumOf{it.excess},days.sumOf{it.confirmed},days.sumOf{it.skipped},feedback)
    }
    fun weeklyAdjustment(review:WeeklyReview,currentTarget:Int):WeeklyAdjustment{
        if(review.days<4)return WeeklyAdjustment(currentTarget,currentTarget,0,"Aún faltan datos para ajustar la semana.")
        val recoveryLoad=(review.feedback.hunger+review.feedback.stress+(6-review.feedback.energy)+(6-review.feedback.recovery)+(6-review.feedback.performance)+(6-review.feedback.satisfaction))/6.0
        val delta=when{
            recoveryLoad>=4.2 -> 100
            review.adherencePercent<75 -> 0
            review.excessOverTolerance>currentTarget/2 -> 0
            else -> 0
        }
        val next=(currentTarget+delta).coerceAtLeast(1200)
        val reason=when{delta>0->"Se suaviza ligeramente el objetivo por hambre/estrés/recuperación.";review.adherencePercent<75->"Se mantiene el objetivo: primero necesitamos una semana más consistente.";else->"Se mantiene el objetivo: los datos no justifican un cambio todavía."}
        return WeeklyAdjustment(currentTarget,next,delta,reason)
    }

    fun weeklyRecalibration(excessKcal: Int, remainingDays: Int): WeeklyRecalibration {
        if (excessKcal <= 0 || remainingDays <= 0) return WeeklyRecalibration(excessKcal.coerceAtLeast(0), remainingDays.coerceAtLeast(0), 0)
        return WeeklyRecalibration(excessKcal, remainingDays, (excessKcal.toDouble() / remainingDays).roundToInt())
    }
}
