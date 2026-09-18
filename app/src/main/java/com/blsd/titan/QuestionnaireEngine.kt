package com.blsd.titan

import kotlin.math.roundToInt

enum class WorkIntensity { LOW, MODERATE, HIGH }
enum class TrainingIntensity { LIGHT, MODERATE, HIGH }

data class ActivityQuestionnaire(
 val standingHours: Double,
 val movingHours: Double,
 val loadHours: Double,
 val workIntensity: WorkIntensity,
 val breaksHours: Double,
 val variableDay: Boolean
)

data class TrainingQuestionnaire(
 val sessionsPerWeek: Int,
 val minutesPerSession: Int,
 val intensity: TrainingIntensity
)

data class QuestionnaireEnergy(
 val workDailyKcal: Int,
 val trainingDailyKcal: Int
)

object QuestionnaireEngine {
 fun energy(weightKg: Double, activity: ActivityQuestionnaire, training: TrainingQuestionnaire): QuestionnaireEnergy {
  val workMet = when(activity.workIntensity){WorkIntensity.LOW->1.8;WorkIntensity.MODERATE->2.5;WorkIntensity.HIGH->3.5}
  val standing = activity.standingHours.coerceIn(0.0,12.0) * weightKg * 0.35
  val moving = activity.movingHours.coerceIn(0.0,12.0) * weightKg * (workMet-1.0)
  val load = activity.loadHours.coerceIn(0.0,8.0) * weightKg * 1.2
  val breaks = activity.breaksHours.coerceIn(0.0,8.0) * weightKg * 0.15
  val variability = if(activity.variableDay) 1.05 else 1.0
  val work = ((standing+moving+load-breaks).coerceAtLeast(0.0)*variability).roundToInt()
  val trainMet = when(training.intensity){TrainingIntensity.LIGHT->4.5;TrainingIntensity.MODERATE->6.5;TrainingIntensity.HIGH->8.0}
  val weekly = training.sessionsPerWeek.coerceIn(0,14) * (training.minutesPerSession.coerceIn(0,240)/60.0) * weightKg * (trainMet-1.0)
  return QuestionnaireEnergy(work, (weekly/7.0).roundToInt())
 }
}
