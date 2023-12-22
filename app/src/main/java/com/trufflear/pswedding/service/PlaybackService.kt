package com.trufflear.pswedding.service

import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.AssetDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class Experience {
    Idle,
    PSMessage,
    Recording;
}

@AndroidEntryPoint
class PlaybackService @Inject constructor(): MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var experience = Experience.Idle
    private var mediaRecorder: MediaRecorder? = null

    private lateinit var baseFile: File

    // Create your Player and MediaSession in the onCreate lifecycle event
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        baseFile = File(filesDir, "guestRecordings")

        mediaSession = MediaSession.Builder(this, getMediaPlayer())
            .setCallback(
                object : MediaSession.Callback {
                    override fun onMediaButtonEvent(
                        session: MediaSession,
                        controllerInfo: MediaSession.ControllerInfo,
                        intent: Intent
                    ): Boolean {
                        Log.d("boagan", "button click received")
                        if (experience != Experience.PSMessage) {
                            updateExperience()
                            handleExperience(experience)
                        }
                        return true
                    }


                }
            )
            .build()

        mediaRecorder = MediaRecorder(this)

        startPlayer("silence", true)
    }

    private fun getRecordingPath(): File {
        if (baseFile.exists().not()) {
            if (baseFile.mkdir().not()) {
                Log.e("boagan", "file failed to create")
            }
        }
        return File(baseFile, "${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4")
    }

    private fun handleExperience(experience: Experience) {
        when (experience) {
            Experience.PSMessage -> {
                startPlayer("PSRecording", false)
                setMediaRecorder()
            }
            Experience.Recording -> {
                mediaSession?.player?.pause()
                startRecording()
            }
            Experience.Idle -> {
                recordAndReset()
                Log.i("boagan", "successfully recordeded ")
                startPlayer("silence", true)
            }
        }
    }

    private fun updateExperience() {
        experience = when (experience) {
            Experience.Idle -> Experience.PSMessage
            Experience.PSMessage -> Experience.Recording
            Experience.Recording -> Experience.Idle
        }
    }

    private fun setMediaRecorder() {
        mediaRecorder?.run {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(getRecordingPath())
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            setAudioEncodingBitRate(16 * 44100)
            setAudioSamplingRate(44100)
            setMaxDuration(10_000) // 60 seconds

            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.d("boagan","Maximum Duration Reached and video saved")
                    if (experience == Experience.Recording) {
                        updateExperience()
                        handleExperience(experience)
                    }
                }
            }
        }
    }

    private fun startRecording() =
        mediaRecorder?.run {
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("boagan", "media recorder starting failed")
            }
        }
    private fun recordAndReset() {
        if (experience == Experience.Idle) {
            mediaRecorder?.run {
                stop()
                reset()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun getMediaPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(this).build()
        val mediaSource = ProgressiveMediaSource.Factory { AssetDataSource(this) }
            .createMediaSource(MediaItem.fromUri(Uri.parse("asset:///silence.mp3")))
        player.setMediaSource(mediaSource)
        player.repeatMode = ExoPlayer.REPEAT_MODE_ONE

        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        if (experience == Experience.PSMessage) {
                            updateExperience()
                            handleExperience(experience)
                        }
                    }
                }
            }
        )

        return player
    }

    private fun startPlayer(filename: String, shouldRepeat: Boolean) =
        mediaSession?.player?.run {
            setMediaItem(MediaItem.fromUri(Uri.parse("asset:///$filename.mp3")))
            seekTo(0)

            repeatMode = if (shouldRepeat) {
                ExoPlayer.REPEAT_MODE_ONE
            } else {
                ExoPlayer.REPEAT_MODE_OFF
            }

            prepare()
            play()
        }

    // This example always accepts the connection request
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.stop()
            player.release()
            release()
            mediaSession = null
        }
        mediaRecorder?.run {
            reset()
            release()
            mediaRecorder = null
        }
        super.onDestroy()
    }
}