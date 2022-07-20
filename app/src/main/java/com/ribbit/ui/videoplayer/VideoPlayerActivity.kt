package com.ribbit.ui.videoplayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ribbit.R
import com.ribbit.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_video_player.*

class VideoPlayerActivity : BaseActivity() {
    companion object {
        private const val EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH"

        fun start(context: Context, videoPath: String) {
            context.startActivity(Intent(context, VideoPlayerActivity::class.java)
                    .putExtra(EXTRA_VIDEO_PATH, videoPath))
        }
    }

    private val videoPath by lazy { intent.getStringExtra(EXTRA_VIDEO_PATH) }

    private var startWindow = C.INDEX_UNSET
    private var startPosition = C.TIME_UNSET

    private var exoPlayer: SimpleExoPlayer? = null
    override fun onSavedInstance(outState: Bundle?, outPersisent: PersistableBundle?) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)), bandwidthMeter)
        return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            val player = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
            exoPlayer = player
            playerView.player = exoPlayer
            val mediaSource = buildMediaSource(Uri.parse(videoPath))
            player.playWhenReady = true

            val haveStartPosition = startWindow != C.INDEX_UNSET
            if (haveStartPosition) {
                player.seekTo(startWindow, startPosition)
            }
            player.prepare(mediaSource, !haveStartPosition, false)
        }
    }

    private fun releasePlayer() {
        val player = exoPlayer
        if (player != null) {
            startPosition = player.currentPosition
            startWindow = player.currentWindowIndex
            player.release()
            exoPlayer = null
        }
    }

    override
    fun onResume() {
        super.onResume()
        if ((Util.SDK_INT <= 23)) {
            initializePlayer()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }
}