package com.example.audio

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAudioFileScanner(private val context: Context) {

    suspend fun scanLocalAudioFiles(): List<MusicTrack> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<MusicTrack>()
        val collection: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown MP3"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val durationMs = cursor.getLong(durationColumn)
                    val sizeBytes = cursor.getLong(sizeColumn)
                    val path = cursor.getString(dataColumn) ?: ""

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val durationSec = (durationMs / 1000).toInt().coerceAtLeast(1)
                    val sizeMb = sizeBytes / (1024.0 * 1024.0)

                    audioList.add(
                        MusicTrack(
                            id = "mp3_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Local File" else artist,
                            category = "Local MP3",
                            bpm = 120,
                            durationSeconds = durationSec,
                            description = "Local Audio File • Path: $path",
                            primaryFreq = 440.0,
                            chordFreqs = emptyList(),
                            beatStyle = "LOCAL_MP3",
                            contentUri = contentUri.toString(),
                            filePath = path,
                            fileSizeMb = sizeMb
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        audioList
    }
}
