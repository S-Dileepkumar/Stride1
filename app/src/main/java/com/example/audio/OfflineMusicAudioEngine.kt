package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val category: String, // "Workout Beat", "Speech Focus", "Chill Lo-Fi", "Local MP3"
    val bpm: Int,
    val durationSeconds: Int = 180,
    val description: String,
    val primaryFreq: Double = 440.0,
    val chordFreqs: List<Double> = emptyList(),
    val beatStyle: String, // "CADENCE", "AMBIENT", "UPTEMPO", "ZEN", "LOFI", "LOCAL_MP3"
    val contentUri: String? = null,
    val filePath: String? = null,
    val fileSizeMb: Double = 0.0
)

data class MusicPlayerState(
    val currentTrack: MusicTrack? = null,
    val isPlaying: Boolean = false,
    val currentPositionSeconds: Int = 0,
    val durationSeconds: Int = 180,
    val volume: Float = 0.8f,
    val isLooping: Boolean = true,
    val tempoMultiplier: Float = 1.0f // 0.8x, 1.0x, 1.25x, 1.5x
)

class OfflineMusicAudioEngine {

    companion object {
        val DEFAULT_SYNTH_TRACKS = listOf(
            MusicTrack(
                id = "track_power_stride",
                title = "Power Stride Cadence",
                artist = "Stride & Speak Rhythm",
                category = "Workout Beat",
                bpm = 120,
                durationSeconds = 180,
                description = "120 BPM energetic rhythm beat tailored for brisk walking pace.",
                primaryFreq = 220.0,
                chordFreqs = listOf(220.0, 277.18, 329.63, 440.0),
                beatStyle = "CADENCE"
            ),
            MusicTrack(
                id = "track_speech_focus",
                title = "Alpha Wave Speech Prep",
                artist = "Mindful Vocal Focus",
                category = "Speech Focus",
                bpm = 80,
                durationSeconds = 240,
                description = "432 Hz calming binaural ambient chime harmonics for outlining speeches.",
                primaryFreq = 216.0,
                chordFreqs = listOf(216.0, 270.0, 324.0, 432.0),
                beatStyle = "AMBIENT"
            ),
            MusicTrack(
                id = "track_cardio_tempo",
                title = "Cardio Tempo Drive",
                artist = "High Energy Beats",
                category = "Workout Beat",
                bpm = 135,
                durationSeconds = 180,
                description = "135 BPM driving uptempo synth kick beat for running and jogging.",
                primaryFreq = 261.63,
                chordFreqs = listOf(261.63, 329.63, 392.00, 523.25),
                beatStyle = "UPTEMPO"
            ),
            MusicTrack(
                id = "track_zen_bells",
                title = "Zen Meditation Bells",
                artist = "Mindful Vocal Focus",
                category = "Chill & Zen",
                bpm = 60,
                durationSeconds = 300,
                description = "Deep grounding sine frequencies with periodic resonant gong intervals.",
                primaryFreq = 174.0,
                chordFreqs = listOf(174.0, 220.0, 261.63, 348.0),
                beatStyle = "ZEN"
            ),
            MusicTrack(
                id = "track_lofi_groove",
                title = "Evening Practice Lo-Fi",
                artist = "Stride & Speak Rhythm",
                category = "Chill Lo-Fi",
                bpm = 90,
                durationSeconds = 210,
                description = "Mellow warm rhodes synth harmony for relaxed speech review.",
                primaryFreq = 196.0,
                chordFreqs = listOf(196.0, 246.94, 293.66, 392.0),
                beatStyle = "LOFI"
            )
        )
    }

    private val _allTracksList = MutableStateFlow<List<MusicTrack>>(DEFAULT_SYNTH_TRACKS)
    val allTracksList: StateFlow<List<MusicTrack>> = _allTracksList.asStateFlow()

    private val _playerState = MutableStateFlow(
        MusicPlayerState(currentTrack = DEFAULT_SYNTH_TRACKS[0], durationSeconds = DEFAULT_SYNTH_TRACKS[0].durationSeconds)
    )
    val playerState: StateFlow<MusicPlayerState> = _playerState.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: android.media.MediaPlayer? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun updateScannedTracks(localMp3Tracks: List<MusicTrack>) {
        val merged = DEFAULT_SYNTH_TRACKS + localMp3Tracks
        _allTracksList.value = merged
    }

