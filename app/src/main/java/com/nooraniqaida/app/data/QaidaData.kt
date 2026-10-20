package com.nooraniqaida.app.data

import com.nooraniqaida.app.R

enum class Makhraj(val titleRes: Int, val descRes: Int) {
    JAWF(R.string.makhraj_jawf, R.string.makhraj_jawf_desc),
    HALQ(R.string.makhraj_halq, R.string.makhraj_halq_desc),
    LISAN(R.string.makhraj_lisan, R.string.makhraj_lisan_desc),
    SHAFATAN(R.string.makhraj_shafatan, R.string.makhraj_shafatan_desc),
}

/** One Arabic letter. [soundEn] and [soundBn] are the consonant sound used to build syllables. */
data class Harf(
    val id: String,
    val char: String,
    val nameAr: String,
    val nameEn: String,
    val nameBn: String,
    val soundEn: String,
    val soundBn: String,
    val makhraj: Makhraj,
    val vowelOnly: Boolean = false,
)

/** One tappable cell in a lesson. */
data class QItem(
    val id: String,              // also the optional audio file name: assets/audio/<id>.ogg
    val display: String,         // what is shown, fully vowelled
    val speak: String,           // what the TTS engine reads
    val en: String,              // English transliteration
    val bn: String,              // Bangla transliteration
    val accept: List<String>,    // Arabic text the speech recogniser may return for a correct attempt
    val harf: Harf? = null,
)

data class Lesson(
    val number: Int,
    val titleAr: String,
    val titleRes: Int,
    val descRes: Int,
    val columns: Int,
    val items: List<QItem>,
)

/**
 * All lesson content. Most lessons are generated from the 29 letters, so the
 * whole curriculum costs a few kilobytes instead of shipping a data file.
 */
object Qaida {

    val letters: List<Harf> = listOf(
        Harf("alif", "ا", "أَلِف", "Alif", "আলিফ", "", "", Makhraj.JAWF, vowelOnly = true),
        Harf("ba", "ب", "بَاء", "Baa", "বা", "b", "ব", Makhraj.SHAFATAN),
        Harf("ta", "ت", "تَاء", "Taa", "তা", "t", "ত", Makhraj.LISAN),
        Harf("tha", "ث", "ثَاء", "Thaa", "ছা", "th", "ছ", Makhraj.LISAN),
        Harf("jeem", "ج", "جِيم", "Jeem", "জীম", "j", "জ", Makhraj.LISAN),
        Harf("hha", "ح", "حَاء", "Ḥaa", "হা", "ḥ", "হ", Makhraj.HALQ),
        Harf("kha", "خ", "خَاء", "Khaa", "খা", "kh", "খ", Makhraj.HALQ),
        Harf("dal", "د", "دَال", "Daal", "দাল", "d", "দ", Makhraj.LISAN),
        Harf("dhal", "ذ", "ذَال", "Dhaal", "যাল", "dh", "য", Makhraj.LISAN),
        Harf("ra", "ر", "رَاء", "Raa", "রা", "r", "র", Makhraj.LISAN),
        Harf("zay", "ز", "زَاي", "Zaay", "যা", "z", "য", Makhraj.LISAN),
        Harf("seen", "س", "سِين", "Seen", "সীন", "s", "স", Makhraj.LISAN),
        Harf("sheen", "ش", "شِين", "Sheen", "শীন", "sh", "শ", Makhraj.LISAN),
        Harf("sad", "ص", "صَاد", "Ṣaad", "সোয়াদ", "ṣ", "স", Makhraj.LISAN),
        Harf("dad", "ض", "ضَاد", "Ḍaad", "দোয়াদ", "ḍ", "দ্ব", Makhraj.LISAN),
        Harf("tta", "ط", "طَاء", "Ṭaa", "ত্বোয়া", "ṭ", "ত্ব", Makhraj.LISAN),
        Harf("zza", "ظ", "ظَاء", "Ẓaa", "যোয়া", "ẓ", "য", Makhraj.LISAN),
        Harf("ain", "ع", "عَيْن", "ʿAyn", "আইন", "ʿ", "", Makhraj.HALQ, vowelOnly = true),
        Harf("ghain", "غ", "غَيْن", "Ghayn", "গাইন", "gh", "গ", Makhraj.HALQ),
        Harf("fa", "ف", "فَاء", "Faa", "ফা", "f", "ফ", Makhraj.SHAFATAN),
        Harf("qaf", "ق", "قَاف", "Qaaf", "ক্বফ", "q", "ক্ব", Makhraj.LISAN),
        Harf("kaf", "ك", "كَاف", "Kaaf", "কাফ", "k", "ক", Makhraj.LISAN),
        Harf("lam", "ل", "لَام", "Laam", "লাম", "l", "ল", Makhraj.LISAN),
        Harf("meem", "م", "مِيم", "Meem", "মীম", "m", "ম", Makhraj.SHAFATAN),
        Harf("noon", "ن", "نُون", "Noon", "নূন", "n", "ন", Makhraj.LISAN),
        Harf("waw", "و", "وَاو", "Waaw", "ওয়াও", "w", "ওয়", Makhraj.SHAFATAN),
        Harf("ha", "ه", "هَاء", "Haa", "হা", "h", "হ", Makhraj.HALQ),
        Harf("hamza", "ء", "هَمْزَة", "Hamzah", "হামযা", "ʾ", "", Makhraj.HALQ, vowelOnly = true),
        Harf("ya", "ي", "يَاء", "Yaa", "ইয়া", "y", "ইয়", Makhraj.LISAN),
    )

