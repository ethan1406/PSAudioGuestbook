package com.trufflear.pswedding.service

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.AssetDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import javax.inject.Inject


class PlaybackService @Inject constructor(): MediaSessionService() {

    private var mediaSession: MediaSession? = null

    // Create your Player and MediaSession in the onCreate lifecycle event
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, getMediaPlayer())
            .setCallback(
                object : MediaSession.Callback {
                    override fun onMediaButtonEvent(
                        session: MediaSession,
                        controllerInfo: MediaSession.ControllerInfo,
                        intent: Intent
                    ): Boolean {
                        Log.d("testing 123", "button click received")
                        return true
                    }
                }
            )
            .build()
    }

    @OptIn(UnstableApi::class)
    private fun getMediaPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(this).build()
        val mediaSource = ProgressiveMediaSource.Factory { AssetDataSource(this) }
            .createMediaSource(MediaItem.fromUri(Uri.parse("asset:///rain.mp3")))
        player.setMediaSource(mediaSource)
        player.prepare()

        return player
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession?.player?.play()
        return super.onStartCommand(intent, flags, startId)
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
        super.onDestroy()
    }
}