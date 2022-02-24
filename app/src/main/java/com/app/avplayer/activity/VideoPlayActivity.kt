package com.app.avplayer.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityVideoPlayBinding
import com.app.avplayer.external.CustomExoVideoPlayer
import com.app.avplayer.model.video.Video
import com.app.avplayer.utils.Constants
import com.app.avplayer.utils.DoubleClickListener

class VideoPlayActivity : BaseActivity(), View.OnTouchListener {
    private val TAG = "VideoPlayActivity"

    private lateinit var videoList: Video
    lateinit var exoVideoPlayer: CustomExoVideoPlayer
    lateinit var binding: ActivityVideoPlayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_play)
        exoVideoPlayer = findViewById(R.id.videoExoPlayer)
        exoVideoPlayer.setOnClickListener(object : DoubleClickListener() {
            override fun onSingleClick(v: View?) {
                Log.d(TAG, "onSingleClick: ")
                toolbar()
            }

            override fun onDoubleClick(v: View?) {
                Log.d(TAG, "onDoubleClick: ")
            }
        })
        videoList = intent.extras?.get(Constants.TAG_DATA) as Video
        exoVideoPlayer.playVideo(this, videoList.data)
        binding.title.text = videoList.display
        binding.backBtn.setOnClickListener {
            finish()
        }
        toolbar()

        binding.forwardVideo.setOnClickListener(object : DoubleClickListener() {
            override fun onSingleClick(v: View?) {
                Log.d(TAG, "onSingleClick: ")
            }

            override fun onDoubleClick(v: View?) {
                Log.d(TAG, "onDoubleClick: ")
                exoVideoPlayer.forwordvideo()
            }
        })
        binding.backwardVideo.setOnClickListener(object : DoubleClickListener() {
            override fun onSingleClick(v: View?) {
                Log.d(TAG, "onSingleClick: ")
            }

            override fun onDoubleClick(v: View?) {
                Log.d(TAG, "onDoubleClick: ")
                exoVideoPlayer.backwardVideo()
            }

        })
    }

    private fun toolbar() {
        if (binding.toolbar.visibility == GONE) {
            binding.toolbar.visibility = VISIBLE
            binding.toolbar.animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.toolbar.visibility = View.GONE
            binding.toolbar.animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_top)
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoVideoPlayer.onPausePlayer()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return true
    }
}