    private enum class V(
        val en: String, val enLong: String,
        val mark: String, val tanween: String, val maddLetter: String,
        val bnSign: String, val bnLongSign: String, val bnVowel: String, val bnLongVowel: String,
        val alifSeat: String,
    ) {
        A("a", "aa", "\u064E", "\u064B", "ا", "া", "া", "আ", "আ", "أ"),
        I("i", "ee", "\u0650", "\u064D", "ي", "ি", "ী", "ই", "ঈ", "إ"),
        U("u", "oo", "\u064F", "\u064C", "و", "ু", "ূ", "উ", "ঊ", "أ"),
    }

    private const val SUKUN = "\u0652"
    private const val SHADDA = "\u0651"

    /** Letters that are not used in the simple sukoon/tashdeed drills. */
    private val noJoin = setOf("alif", "ain", "hamza", "waw", "ya")

    private fun seat(h: Harf, v: V) = if (h.id == "alif") v.alifSeat else h.char
    private fun key(v: V) = v.name.lowercase()

    private fun bn(h: Harf, v: V, long: Boolean = false): String =
        if (h.vowelOnly) (if (long) v.bnLongVowel else v.bnVowel)
        else h.soundBn + (if (long) v.bnLongSign else v.bnSign)

    fun letterItem(h: Harf) =
        QItem("l_${h.id}", h.char, h.nameAr, h.nameEn, h.nameBn, listOf(h.nameAr, h.char), h)

    private fun harakat() = letters.flatMap { h ->
        V.entries.map { v ->
            val s = seat(h, v)
            val text = s + v.mark
            QItem("h_${h.id}_${key(v)}", text, text, h.soundEn + v.en, bn(h, v), listOf(s + v.maddLetter, s), h)
        }
    }

    private fun tanween() = letters.flatMap { h ->
        V.entries.map { v ->
            val s = seat(h, v)
            val text = s + v.tanween
            QItem("t_${h.id}_${key(v)}", text, text, h.soundEn + v.en + "n", bn(h, v) + "ন", listOf(s + "ن", s), h)
        }
    }

    private fun madd() = letters.filter { it.id != "alif" && it.id != "hamza" }.flatMap { h ->
        V.entries.map { v ->
            val text = h.char + v.mark + v.maddLetter
            QItem("d_${h.id}_${key(v)}", text, text, h.soundEn + v.enLong, bn(h, v, long = true) + "—",
                listOf(h.char + v.maddLetter, h.char), h)
        }
    }

