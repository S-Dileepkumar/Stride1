# Stride & Speak

[![Android Studio](https://img.shields.io/badge/Android%20Studio-2024.1+-brightgreen.svg)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Gemini API](https://img.shields.io/badge/Gemini%20API-Server--Side-orange.svg)](https://ai.google.dev)

**Stride & Speak** is an Android application combining hardware sensor step tracking with AI-powered speech practice analysis. Designed for active professionals, public speakers, and fitness enthusiasts, Stride & Speak helps users track their physical activity and hone their public speaking skills simultaneously.

---

## 🌟 Key Features

### 🏃 Step & Activity Tracking
* **Real-time Pedometer**: Uses Android hardware step sensors and fallback accelerometer algorithms to track steps accurately.
* **Active Walk/Run Sessions**: Record workouts with real-time tracking of step count, distance, pace, and estimated calorie burn.
* **Customizable Goals**: Adjustable daily step targets and stride length settings (metric & imperial units).

### 🎙️ AI Speech Training Studio
* **Voice Practice Recording**: Integrated audio recorder for practicing speeches, presentations, or interview answers.
* **Gemini AI Analysis**: Automatically transcribes speech, measures speaking pace, detects filler words (*"um"*, *"ah"*, *"like"*), and generates personalized feedback.
* **Filler Word Trend Tracking**: Monitor improvement over time with interactive speech analytics charts.

### 🎵 Rhythm & Offline Music Player
* **Stride-Synced Audio Beats**: Built-in rhythmic audio beats to help maintain a steady cadence while walking or running.
* **Device MP3 File Scanner**: Scans local device storage for user audio files with an offline mini-player and full-screen player dialog.
* **Glassmorphic Floating Player**: Non-intrusive floating audio player available across all app screens.

### 📊 Analytics & Consistency Heatmap
* **Activity Heatmap**: GitHub-style 30-day calendar heatmap visualizing daily step achievements and speech practice sessions.
* **Streak Tracking**: Separate streak counters for physical movement and speech practice consistency.
* **Weekly Trend Charts**: Custom Compose bar charts displaying step distribution and filler word statistics.

### ✨ Glassmorphic Modern UI
* **Material Design 3**: Modern UI with frosted glass translucency, subtle border outlines, and edge-to-edge layout support.
* **Full App Scrollability**: Every screen and dialog supports smooth vertical scrolling for seamless navigation on all screen sizes.
* **Light & High-Contrast Dark Modes**: Built-in theme switching tailored for low-light evening workouts or daylight viewing.

---

## 🛠️ Architecture & Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
* **Architecture**: Clean Architecture / MVVM pattern (`ViewModel`, `StateFlow`, `collectAsStateWithLifecycle`)
* **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) for persistent step logs, active workout sessions, and speech recordings
* **AI Integration**: Server-Side [Google Gemini API](https://ai.google.dev/) for intelligent natural language speech transcription and feedback
* **Concurrency**: Kotlin Coroutines & Flow
* **Audio Engine**: Android `AudioTrack` synthesizer and `MediaPlayer` API
* **Sensors**: Android Hardware `SensorManager` (`TYPE_STEP_COUNTER` & `TYPE_ACCELEROMETER`)

---

## 🚀 Getting Started

### Prerequisites
* Android Studio Ladybug (2024.2.1) or newer
* JDK 17 or higher
* Android device or emulator running Android 8.0 (API Level 26) or higher

### Building & Running
1. Clone or download the project repository.
2. Open the project folder in **Android Studio**.
3. Allow Gradle to sync dependencies automatically.
4. Run the app on a physical device or emulator via the **Run** menu (`Shift + F10`).

---

## 🔑 Environment & Secrets

If using Gemini AI speech transcription features, ensure your Gemini API key is configured in the **Secrets** panel or passed via environment configuration (`GEMINI_API_KEY`). The key is injected safely via `BuildConfig` at runtime.

---

## 📄 License

This project is built for demonstration and personal fitness/speech practice purposes.
