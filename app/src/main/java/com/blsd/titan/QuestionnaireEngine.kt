package com.blsd.titan

import kotlin.math.roundToInt

enum class WorkIntensity { LOW, MODERATE, HIGH }
enum class TrainingIntensity { LIGHT, MODERATE, HIGH }

data class Profession(val id:String,val name:String,val referenceMet:Double)

val TITAN_PROFESSIONS=listOf(
 Profession("office","Administrativo / trabajo de oficina",1.5), Profession("driver","Conductor profesional",2.0),
 Profession("teacher","Profesor / docente",2.0), Profession("retail","Dependiente / comercio",2.3),
 Profession("hairdresser","Peluquero / esteticista",2.3), Profession("cook","Cocinero / chef",2.5),
 Profession("waiter","Camarero / hostelería",3.0), Profession("cleaner","Limpieza profesional",3.3),
 Profession("mechanic","Mecánico de automóviles",3.3), Profession("body_painter","Chapista / pintor de automóviles",3.5),
 Profession("warehouse","Almacén / logística",3.5), Profession("healthcare","Sanitario / enfermería",3.0),
 Profession("farmer","Agricultura / jardinería",4.0), Profession("carpenter","Carpintero",4.3),
 Profession("construction","Construcción / albañilería",4.0), Profession("electrician","Electricista / instalador",3.3),
 Profession("plumber","Fontanero",3.5), Profession("factory","Operario industrial / fábrica",3.3),
 Profession("delivery","Repartidor",3.0), Profession("security","Seguridad / vigilancia",2.3),
 Profession("other_light","Otra profesión principalmente sedentaria",1.8), Profession("other_moderate","Otra profesión de actividad moderada",3.0),
 Profession("other_physical","Otra profesión de trabajo físico",4.0)
)


data class ActivityQuestionnaire(
 val standingHours: Double,
 val movingHours: Double,
 val loadHours: Double,
 val workIntensity: WorkIntensity,
 val breaksHours: Double,
 val variableDay: Boolean,
 val profession: Profession = TITAN_PROFESSIONS.first()
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
  val intensityMet = when(activity.workIntensity){WorkIntensity.LOW->1.8;WorkIntensity.MODERATE->2.5;WorkIntensity.HIGH->3.5}
  // Profession supplies a scientific MET reference; the real-day questionnaire adjusts it rather than assigning a fixed calorie value.
  val workMet = (activity.profession.referenceMet*0.55 + intensityMet*0.45).coerceIn(1.5,7.0)
  // Hours can overlap (standing includes moving/loading). Convert them into exclusive
  // portions so the same work hour is never counted two or three times.
  val standingTotal=activity.standingHours.coerceIn(0.0,12.0)
  val movingTotal=activity.movingHours.coerceIn(0.0,standingTotal)
  val loadExclusive=activity.loadHours.coerceIn(0.0,movingTotal)
  val movingExclusive=(movingTotal-loadExclusive).coerceAtLeast(0.0)
  val standingExclusive=(standingTotal-movingTotal).coerceAtLeast(0.0)
  val standing = standingExclusive * weightKg * 0.35
  val moving = movingExclusive * weightKg * (workMet-1.0)
  val load = loadExclusive * weightKg * ((workMet-1.0)+0.65)
  val breaks = activity.breaksHours.coerceIn(0.0,standingTotal) * weightKg * 0.15
  val variability = if(activity.variableDay) 1.05 else 1.0
  val work = ((standing+moving+load-breaks).coerceAtLeast(0.0)*variability).roundToInt()
  val trainMet = when(training.intensity){TrainingIntensity.LIGHT->4.5;TrainingIntensity.MODERATE->6.5;TrainingIntensity.HIGH->8.0}
  val weekly = training.sessionsPerWeek.coerceIn(0,14) * (training.minutesPerSession.coerceIn(0,240)/60.0) * weightKg * (trainMet-1.0)
  return QuestionnaireEnergy(work, (weekly/7.0).roundToInt())
 }
}
