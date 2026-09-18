package com.blsd.titan

import android.app.Application
import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

class TitanApplication : Application() {
 override fun onCreate() {
  super.onCreate()
  val previous = Thread.getDefaultUncaughtExceptionHandler()
  Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
   runCatching {
    val sw=StringWriter()
    throwable.printStackTrace(PrintWriter(sw))
    getSharedPreferences(CRASH_PREFS,Context.MODE_PRIVATE).edit()
     .putString(CRASH_KEY,sw.toString().take(12000))
     .apply()
   }
   if(previous!=null) previous.uncaughtException(thread,throwable)
  }
 }
 companion object {
  const val CRASH_PREFS="titan_crash_diagnostics"
  const val CRASH_KEY="last_crash"
 }
}