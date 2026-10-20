package com.nooraniqaida.app.data

import com.nooraniqaida.app.R

enum class Makhraj(val titleRes: Int, val descRes: Int) {
    JAWF(R.string.makhraj_jawf, R.string.makhraj_jawf_desc),
    HALQ(R.string.makhraj_halq, R.string.makhraj_halq_desc),
    LISAN(R.string.makhraj_lisan, R.string.makhraj_lisan_desc),
    SHAFATAN(R.string.makhraj_shafatan, R.string.makhraj_shafatan_desc),
}

/** Text in the three interface languages. */
data class L(val en: String, val bn: String, val ar: String) {
    fun pick(lang: String) = when (lang) { "bn" -> bn; "ar" -> ar; else -> en }
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
    val title: L,
    val desc: L,
    val icon: String,
    val columns: Int,
    val items: List<QItem>,
)

/**
 * The 17 lessons of Al-Qaida an-Nuraniyya. Drill lessons are generated from the
 * 29 letters, so the whole curriculum costs a few kilobytes.
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
    private val byId = letters.associateBy { it.id }

    private enum class V(
        val en: String, val enLong: String,
        val mark: String, val tanween: String, val maddLetter: String, val standing: String,
        val bnSign: String, val bnLongSign: String, val bnVowel: String, val bnLongVowel: String,
        val alifSeat: String,
    ) {
        A("a", "aa", "\u064E", "\u064B", "ا", "\u0670", "া", "া", "আ", "আ", "أ"),
        I("i", "ee", "\u0650", "\u064D", "ي", "\u0656", "ি", "ী", "ই", "ঈ", "إ"),
        U("u", "oo", "\u064F", "\u064C", "و", "\u0657", "ু", "ূ", "উ", "ঊ", "أ"),
    }

    private const val SUKUN = "\u0652"
    private const val SHADDA = "\u0651"

    /** Letters left out of the simple sukoon/tashdeed drills. */
    private val noJoin = setOf("alif", "ain", "hamza", "waw", "ya")

    private fun seat(h: Harf, v: V) = if (h.id == "alif") v.alifSeat else h.char
    private fun key(v: V) = v.name.lowercase()

    private fun bn(h: Harf, v: V, long: Boolean = false): String =
        if (h.vowelOnly) (if (long) v.bnLongVowel else v.bnVowel)
        else h.soundBn + (if (long) v.bnLongSign else v.bnSign)

    fun letterItem(h: Harf) =
        QItem("l_${h.id}", h.char, h.nameAr, h.nameEn, h.nameBn, listOf(h.nameAr, h.char), h)

    /** A plain word/phrase item. */
    private fun w(prefix: String, i: Int, ar: String, en: String, bn: String, speak: String = ar) =
        QItem("${prefix}_${i + 1}", ar, speak, en, bn, listOf(speak))

    private fun words(prefix: String, vararg rows: Triple<String, String, String>) =
        rows.mapIndexed { i, (ar, en, bn) -> w(prefix, i, ar, en, bn) }

    private fun t(ar: String, en: String, bn: String) = Triple(ar, en, bn)

    // ---------------------------------------------------------------- generated drills

    private fun murakkabat(): List<QItem> = listOf(
        "lam" to "alif", "ba" to "ta", "tha" to "jeem", "hha" to "kha", "seen" to "sheen",
        "sad" to "dad", "tta" to "zza", "ain" to "ghain", "fa" to "qaf", "kaf" to "lam",
        "meem" to "noon", "ha" to "ya", "ba" to "seen", "ta" to "meem", "ya" to "lam",
        "noon" to "ba", "fa" to "ta", "kaf" to "meem", "ain" to "lam", "qaf" to "lam",
        "lam" to "meem", "ha" to "meem", "hha" to "ya", "jeem" to "ain",
    ).mapIndexed { i, (a, b) ->
        val x = byId.getValue(a); val y = byId.getValue(b)
        val speak = x.nameAr + " " + y.nameAr
        QItem("c_${i + 1}", x.char + y.char, speak, "${x.nameEn} ${y.nameEn}", "${x.nameBn} ${y.nameBn}", listOf(speak))
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
    ).mapIndexed { i, (display, speak, en, bn) -> QItem("m_${i + 1}", display, speak, en, bn, listOf(speak)) }

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

    /** Standing fatha, standing kasra and inverted damma: read like madd. */
    private fun standing() = letters.filter { it.id != "alif" && it.id != "hamza" }.flatMap { h ->
        V.entries.map { v ->
            val speak = h.char + v.mark + v.maddLetter
            QItem("k_${h.id}_${key(v)}", h.char + v.standing, speak, h.soundEn + v.enLong, bn(h, v, long = true) + "—",
                listOf(h.char + v.maddLetter, h.char), h)
        }
    }

    private fun madd() = letters.filter { it.id != "alif" && it.id != "hamza" }.flatMap { h ->
        V.entries.map { v ->
            val text = h.char + v.mark + v.maddLetter
            QItem("d_${h.id}_${key(v)}", text, text, h.soundEn + v.enLong, bn(h, v, long = true) + "—",
                listOf(h.char + v.maddLetter, h.char), h)
        }
    }

    private fun leen() = letters.filter { it.id != "alif" && it.id != "hamza" }.flatMap { h ->
        val fatha = V.A.mark
        listOf(
            QItem("n_${h.id}_y", h.char + fatha + "ي" + SUKUN, h.char + fatha + "ي" + SUKUN,
                h.soundEn + "ay", bn(h, V.A) + "ই", listOf(h.char + "ي"), h),
            QItem("n_${h.id}_w", h.char + fatha + "و" + SUKUN, h.char + fatha + "و" + SUKUN,
                h.soundEn + "aw", bn(h, V.A) + "উ", listOf(h.char + "و"), h),
        )
    }

    private fun sukoonDrill(prefix: String, source: List<Harf>) = source.flatMap { h ->
        V.entries.map { v ->
            val text = v.alifSeat + v.mark + h.char + SUKUN
            QItem("${prefix}_${h.id}_${key(v)}", text, text, v.en + h.soundEn, v.bnVowel + h.soundBn + "্",
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

    private fun waqf(): List<QItem> = listOf(
        listOf("أَحَدٌ", "أَحَدْ", "aḥadun → aḥad", "আহাদুন → আহাদ"),
        listOf("اَلرَّحِيمِ", "اَلرَّحِيمْ", "ar-raḥīmi → ar-raḥīm", "আর-রাহীমি → আর-রাহীম"),
        listOf("عَلِيمًا", "عَلِيمَا", "ʿalīman → ʿalīmā", "আলীমান → আলীমা"),
        listOf("رَحْمَةٌ", "رَحْمَهْ", "raḥmatun → raḥmah", "রাহমাতুন → রাহমাহ"),
        listOf("نَسْتَعِينُ", "نَسْتَعِينْ", "nastaʿīnu → nastaʿīn", "নাসতাঈনু → নাসতাঈন"),
        listOf("اَلْعَالَمِينَ", "اَلْعَالَمِينْ", "al-ʿālamīna → al-ʿālamīn", "আল-আলামীনা → আল-আলামীন"),
        listOf("أَبَدًا", "أَبَدَا", "abadan → abadā", "আবাদান → আবাদা"),
        listOf("جَنَّةٍ", "جَنَّهْ", "jannatin → jannah", "জান্নাতিন → জান্নাহ"),
    ).mapIndexed { i, (full, stop, en, bn) ->
        QItem("w_${i + 1}", "$full\n$stop", stop, en, bn, listOf(stop))
    }

    // ---------------------------------------------------------------- the 17 lessons

    val lessons: List<Lesson> by lazy {
        listOf(
            Lesson(1, "حُرُوفُ الْهِجَاءِ الْمُفْرَدَةُ",
                L("Single letters", "একক হরফ", "الحروف المفردة"),
                L("Pronounce the 29 letters with their correct names", "২৯টি হরফ সঠিক নামে উচ্চারণ", "نطق الحروف الهجائية بأسمائها الصحيحة"),
                "📖", 4, letters.map { letterItem(it) }),
            Lesson(2, "حُرُوفُ الْهِجَاءِ الْمُرَكَّبَةُ",
                L("Joined letters", "যুক্ত হরফ", "الحروف المركبة"),
                L("How letters look when they join together", "হরফ মিলে গেলে কেমন দেখায়", "معرفة أشكال الحروف عندما تتصل ببعضها"),
                "🧩", 3, murakkabat()),
            Lesson(3, "الْحُرُوفُ الْمُقَطَّعَةُ",
                L("Muqattaʿat letters", "হুরূফে মুকাত্তাআত", "الحروف المقطعة"),
                L("The letters that open some surahs, like Alif Laam Meem", "কিছু সূরার শুরুর হরফ, যেমন আলিফ লাম মীম", "أوائل السور مثل الم ويس وحم"),
                "✨", 2, muqattaat()),
            Lesson(4, "الْحَرَكَاتُ",
                L("Harakat", "হরকত", "الحركات"),
                L("Fatha, kasra and damma", "যবর, যের ও পেশ", "الفتحة والكسرة والضمة"),
                "🔤", 3, harakat()),
            Lesson(5, "التَّنْوِينُ",
                L("Tanween", "তানভীন", "التنوين"),
                L("Double fatha, double kasra and double damma", "দুই যবর, দুই যের ও দুই পেশ", "الفتحتان والكسرتان والضمتان"),
                "🌟", 3, tanween()),
            Lesson(6, "تَدْرِيبَاتٌ عَلَى الْحَرَكَاتِ وَالتَّنْوِينِ",
                L("Harakat and tanween practice", "হরকত ও তানভীন অনুশীলন", "تدريبات على الحركات والتنوين"),
                L("Reading real words with harakat and tanween", "হরকত ও তানভীনসহ শব্দ পড়া", "قراءة كلمات بحركات وتنوين متنوع"),
                "📚", 2, words("p6",
                    t("خَلَقَ", "khalaqa", "খালাক্বা"), t("كَتَبَ", "kataba", "কাতাবা"),
                    t("ذَهَبَ", "dhahaba", "যাহাবা"), t("فَتَحَ", "fataḥa", "ফাতাহা"),
                    t("جَعَلَ", "jaʿala", "জাআলা"), t("نَصَرَ", "naṣara", "নাসারা"),
                    t("عَلِمَ", "ʿalima", "আলিমা"), t("سَمِعَ", "samiʿa", "সামিআ"),
                    t("حَسِبَ", "ḥasiba", "হাসিবা"), t("كُتُبٌ", "kutubun", "কুতুবুন"),
                    t("رُسُلٌ", "rusulun", "রুসুলুন"), t("أَحَدٌ", "aḥadun", "আহাদুন"),
                    t("وَلَدٌ", "waladun", "ওয়ালাদুন"), t("قَمَرٌ", "qamarun", "ক্বামারুন"),
                    t("شَجَرٌ", "shajarun", "শাজারুন"), t("بَلَدًا", "baladan", "বালাদান"),
                    t("رَجُلٍ", "rajulin", "রাজুলিন"),
                )),
            Lesson(7, "الْأَلِفُ الصَّغِيرَةُ وَالْيَاءُ الصَّغِيرَةُ وَالْوَاوُ الصَّغِيرَةُ",
                L("Standing harakat", "খাড়া হরকত", "الحركات القائمة"),
                L("The small alif, yaa and waaw, read long", "খাড়া যবর, খাড়া যের ও উল্টা পেশ, টেনে পড়া", "الألف والياء والواو الصغيرة وحكم مدها حركتين"),
                "🌱", 3, standing()),
            Lesson(8, "حُرُوفُ الْمَدِّ",
                L("Madd letters", "মদের হরফ", "حروف المد"),
                L("Stretching the sound with alif, yaa and waaw", "আলিফ, ইয়া ও ওয়াও দিয়ে টেনে পড়া", "المد بالألف والياء والواو"),
                "🎵", 3, madd()),
            Lesson(9, "حُرُوفُ اللِّينِ",
                L("Leen letters", "লীনের হরফ", "حروف اللين"),
                L("Soft yaa and waaw after a fatha", "যবরের পর নরম ইয়া ও ওয়াও", "الياء والواو الساكنتان بعد فتح"),
                "🍃", 2, leen()),
            Lesson(10, "السُّكُونُ",
                L("Sukoon", "জযম (সুকূন)", "السكون"),
                L("Joining a letter that has no vowel", "স্বরবিহীন হরফ মিলিয়ে পড়া", "الحرف الساكن ووصله بما قبله"),
                "⏸️", 3, sukoonDrill("s", letters.filter { it.id !in noJoin })),
            Lesson(11, "تَدْرِيبَاتٌ عَلَى السُّكُونِ",
                L("Sukoon practice", "জযম অনুশীলন", "تدريبات على السكون"),
                L("Words with sukoon", "জযমসহ শব্দ", "كلمات فيها سكون"),
                "📝", 2, words("p11",
                    t("قُلْ", "qul", "ক্বুল"), t("لَمْ", "lam", "লাম"), t("مِنْ", "min", "মিন"),
                    t("عَنْ", "ʿan", "আন"), t("قَدْ", "qad", "ক্বাদ"), t("هَلْ", "hal", "হাল"),
                    t("كُنْ", "kun", "কুন"), t("يَلِدْ", "yalid", "ইয়ালিদ"), t("أَنْتَ", "anta", "আনতা"),
                    t("اَلْحَمْدُ", "al-ḥamdu", "আলহামদু"), t("يَعْلَمُ", "yaʿlamu", "ইয়া'লামু"),
                    t("اِقْرَأْ", "iqraʾ", "ইক্বরা"), t("مَسْجِدٌ", "masjidun", "মাসজিদুন"),
                    t("أَرْسَلْنَا", "arsalnā", "আরসালনা"),
                )),
            Lesson(12, "التَّشْدِيدُ",
                L("Tashdeed", "তাশদীদ", "التشديد"),
                L("Doubled letters, read with stress", "দ্বিত্ব হরফ জোর দিয়ে পড়া", "الحرف المشدد ونطقه"),
                "💪", 3, tashdeed()),
            Lesson(13, "تَدْرِيبَاتٌ عَلَى التَّشْدِيدِ",
                L("Tashdeed practice", "তাশদীদ অনুশীলন", "تدريبات على التشديد"),
                L("Words with tashdeed", "তাশদীদসহ শব্দ", "كلمات فيها تشديد"),
                "🏋️", 2, words("p13",
                    t("رَبَّنَا", "rabbanā", "রাব্বানা"), t("إِنَّ", "inna", "ইন্না"),
                    t("ثُمَّ", "thumma", "ছুম্মা"), t("كَلَّا", "kallā", "কাল্লা"),
                    t("حَقًّا", "ḥaqqan", "হাক্কান"), t("أُمِّ", "ummi", "উম্মি"),
                    t("مُحَمَّدٌ", "muḥammadun", "মুহাম্মাদুন"), t("اَلرَّحْمٰنِ", "ar-raḥmāni", "আর-রাহমানি"),
                    t("اَلرَّحِيمِ", "ar-raḥīmi", "আর-রাহীমি"), t("إِيَّاكَ", "iyyāka", "ইয়্যাকা"),
                    t("اَللّٰهُ", "allāhu", "আল্লাহু"), t("يُكَذِّبُ", "yukadhdhibu", "ইউকায্যিবু"),
                )),
            Lesson(14, "أَحْكَامُ النُّونِ السَّاكِنَةِ وَالتَّنْوِينِ",
                L("Noon sakin and tanween", "নূন সাকিন ও তানভীন", "أحكام النون الساكنة والتنوين"),
                L("Iẓhār, idghām, iqlāb and ikhfāʾ with examples", "ইযহার, ইদগাম, ইক্লাব ও ইখফা উদাহরণসহ", "الإظهار والإدغام والإقلاب والإخفاء"),
                "👃", 1, words("p14",
                    t("مِنْ هَادٍ", "Iẓhār: min hādin", "ইযহার: মিন হাদিন"),
                    t("مَنْ آمَنَ", "Iẓhār: man āmana", "ইযহার: মান আমানা"),
                    t("مَنْ يَقُولُ", "Idghām: may-yaqūlu", "ইদগাম: মাই-ইয়াক্বূলু"),
                    t("مِنْ رَبِّهِمْ", "Idghām: mir-rabbihim", "ইদগাম: মির-রাব্বিহিম"),
                    t("مِنْ بَعْدِ", "Iqlāb: mim-baʿdi", "ইক্লাব: মিম-বা'দি"),
                    t("سَمِيعٌ بَصِيرٌ", "Iqlāb: samīʿum-baṣīr", "ইক্লাব: সামীউম-বাসীর"),
                    t("مِنْ تَحْتِهَا", "Ikhfāʾ: min taḥtihā", "ইখফা: মিন তাহতিহা"),
                    t("أَنْفُسَكُمْ", "Ikhfāʾ: anfusakum", "ইখফা: আনফুসাকুম"),
                )),
            Lesson(15, "أَحْكَامُ الْمِيمِ السَّاكِنَةِ",
                L("Meem sakin", "মীম সাকিন", "أحكام الميم الساكنة"),
                L("Ikhfāʾ, idghām and iẓhār shafawī", "ইখফা, ইদগাম ও ইযহার শাফাবী", "الإخفاء والإدغام والإظهار الشفوي"),
                "👄", 1, words("p15",
                    t("تَرْمِيهِمْ بِحِجَارَةٍ", "Ikhfāʾ shafawī: tarmīhim biḥijāratin", "ইখফা শাফাবী: তারমীহিম বিহিজারাতিন"),
                    t("وَهُمْ بِالْآخِرَةِ", "Ikhfāʾ shafawī: wa hum bil-ākhirah", "ইখফা শাফাবী: ওয়া হুম বিল-আখিরাহ"),
                    t("لَهُمْ مَا", "Idghām shafawī: lahum-mā", "ইদগাম শাফাবী: লাহুম-মা"),
                    t("كُنْتُمْ مُؤْمِنِينَ", "Idghām shafawī: kuntum-muʾminīn", "ইদগাম শাফাবী: কুনতুম-মু'মিনীন"),
                    t("أَلَمْ تَرَ", "Iẓhār shafawī: alam tara", "ইযহার শাফাবী: আলাম তারা"),
                    t("هُمْ فِيهَا", "Iẓhār shafawī: hum fīhā", "ইযহার শাফাবী: হুম ফীহা"),
                )),
            Lesson(16, "الْقَلْقَلَةُ",
                L("Qalqalah", "ক্বলক্বলা", "القلقلة"),
                L("The bouncing letters ق ط ب ج د", "প্রতিধ্বনির হরফ ق ط ب ج د", "حروف القلقلة: قطب جد"),
                "🔔", 3, sukoonDrill("q", listOf("qaf", "tta", "ba", "jeem", "dal").map { byId.getValue(it) })),
            Lesson(17, "الْوَقْفُ",
                L("Waqf (stopping)", "ওয়াকফ (থামা)", "الوقف"),
                L("How a word changes when you stop on it", "শব্দের শেষে থামলে কেমন পড়তে হয়", "الوقف على أواخر الكلمات"),
                "✋", 2, waqf()),
        )
    }
}
