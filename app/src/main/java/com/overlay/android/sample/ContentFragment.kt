package com.overlay.android.sample

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.mobile.aibuysdk.model.analytics.VideoEventType
import com.mobile.aibuysdk.model.analytics.VideoParameter
import com.mobile.aibuysdk.utils.analytics.VideoEventData
import com.mobile.aibuysdk.view.overlayview.OverlayView
import java.util.concurrent.TimeUnit

//debug purpose video
private const val VIDEO_URL =
    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
private const val KEY_PLAYER_POSITION = "key_player_position"
private const val KEY_PLAYER_PLAY_WHEN_READY = "key_player_when_ready"

class ContentFragment : Fragment() {

    val viewModel: ContentFragmentViewModel by viewModels()
    private var listener: Player.Listener? = null
    private lateinit var playerView: PlayerView
    private lateinit var muteButton: ImageView
    private lateinit var fullScreenButton: ImageView
    private lateinit var overlayView: OverlayView
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupOnBackPressedListener()
    }

    private fun setupOnBackPressedListener() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.player?.release()
                    overlayView.release()
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.player?.apply {
            outState.putLong(KEY_PLAYER_POSITION, viewModel.player?.contentPosition ?: 0)
            outState.putBoolean(
                KEY_PLAYER_PLAY_WHEN_READY,
                viewModel.player?.playWhenReady ?: false
            )
            pause()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            viewModel.player?.seekTo(it.getLong(KEY_PLAYER_POSITION))
            viewModel.player?.playWhenReady = it.getBoolean(KEY_PLAYER_PLAY_WHEN_READY)
        }
    }

    private fun setProvidingInfoMethods(overlayView: OverlayView) {
        overlayView.setOnStopVideo {
            viewModel.player?.pause()
        }
        overlayView.setOnStartVideo {
            viewModel.player?.play()
        }
        overlayView.setGetTiming {
            viewModel.player?.currentPosition ?: 0L
        }
        overlayView.setGetPlaying {
            viewModel.player?.isPlaying ?: false
        }
        overlayView.initStyles()
    }

    private fun setGlobalLayoutListener(overlayView: OverlayView, playerView: PlayerView) {
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            /*
             Workaround for Android 12, because on Android 12 OnGlobalLayout is being called with some weird
             view heights. I.e. On Empty Screen it returns like 1400 a few times, and then 2600 for a specified device.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (overlayView.isViewResized()) {
                    overlayView.adjustPortrait(playerView.height)
                }
            } else {
                clearGlobalLayoutListener()
                overlayView.adjustPortrait(playerView.height)

            }
        }
        overlayView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun clearGlobalLayoutListener() {
        val overlayViewTreeObserver =
            view?.findViewById<OverlayView>(R.id.overlay)?.viewTreeObserver
        overlayViewTreeObserver?.let {
            globalLayoutListener?.let(overlayViewTreeObserver::removeOnGlobalLayoutListener)
        }
        globalLayoutListener = null
    }

    private fun initializePlayer(overlayView: OverlayView, playerView: PlayerView) {
        viewModel.player = ExoPlayer.Builder(requireContext()).build()
        viewModel.playerCreated = true
        playerView.player = viewModel.player
        val mediaItem = MediaItem.fromUri(VIDEO_URL)
        viewModel.player?.setMediaItem(mediaItem)
        overlayView.setUidVideo(
            UidContainer.uid,
            UidContainer.isRealtime,
            UidContainer.isLinkoutInBrowser,
            UidContainer.jsonCustomizationURL
        )
        viewModel.player?.let {
            setPlayerConfig(it, overlayView)
        }
    }

    private fun setListeners(overlayView: OverlayView) {
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (viewModel.player?.isPlaying == true) {
                    overlayView.startPlay(viewModel.player?.contentPosition ?: 0)
                } else {
                    overlayView.stopPlay(viewModel.player?.contentPosition ?: 0)
                }
                super.onIsPlayingChanged(isPlaying)
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                    if (!player.isPlaying) {
                        overlayView.postNewTime(player.contentPosition)
                    }
                }
                setPlayerConfig(player, overlayView)
                super.onEvents(player, events)
                overlayView.setPlayerDimensions(playerView.height, playerView.width)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    overlayView.setIsVideoEnded(true)
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                viewModel.player?.let {
                    setPlayerConfig(it, overlayView)
                }
                overlayView.onVideoEvents(
                    listOf(
                        VideoEventData(
                            VideoEventType.VIDEO_SEEK, mapOf(
                                VideoParameter.TIME to oldPosition.positionMs / TimeUnit.SECONDS.toMillis(
                                    1
                                )
                            )
                        ),
                        VideoEventData(
                            VideoEventType.VIDEO_SEEKED, mapOf(
                                VideoParameter.TIME to newPosition.positionMs / TimeUnit.SECONDS.toMillis(
                                    1
                                )
                            )
                        )
                    ), true
                )
            }

        }.let {
            listener = it
            viewModel.player?.addListener(it)
        }
    }

    private fun setPlayerConfig(
        player: Player,
        overlayView: OverlayView
    ) {
        val duration = if (player.duration > 0) player.duration else 0
        val playerConfig = mapOf<VideoParameter, Any?>(
            VideoParameter.VOLUME to player.volume,
            VideoParameter.IS_MUTED to (player.volume == 0.0f),
            VideoParameter.PLAYBACK_RATE to player.playbackParameters.speed,
            VideoParameter.VISUAL_QUALITY_PX to player.videoSize.height,
            VideoParameter.DURATION to duration / TimeUnit.SECONDS.toMillis(1),
            VideoParameter.TIME to player.currentPosition / TimeUnit.SECONDS.toMillis(1)
        )
        overlayView.setPlayerDimensions(playerView.height, playerView.width)
        overlayView.setPlayerControlsHeight((playerView.findViewById<View>(com.google.android.exoplayer2.R.id.exo_play).parent.parent as LinearLayout).height)
        overlayView.onConfigChanged(playerConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView = view.findViewById(R.id.player_video)
        playerView.let { playerView ->
            overlayView = view.findViewById(R.id.overlay)
            setGlobalLayoutListener(overlayView, playerView)
            if (!viewModel.playerCreated || viewModel.player == null) {
                initializePlayer(overlayView, playerView)
            } else {
                playerView.player = viewModel.player
            }
            setProvidingInfoMethods(overlayView)
            setListeners(overlayView)
            muteButton = view.findViewById(R.id.mute_button)
            muteButton.setOnClickListener {
                changeVolumeState()
                val time =
                    (viewModel.player?.contentPosition?.div(TimeUnit.SECONDS.toMillis(1))) ?: 0
                overlayView.onVideoEvent(
                    VideoEventData(
                        VideoEventType.VIDEO_MUTE,
                        mapOf(VideoParameter.TIME to time)
                    )
                )
            }
            fullScreenButton = view.findViewById(R.id.fullscreen_button)
            setFullScreenIcon()
            fullScreenButton.setOnClickListener {
                val isLandscape = changeScreenMode()

                val time =
                    (viewModel.player?.contentPosition?.div(TimeUnit.SECONDS.toMillis(1))) ?: 0
                overlayView.onVideoEvent(
                    VideoEventData(
                        VideoEventType.VIDEO_FULLSCREEN, mapOf(
                            VideoParameter.TIME to time,
                            VideoParameter.FULL_SCREEN to isLandscape
                        )
                    )
                )
            }
        }
        activity?.window?.decorView?.let {
            overlayView.subscribeOnKeyboardVisibility(it)
        }
    }

    private fun changeVolumeState() {
    }

    private fun setFullScreenIcon() {
        if (activity?.resources?.configuration?.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        } else {
        }
    }

    /*
    Returns the value which is equals to isLandscape orientation applied
     */
    private fun changeScreenMode(): Boolean =
        if (activity?.resources?.configuration?.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            true
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            false
        }

    override fun onDestroyView() {
        clearGlobalLayoutListener()
        listener?.let {
            viewModel.player?.removeListener(it)
        }
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContentFragment()
    }
}