    fun selectTrack(track: MusicTrack, context: android.content.Context? = null) {
        val wasPlaying = _playerState.value.isPlaying
        stopPlayback()
        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            currentPositionSeconds = 0,
            durationSeconds = track.durationSeconds
        )
        if (wasPlaying) {
            play(context)
        }
    }

    fun play(context: android.content.Context? = null) {
        if (_playerState.value.isPlaying) return
        val track = _playerState.value.currentTrack ?: _allTracksList.value.firstOrNull() ?: DEFAULT_SYNTH_TRACKS[0]

        _playerState.value = _playerState.value.copy(isPlaying = true)

        playbackJob = scope.launch {
            if (track.beatStyle == "LOCAL_MP3" && !track.contentUri.isNullOrEmpty() && context != null) {
                runMediaPlayerLoop(context, track)
            } else {
                runAudioLoop(track)
            }
        }
    }

    fun pause() {
        _playerState.value = _playerState.value.copy(isPlaying = false)
        stopPlayback()
    }

    fun togglePlayPause(context: android.content.Context? = null) {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            play(context)
        }
    }

    fun seekTo(seconds: Int) {
        val duration = _playerState.value.durationSeconds
        val clamped = seconds.coerceIn(0, duration)
        _playerState.value = _playerState.value.copy(currentPositionSeconds = clamped)

        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(clamped * 1000)
            } catch (_: Exception) {}
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _playerState.value = _playerState.value.copy(volume = clamped)
        audioTrack?.setVolume(clamped)
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun toggleLoop() {
        val newLoop = !_playerState.value.isLooping
        _playerState.value = _playerState.value.copy(isLooping = newLoop)
        mediaPlayer?.isLooping = newLoop
    }

    fun setTempoMultiplier(multiplier: Float) {
        _playerState.value = _playerState.value.copy(tempoMultiplier = multiplier)
        mediaPlayer?.let { mp ->
            try {
                val params = mp.playbackParams
                params.speed = multiplier
                mp.playbackParams = params
            } catch (_: Exception) {}
        }
    }

    fun nextTrack(context: android.content.Context? = null) {
        val current = _playerState.value.currentTrack ?: return
        val tracks = _allTracksList.value
        val currentIndex = tracks.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % tracks.size
        selectTrack(tracks[nextIndex], context)
    }

    fun previousTrack(context: android.content.Context? = null) {
        val current = _playerState.value.currentTrack ?: return
        val tracks = _allTracksList.value
        val currentIndex = tracks.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex <= 0) tracks.size - 1 else currentIndex - 1
        selectTrack(tracks[prevIndex], context)
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null

        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (_: Exception) {}
        audioTrack = null

        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    private suspend fun runMediaPlayerLoop(context: android.content.Context, track: MusicTrack) {
        try {
            val uri = android.net.Uri.parse(track.contentUri)
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(context, uri)
                prepare()
                setVolume(_playerState.value.volume, _playerState.value.volume)
                isLooping = _playerState.value.isLooping
                start()
            }

            val durMs = mediaPlayer?.duration ?: (track.durationSeconds * 1000)
            val durSec = (durMs / 1000).coerceAtLeast(1)
            _playerState.value = _playerState.value.copy(durationSeconds = durSec)

            while (_playerState.value.isPlaying && mediaPlayer?.isPlaying == true) {
                val curMs = mediaPlayer?.currentPosition ?: 0
                _playerState.value = _playerState.value.copy(currentPositionSeconds = curMs / 1000)
                kotlinx.coroutines.delay(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _playerState.value = _playerState.value.copy(isPlaying = false)
        }
    }

    private suspend fun runAudioLoop(track: MusicTrack) {
        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        val trackAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormatObj = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(trackAttr)
            .setAudioFormat(audioFormatObj)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(_playerState.value.volume)
        audioTrack?.play()

        val pcmBuffer = ShortArray(4410 * 2) // 100ms stereo chunks
        var totalSamplesWritten = 0L

        while (_playerState.value.isPlaying) {
            val tempo = _playerState.value.tempoMultiplier
            val effectiveBpm = track.bpm * tempo

            generatePCMChunk(
                buffer = pcmBuffer,
                startSampleIndex = totalSamplesWritten,
                sampleRate = sampleRate,
                bpm = effectiveBpm,
                track = track
            )

            audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
            totalSamplesWritten += pcmBuffer.size / 2

            // Advance playback position counter
            val currentSec = (totalSamplesWritten / sampleRate).toInt()
            if (currentSec != _playerState.value.currentPositionSeconds) {
                val duration = _playerState.value.durationSeconds
                if (currentSec >= duration) {
                    if (_playerState.value.isLooping) {
                        totalSamplesWritten = 0L
                        _playerState.value = _playerState.value.copy(currentPositionSeconds = 0)
                    } else {
                        nextTrack()
                        break
                    }
                } else {
                    _playerState.value = _playerState.value.copy(currentPositionSeconds = currentSec)
                }
            }
        }
    }

    private fun generatePCMChunk(
        buffer: ShortArray,
        startSampleIndex: Long,
        sampleRate: Int,
        bpm: Float,
        track: MusicTrack
    ) {
        val samplesPerBeat = (sampleRate * 60f / bpm).toDouble()
        val numChannels = 2

        for (i in 0 until buffer.size / numChannels) {
            val globalSample = startSampleIndex + i
            val timeSec = globalSample.toDouble() / sampleRate
            val beatPosition = (globalSample % samplesPerBeat) / samplesPerBeat

            var leftVal = 0.0
            var rightVal = 0.0

            when (track.beatStyle) {
                "CADENCE", "UPTEMPO" -> {
                    // Kick Drum on beat start
                    val kickEnvelope = Math.exp(-beatPosition * 12.0)
                    val kickFreq = 120.0 * Math.exp(-beatPosition * 20.0) + 45.0
                    val kickTone = sin(2.0 * PI * kickFreq * timeSec) * kickEnvelope * 0.55

                    // Hi-hat noise burst on offbeats
                    val isOffbeat = beatPosition in 0.45..0.55
                    val hatVal = if (isOffbeat) (Math.random() - 0.5) * 0.15 * Math.exp(-(beatPosition - 0.5) * 20.0) else 0.0

                    // Chord Harmony
                    val chordIndex = ((timeSec / 2.0).toInt()) % track.chordFreqs.size
                    val chordFreq = track.chordFreqs[chordIndex]
                    val synthEnvelope = 0.25 + 0.1 * sin(2.0 * PI * 0.5 * timeSec)
                    val synthTone = sin(2.0 * PI * chordFreq * timeSec) * synthEnvelope * 0.2

                    leftVal = kickTone + hatVal + synthTone
                    rightVal = kickTone - hatVal + synthTone
                }

                "AMBIENT" -> {
                    // Smooth binaural ambient sine wave harmonic
                    val chordIndex = ((timeSec / 4.0).toInt()) % track.chordFreqs.size
                    val freqL = track.chordFreqs[chordIndex]
                    val freqR = freqL + 3.0 // 3Hz binaural beat offset for focus

                    val toneL = sin(2.0 * PI * freqL * timeSec) * 0.25
                    val toneR = sin(2.0 * PI * freqR * timeSec) * 0.25

                    val shimmer = sin(2.0 * PI * (freqL * 2.0) * timeSec) * 0.05

                    leftVal = toneL + shimmer
                    rightVal = toneR + shimmer
                }

                "ZEN" -> {
                    // Deep grounding frequency + periodic bell gong every 4 seconds
                    val baseTone = sin(2.0 * PI * track.primaryFreq * timeSec) * 0.2
                    val bellCycle = timeSec % 4.0
                    val bellEnvelope = Math.exp(-bellCycle * 1.5)
                    val bellGong = sin(2.0 * PI * 528.0 * timeSec) * bellEnvelope * 0.35

                    leftVal = baseTone + bellGong
                    rightVal = baseTone + bellGong
                }

                "LOFI" -> {
                    // Warm rhodes chord synth + mellow soft pulse
                    val chordIndex = ((timeSec / 3.0).toInt()) % track.chordFreqs.size
                    val freq = track.chordFreqs[chordIndex]
                    val rhodesTone = sin(2.0 * PI * freq * timeSec) * 0.22 +
                            sin(2.0 * PI * (freq * 1.5) * timeSec) * 0.08
                    val softPulse = sin(2.0 * PI * (bpm / 60.0) * timeSec) * 0.06

                    leftVal = rhodesTone + softPulse
                    rightVal = rhodesTone - softPulse
                }
            }

            // Convert double (-1.0 to +1.0) to 16-bit PCM Short (-32768 to 32767)
            val leftShort = (leftVal.coerceIn(-0.95, 0.95) * 32767.0).toInt().toShort()
            val rightShort = (rightVal.coerceIn(-0.95, 0.95) * 32767.0).toInt().toShort()

            buffer[i * 2] = leftShort
            buffer[i * 2 + 1] = rightShort
        }
    }
}
