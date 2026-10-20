# Noorani Qaida (Al-Qaida an-Nuraniyya) for Android

An interactive Qur'an-reading primer. Tap any harf to hear it, say it back to have
your pronunciation checked, and practise writing letters with on-device handwriting AI.
UI in English and Bangla (switch from the top bar; follows Android 13+ per-app language too).

## Features
- 7 lessons: single letters, muqatta'at, harakat, tanween, madd, sukoon, tashdeed
  (about 440 tappable items, generated from the 29 letters so content costs only a few KB)
- Tap to pronounce; "Play all" reads a whole lesson with highlighting; slow mode for beginners
- English and Bangla transliteration for every item, plus each letter's makhraj
- Grids flow right to left, like a printed Qaida

## The ML pieces (all open / free, nothing large bundled)
| Feature | Engine | APK cost |
|---|---|---|
| Pronunciation (tap to hear) | Android TextToSpeech, Arabic neural voice (Google Speech Services) | 0 MB |
| Pronunciation check ("Say it") | Android `SpeechRecognizer`, `ar-SA`, with expected-answer biasing on Android 13+ | 0 MB |
| Handwriting check | ML Kit Digital Ink Recognition, Arabic model downloaded once on first use | small library, model ~20 MB downloaded later |

Honest limits: speech recognisers are trained on words and sentences, so isolated
syllables like بَ are sometimes misheard. The check is deliberately lenient and shows
what was heard. Text-to-speech also isn't a qari: for teaching-grade audio, see below.

## Build
1. Open the `NooraniQaida` folder in Android Studio (Koala or newer). It reads
   `gradle/wrapper/gradle-wrapper.properties` and downloads Gradle 8.7; if asked, let it
   create the wrapper.
2. Run on a device with Google Speech Services (most Android phones).
3. On the device, make sure the Arabic TTS voice is installed (the app shows an
   "Install voice" button if it isn't) and Arabic is added under voice typing for "Say it".

Release build: `./gradlew bundleRelease` (R8 + resource shrinking are on). Publish as
an App Bundle so Google Play delivers only the CPU-specific native code each phone needs.
Replace the debug signing config in `app/build.gradle.kts` before publishing.

## Size choices
- No bundled audio, fonts, or speech models; system Arabic and Bangla fonts are used
- R8 minify + `shrinkResources`, resources limited to `en` and `bn`
- No navigation, image or networking libraries
- Language splits disabled in the bundle so in-app English/Bangla switching always works

## Optional: real recitation audio
Drop mono Opus clips into `app/src/main/assets/audio/` named by item id and the app plays
them instead of TTS (about 3-6 KB per clip, roughly 2-3 MB for the full course):

    ffmpeg -i ba.wav -ac 1 -c:a libopus -b:a 24k l_ba.ogg

Ids: letters `l_<letter>` (l_alif, l_ba, ... l_ya), muqatta'at `m_1`..`m_14`,
harakat `h_<letter>_a|i|u`, tanween `t_...`, madd `d_...`, sukoon `s_...`,
tashdeed `sh_...`. Letter ids are listed in `data/QaidaData.kt`.
Only use recordings you have rights to.

## Ideas for next versions
- Murakkabat (joined letters) lesson and the noon sakin/tanween rules lesson
- Progress tracking per lesson
- Stronger offline pronunciation scoring with a Whisper model fine-tuned on Qur'anic
  recitation (for example ones published on Hugging Face), delivered as an on-demand
  Play Feature module so the base app stays small
