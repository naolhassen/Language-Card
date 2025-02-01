package com.naol.languagecard

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playAudioSequence(context: Context, wordAudioFile: String, sentenceAudioFile: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                val wordFileDescriptor = context.assets.openFd("audio/$wordAudioFile")
                setDataSource(
                    wordFileDescriptor.fileDescriptor,
                    wordFileDescriptor.startOffset,
                    wordFileDescriptor.length
                )
                prepareAsync()
                setOnPreparedListener {
                    start()
                }
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    playSentenceAudio(context, sentenceAudioFile)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error playing word audio: $wordAudioFile", e)
                playSentenceAudio(context, sentenceAudioFile)
            }
        }
    }

    private fun playSentenceAudio(context: Context, sentenceAudioFile: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                val sentenceFileDescriptor = context.assets.openFd("audio/$sentenceAudioFile")
                setDataSource(
                    sentenceFileDescriptor.fileDescriptor,
                    sentenceFileDescriptor.startOffset,
                    sentenceFileDescriptor.length
                )
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error playing sentence audio: $sentenceAudioFile", e)
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}