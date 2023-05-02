package com.example.tappintegration

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.tappintegration.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.tappp.library.context.TapppContext
import com.tappp.library.model.PanelSettingModel
import com.tappp.library.view.TapppPanel
import org.json.JSONObject

class MainActivity : AppCompatActivity(), Player.Listener {
    private lateinit var binding : ActivityMainBinding
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true

    private lateinit var exoPlayer: ExoPlayer
//    private lateinit var exoPlayerView: PlayerView
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var frameContainer: FrameLayout

    private val BroadCastName: String = TapppContext.Sports.TRN
    private var GameID = "cb0403c8-0f3c-4778-8d26-c4a63329678b"
    private var UserID = "cf9bb061-a040-4f43-9165-dac3adfb4258"


    companion object {
        var HLS_STATIC_URL =
            "https://sandbox-tappp.s3.us-east-2.amazonaws.com/content/videos/full_UTAHvTOR_480.mp4"
        const val STATE_RESUME_WINDOW = "resumeWindow"
        const val STATE_RESUME_POSITION = "resumePosition"
        const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
        const val STATE_PLAYER_PLAYING = "playerOnPlay"
    }

    private lateinit var tapppPanel: TapppPanel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exoPlayerView = binding.exPlayer
        frameContainer = binding.fragmentContainer

        savedInstanceState?.run {
            currentWindow = getInt(STATE_RESUME_WINDOW)
            playbackPosition = getLong(STATE_RESUME_POSITION)
            isFullscreen = getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = getBoolean(STATE_PLAYER_PLAYING)
        }

        initPlayer()
    }
    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(applicationContext).build()
        exoPlayerView.player = exoPlayer
        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(HLS_STATIC_URL)
            .setMimeType(if (HLS_STATIC_URL.contains(".mp4")) MimeTypes.APPLICATION_MP4 else MimeTypes.APPLICATION_M3U8)
            .build()
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        exoPlayerView.player!!.addListener(this)

        initPanel()
    }
    private fun initPanel() {
        val panelSetting = PanelSettingModel()
        panelSetting.supportManager = supportFragmentManager
        panelSetting.panelView = frameContainer

        val panelData = JSONObject()
        panelData.put(TapppContext.Sports.GAME_ID, GameID)
        panelData.put(TapppContext.Sports.BROADCASTER_NAME, BroadCastName)
        panelData.put(TapppContext.User.USER_ID, UserID)

        val tapppContext = JSONObject()
        tapppContext.put(TapppContext.Sports.Context, panelData)

        tapppPanel = TapppPanel(this)
        tapppPanel.initPanel(tapppContext, panelSetting)
        tapppPanel.startPanel()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        exoPlayerView.player!!.prepare()
    }

    private fun releasePlayer() {
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentMediaItemIndex
        exoPlayer.release()
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentMediaItemIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            exoPlayerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            exoPlayerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            exoPlayerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            exoPlayerView.onPause()
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Util.SDK_INT > 23) {
            exoPlayerView.onPause()
            releasePlayer()
        }
    }
}