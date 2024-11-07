package com.example.recyc.utils

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private lateinit var logFilePath: String

    fun init(cacheDir: File) {
        logFilePath = File(cacheDir, "logfile.log").absolutePath
    }

    fun log(tag: String, message: String) {
        Log.d(tag, message)
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timeStamp - $tag : $message\n"

        try {
            val file = File(logFilePath)
            val fileWriter = FileWriter(file, true)
            fileWriter.append(logMessage)
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}