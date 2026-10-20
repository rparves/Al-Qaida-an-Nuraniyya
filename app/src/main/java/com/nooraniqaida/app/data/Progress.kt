package com.nooraniqaida.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Learner name, mastered lessons and best quiz scores, kept in SharedPreferences. */
class Progress(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("progress", Context.MODE_PRIVATE)

    var name by mutableStateOf(prefs.getString("name", "") ?: "")
        private set
    var mastered by mutableStateOf(prefs.getStringSet("mastered", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet())
        private set
    var stars by mutableStateOf(prefs.getInt("stars", 0))
        private set
    /** How many times the quiz sound may be played per question; 0 means unlimited. */
    var listenLimit by mutableStateOf(prefs.getInt("listen_limit", 0))
        private set

    fun updateListenLimit(value: Int) {
        listenLimit = value
        prefs.edit().putInt("listen_limit", value).apply()
    }

    fun updateName(value: String) {
        name = value.take(30)
        prefs.edit().putString("name", name).apply()
    }

    fun best(lesson: Int) = prefs.getInt("best_$lesson", 0)

    /** Records a finished quiz; returns true if the lesson is now mastered. */
    fun recordQuiz(lesson: Int, score: Int, total: Int): Boolean {
        val e = prefs.edit()
        if (score > best(lesson)) e.putInt("best_$lesson", score)
        stars += score
        e.putInt("stars", stars)
        val passed = score * 10 >= total * 8
        if (passed && lesson !in mastered) {
            mastered = mastered + lesson
            e.putStringSet("mastered", mastered.map { it.toString() }.toSet())
        }
        e.apply()
        return passed
    }

    fun reset() {
        val keep = name
        val limit = listenLimit
        prefs.edit().clear().putString("name", keep).putInt("listen_limit", limit).apply()
        mastered = emptySet()
        stars = 0
    }
}
