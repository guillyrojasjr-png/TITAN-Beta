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

    fun weeklyRecalibration(excessKcal: Int, remainingDays: Int): WeeklyRecalibration {
        if (excessKcal <= 0 || remainingDays <= 0) return WeeklyRecalibration(excessKcal.coerceAtLeast(0), remainingDays.coerceAtLeast(0), 0)
        return WeeklyRecalibration(excessKcal, remainingDays, (excessKcal.toDouble() / remainingDays).roundToInt())
    }
}
