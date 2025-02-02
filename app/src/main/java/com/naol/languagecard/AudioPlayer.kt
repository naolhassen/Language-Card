package com.naol.languagecard

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    var isAudioLoading: Boolean = false
        private set

    fun playAudioSequence(
        context: Context,
        wordAudioFile: String,
        sentenceAudioFile: String,
        onLoading: (Boolean) -> Unit
    ) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                isAudioLoading = true
                onLoading(true)

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
                    playSentenceAudio(context, sentenceAudioFile, onLoading)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error playing word audio: $wordAudioFile", e)
                playSentenceAudio(context, sentenceAudioFile, onLoading)
            }
        }
    }

    private fun playSentenceAudio(
        context: Context,
        sentenceAudioFile: String,
        onLoading: (Boolean) -> Unit
    ) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                isAudioLoading = true
                onLoading(true)
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
//        isAudioLoading = false
    }
}