    private fun sukoon() = letters.filter { it.id !in noJoin }.flatMap { h ->
        V.entries.map { v ->
            val text = v.alifSeat + v.mark + h.char + SUKUN
            QItem("s_${h.id}_${key(v)}", text, text, v.en + h.soundEn, v.bnVowel + h.soundBn + "্",
                listOf("ا" + h.char), h)
        }
    }

    private fun tashdeed() = letters.filter { it.id !in noJoin }.flatMap { h ->
        V.entries.map { v ->
            val text = v.alifSeat + v.mark + h.char + SHADDA + v.mark
            QItem("sh_${h.id}_${key(v)}", text, text,
                v.en + h.soundEn + h.soundEn + v.en,
                v.bnVowel + h.soundBn.first() + "্" + h.soundBn + v.bnSign,
                listOf("ا" + h.char + h.char, "ا" + h.char), h)
        }
    }

    private fun muqattaat(): List<QItem> = listOf(
        listOf("الٓمٓ", "أَلِفْ لَامْ مِيمْ", "Alif Laam Meem", "আলিফ লাম মীম"),
        listOf("الٓر", "أَلِفْ لَامْ رَا", "Alif Laam Raa", "আলিফ লাম রা"),
        listOf("الٓمٓر", "أَلِفْ لَامْ مِيمْ رَا", "Alif Laam Meem Raa", "আলিফ লাম মীম রা"),
        listOf("الٓمٓصٓ", "أَلِفْ لَامْ مِيمْ صَادْ", "Alif Laam Meem Ṣaad", "আলিফ লাম মীম সোয়াদ"),
        listOf("كٓهيعٓصٓ", "كَافْ هَا يَا عَيْنْ صَادْ", "Kaaf Haa Yaa ʿAyn Ṣaad", "কাফ হা ইয়া আইন সোয়াদ"),
        listOf("طه", "طَا هَا", "Ṭaa Haa", "ত্বোয়া হা"),
        listOf("طسٓمٓ", "طَا سِينْ مِيمْ", "Ṭaa Seen Meem", "ত্বোয়া সীন মীম"),
        listOf("طسٓ", "طَا سِينْ", "Ṭaa Seen", "ত্বোয়া সীন"),
        listOf("يسٓ", "يَا سِينْ", "Yaa Seen", "ইয়া সীন"),
        listOf("صٓ", "صَادْ", "Ṣaad", "সোয়াদ"),
        listOf("حمٓ", "حَا مِيمْ", "Ḥaa Meem", "হা মীম"),
        listOf("عٓسٓقٓ", "عَيْنْ سِينْ قَافْ", "ʿAyn Seen Qaaf", "আইন সীন ক্বফ"),
        listOf("قٓ", "قَافْ", "Qaaf", "ক্বফ"),
        listOf("نٓ", "نُونْ", "Noon", "নূন"),
    ).mapIndexed { i, (display, speak, en, bn) ->
        QItem("m_${i + 1}", display, speak, en, bn, listOf(speak))
    }

    val lessons: List<Lesson> by lazy {
        listOf(
            Lesson(1, "الحروف المفردة", R.string.l1_title, R.string.l1_desc, 4, letters.map { letterItem(it) }),
            Lesson(2, "الحروف المقطعات", R.string.l2_title, R.string.l2_desc, 2, muqattaat()),
            Lesson(3, "الحركات", R.string.l3_title, R.string.l3_desc, 3, harakat()),
            Lesson(4, "التنوين", R.string.l4_title, R.string.l4_desc, 3, tanween()),
            Lesson(5, "حروف المد", R.string.l5_title, R.string.l5_desc, 3, madd()),
            Lesson(6, "السكون", R.string.l6_title, R.string.l6_desc, 3, sukoon()),
            Lesson(7, "التشديد", R.string.l7_title, R.string.l7_desc, 3, tashdeed()),
        )
    }
}
