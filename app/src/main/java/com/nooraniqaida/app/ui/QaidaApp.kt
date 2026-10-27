package com.nooraniqaida.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nooraniqaida.app.audio.Recorder
import com.nooraniqaida.app.audio.Speaker
import com.nooraniqaida.app.data.Progress
import com.nooraniqaida.app.data.Qaida

/**
 * Routes: "home", "lesson:N", "quiz:N", "trace". Saved as a string so the screen
 * survives the activity recreate after a language switch.
 */
@Composable
fun QaidaApp(speaker: Speaker, progress: Progress, recorder: Recorder) {
    var route by rememberSaveable { mutableStateOf("home") }
    BackHandler(enabled = route != "home") { route = "home" }

    val parts = route.split(":")
    val lesson = parts.getOrNull(1)?.toIntOrNull()?.let { n -> Qaida.lessons.firstOrNull { it.number == n } }

    when {
        parts[0] == "lesson" && lesson != null ->
            LessonScreen(lesson, speaker, recorder, progress.teacherMode, onBack = { route = "home" }, onQuiz = { route = "quiz:${lesson.number}" })
        parts[0] == "quiz" && lesson != null ->
            QuizScreen(lesson, speaker, progress, onBack = { route = "home" })
        parts[0] == "trace" ->
            TraceScreen(speaker, onBack = { route = "home" })
        else ->
            HomeScreen(
                speaker, progress, recorder,
                onLesson = { route = "lesson:$it" },
                onQuiz = { route = "quiz:$it" },
                onTrace = { route = "trace" },
            )
    }
}
