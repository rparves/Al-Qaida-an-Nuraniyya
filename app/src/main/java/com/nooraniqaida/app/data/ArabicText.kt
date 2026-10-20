package com.nooraniqaida.app.data

/** Normalises Arabic text so recogniser output can be compared with the expected answer. */
object ArabicText {
    // Harakat, Qur'anic marks, tatweel and whitespace.
    private val marks = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640\\s]")

    fun normalize(s: String): String = s.replace(marks, "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا')
        .replace('ى', 'ي').replace('ئ', 'ي').replace('ؤ', 'و').replace('ة', 'ه')

    fun matches(heard: List<String>, accept: List<String>): Boolean {
        val targets = accept.map(::normalize).filter { it.isNotEmpty() }
        return heard.map(::normalize).any { h ->
            h.isNotEmpty() && targets.any { t -> h == t || h.contains(t) }
        }
    }
}
