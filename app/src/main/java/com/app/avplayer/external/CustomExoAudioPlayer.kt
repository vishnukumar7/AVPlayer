package com.app.avplayer.external


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.Nullable
import com.app.avplayer.activity.PlayAudioActivity
import com.app.medialoader.DownloadManager
import com.app.medialoader.MediaLoader
import com.app.medialoader.download.DownloadListener
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.PositionInfo
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File


class CustomExoAudioPlayer : FrameLayout, DownloadListener {
    private val APP_NAME = "AudioVideoPlayer"

    /**
     * PlayerViewHolder UI component
     * Watch PlayerViewHolder class
     */
    private var durationSongInMill: Long = 0
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private var durationSet: Boolean = false
    private lateinit var activity: PlayAudioActivity
    private var mMediaLoader // used for cache
            : MediaLoader? = null

    /**
     * variable declaration
     */
    // Media List
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded = false
    private var mediaContainer: FrameLayout? = null

    // controlling volume state
    private var volumeState: VolumeState? = null
    var videoState // video pause and play
            : PlayState? = null
    private val videoViewClickListener: View.OnClickListener =
        View.OnClickListener { v -> togglePlay() }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    fun togglePlay() {
        if (videoPlayer != null) {
            if (videoState == PlayState.OFF) {
                videoPlayer!!.playWhenReady = true
                videoState = PlayState.ON

            } else if (videoState == PlayState.ON) {
                videoPlayer!!.playWhenReady = false
                videoState = PlayState.OFF
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun togglePlay(activity: PlayAudioActivity) {
        if (videoPlayer == null)
            init(context)
        if (videoPlayer != null) {
            if (videoState == PlayState.OFF) {
                videoPlayer!!.playWhenReady = true
                videoState = PlayState.ON
                activity.binding.play.background = context.getDrawable(R.drawable.exo_icon_pause)
                activity.countDownTimer!!.resumeTimer()
            } else if (videoState == PlayState.ON) {
                videoPlayer!!.playWhenReady = false
                videoState = PlayState.OFF
                activity.binding.play.background = context.getDrawable(R.drawable.exo_icon_play)
                activity.countDownTimer!!.pauseTimer()
            }
        }
    }

    private fun init(context1: Context) {
        if (videoPlayer == null) {
            val context = context1.applicationContext
            val display: Display =
                (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val point = Point()
            display.getSize(point)
            videoSurfaceDefaultHeight = point.x
            screenDefaultHeight = point.y
            mMediaLoader = MediaLoader.getInstance(getContext())
            videoSurfaceView = PlayerView(this.context)
            val trackSelector = DefaultTrackSelector(context)
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd()
            )
            videoPlayer = SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build()


            /*BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
         TrackSelection.Factory videoTrackSelectionFactory =
                 new AdaptiveTrackSelection.Factory(bandwidthMeter);
         TrackSelector trackSelector =
                 new DefaultTrackSelector(videoTrackSelectionFactory);*/

            //Create the player using ExoPlayerFactory
            // videoPlayer = new SimpleExoPlayer.Builder(context).build();
            // Disable Player Control
            videoSurfaceView!!.useController = false
            // Bind the player to the view.
            videoSurfaceView!!.player = videoPlayer
            // Turn on Volume
            setVolumeControl(VolumeState.ON)
            videoPlayer!!.addListener(object : Player.Listener {
                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    Log.d(TAG, "onTimelineChanged: $timeline")
                }

                override fun onTracksChanged(
                    trackGroups: TrackGroupArray,
                    trackSelections: TrackSelectionArray
                ) {
                    Log.d(TAG, "onTracksChanged: ")
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    Log.d(TAG, "onIsLoadingChanged: $isLoading")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                        }
                        Player.STATE_ENDED -> activity.nextSong()
                        Player.STATE_IDLE -> {
                        }
                        Player.STATE_READY -> {
                            videoState = PlayState.ON
                            volumeState = VolumeState.ON
                            if (!isVideoViewAdded) {
                                addVideoView()
                            }
                            if (!durationSet) {
                                durationSet = true
                                durationSongInMill = videoPlayer!!.duration
                                activity.setTimer(durationSongInMill)
                            }

                        }

                        else -> {
                        }
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    Log.d(
                        TAG,
                        "onPlayWhenReadyChanged: $playWhenReady $reason"
                    )
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    Log.d(TAG, "onRepeatModeChanged: $repeatMode")
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    Log.d(TAG, "onShuffleModeEnabledChanged: ")
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.d(TAG, "onPlayerError: error : $error")
                }

                override fun onPositionDiscontinuity(
                    oldPosition: PositionInfo,
                    newPosition: PositionInfo,
                    reason: Int
                ) {
                    Log.d(TAG, "onPositionDiscontinuity: ")
                }

                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                    Log.d(TAG, "onPlaybackParametersChanged: ")
                }

                override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
                    Log.d(TAG, "onSeekBackIncrementChanged: ")
                }

                override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
                    Log.d(TAG, "onSeekForwardIncrementChanged: ")
                }

                override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Int) {
                    Log.d(TAG, "onMaxSeekToPreviousPositionChanged: ")
                }
            })
        }
    }

    fun setPlayControl(state: Boolean?) {
        videoPlayer!!.playWhenReady = state!!
    }

    fun playVideo(context: PlayAudioActivity, videoURL: String) {
        if (videoSurfaceView == null) {
            return
        }
        if (videoPlayer == null)
            init(context)

        durationSet = false
        activity = context
        durationSongInMill = 0
        activity.resettimer()
        // remove any old surface views from previously playing videos
        videoSurfaceView!!.visibility = INVISIBLE
        // removeVideoView(videoSurfaceView)
        Log.d(TAG, "playVideo: removevideoview")
        videoSurfaceView!!.player = videoPlayer
        //viewHolderParent.setOnClickListener(videoViewClickListener);

        //DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, AppName));
        val mediaUrl: String

        //mediaUrl = homepageResponseResult.get(targetPosition).getPlaybackUrl();
        var videoSource: MediaSource? = null

        //   if (homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {
        val isCached = mMediaLoader!!.isCached(videoURL)
        Log.d(TAG, "playVideo: cached : $isCached")
        if (!isCached && getContext() != null) DownloadManager.getInstance(getContext())?.enqueue(DownloadManager.Request(videoURL), this)


        mediaContainer = activity.exoAudioPlayer
        mediaUrl = if (isCached) {
            mMediaLoader!!.getCacheFile(videoURL).toString()
        } else {
            videoURL
        }
        Log.d(TAG, "playVideo: media url : $mediaUrl")
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        videoSource = buildMediaSource(Uri.fromFile(File(mediaUrl)))
        Log.d(TAG, "playVideo: video source : $videoSource")
        videoPlayer!!.setMediaSource(videoSource)
        videoPlayer!!.prepare()
        videoPlayer!!.playWhenReady = true
        videoSurfaceView!!.defaultArtwork=context.getDrawable(R.drawable.exo_ic_default_album_image)
        videoSurfaceView!!.useArtwork=true
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context.applicationContext, APP_NAME)
        )
        @C.ContentType val type: Int = Util.inferContentType(uri)
        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: PlayerView?) {
        val parent = videoView!!.parent as ViewGroup
        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
        }
    }

    private fun addVideoView() {
        mediaContainer!!.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView!!.requestFocus()
        videoSurfaceView!!.visibility = VISIBLE
        videoSurfaceView!!.alpha = 1f
        //mediaCoverImage.setVisibility(GONE);
    }

    fun resetVideoView() {
        if (isVideoViewAdded) {
            playPosition = -1
            removeVideoView(videoSurfaceView)
            videoSurfaceView!!.visibility = INVISIBLE
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoSurfaceView = null
            videoPlayer!!.release()
            videoPlayer = null
        }
    }

    fun onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.stop(true)
        }
    }

    //public void onRestartPlayer() {
    //  if (videoPlayer != null) {
    //   playVideo(true);
    //  }
    //}
    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        if (state == VolumeState.OFF) {
            videoPlayer!!.volume = 0f
            //animateVolumeControl();
        } else if (state == VolumeState.ON) {
            videoPlayer!!.volume = 1f
            //animateVolumeControl();
        }
    }

    override fun onProgress(url: String?, file: File?, progress: Int) {
        Log.d(
            TAG,
            "onProgress: " + url + " file : " + file?.absolutePath + " progress : " + progress
        )
    }

    override fun onError(e: Throwable?) {
        Log.d(TAG, "onError: $e")
    }

    /**
     * Volume ENUM
     */
    private enum class VolumeState {
        ON, OFF
    }

    enum class PlayState {
        ON, OFF
    }

    companion object {
        const val TAG = "CustomExoPlayer"
        const val AppName = "KotlinApplication"
    }

    /**
     * Notify the host application that a file should be downloaded
     * @param url The full url to the content that should be downloaded
     * @param userAgent the user agent to be used for the download.
     * @param contentDisposition Content-disposition http header, if
     * present.
     * @param mimetype The mimetype of the content reported by the server
     * @param contentLength The file size reported by the server
     */
}