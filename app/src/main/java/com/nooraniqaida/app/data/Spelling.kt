package com.nooraniqaida.app.data

/**
 * Furqan-style spelling (hijāʾ): each letter is named with its marks, then the
 * whole is read. أَبْ → "هَمْزَهْ فَتْحَهْ · بَا سُكُونْ → أَبْ".
 */
object Spelling {

    /** One spelled letter: what the voice says and how it is written in English and Bangla. */
    data class Part(val speak: String, val en: String, val bn: String)

    private class Mark(val ar: String, val en: String, val bn: String)

    private val marks = mapOf(
        '\u064E' to Mark("فَتْحَهْ", "fatha", "যবর"),
        '\u0650' to Mark("كَسْرَهْ", "kasra", "যের"),
        '\u064F' to Mark("ضَمَّهْ", "damma", "পেশ"),
        '\u0652' to Mark("سُكُونْ", "sukoon", "জযম"),
        '\u0651' to Mark("شَدَّهْ", "shadda", "তাশদীদ"),
        '\u064B' to Mark("فَتْحَتَانْ", "double fatha", "দুই যবর"),
        '\u064D' to Mark("كَسْرَتَانْ", "double kasra", "দুই যের"),
        '\u064C' to Mark("ضَمَّتَانْ", "double damma", "দুই পেশ"),
        '\u0670' to Mark("أَلِفْ صَغِيرَهْ", "standing fatha", "খাড়া যবর"),
        '\u0656' to Mark("يَاءْ صَغِيرَهْ", "standing kasra", "খাড়া যের"),
        '\u0657' to Mark("وَاوْ صَغِيرَهْ", "inverted damma", "উল্টা পেশ"),
        '\u0653' to Mark("مَدّْ", "madd", "মদ"),
    )

    private val byChar = Qaida.letters.associateBy { it.char[0] }
    private val hamza = Triple("هَمْزَهْ", "Hamzah", "হামযা")
    private val alif = Triple("أَلِفْ", "Alif", "আলিফ")

    private fun name(c: Char, hasMark: Boolean): Triple<String, String, String> = when (c) {
        'أ', 'إ', 'ؤ', 'ئ', 'ء' -> hamza
        'ا', 'ٱ' -> if (hasMark) hamza else alif
        'آ' -> Triple("هَمْزَهْ مَدّْ", "Hamzah madd", "হামযা মদ")
        'ى' -> alif
        'ة' -> Triple("تَا مَرْبُوطَهْ", "Taa marbuta", "গোল তা")
        else -> byChar[c]?.let { Triple(it.nameAr, it.nameEn, it.nameBn) } ?: Triple(c.toString(), c.toString(), c.toString())
    }

    fun parts(text: String): List<Part> {
        val out = mutableListOf<Part>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c.isWhitespace() || c in marks || c == '\u0640') { i++; continue }
            var j = i + 1
            val ms = mutableListOf<Mark>()
            while (j < text.length && text[j] in marks) { ms.add(marks.getValue(text[j])); j++ }
            val (ar, en, bn) = name(c, ms.isNotEmpty())
            out.add(
                Part(
                    (listOf(ar) + ms.map { it.ar }).joinToString(" "),
                    (listOf(en) + ms.map { it.en }).joinToString(" "),
                    (listOf(bn) + ms.map { it.bn }).joinToString(" "),
                )
            )
            i = j
        }
        return out
    }

    /** The text to spell for an item (the stopped form for waqf pairs). */
    fun source(item: QItem): String = item.display.substringAfterLast('\n')
}
