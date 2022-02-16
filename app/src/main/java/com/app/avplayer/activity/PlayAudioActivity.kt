package com.app.avplayer.activity

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.databinding.DataBindingUtil
import com.app.avplayer.AVPlayerApplication
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityPlayAudioBinding
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.audio.AudioDao
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.app.avplayer.external.CustomExoAudioPlayer
import com.app.avplayer.external.Hourglass

class PlayAudioActivity : BaseActivity() {
    lateinit var binding: ActivityPlayAudioBinding
    lateinit var exoAudioPlayer: CustomExoAudioPlayer
    lateinit var audioList: ArrayList<Audio>
    var position: Int = 0
    lateinit var displayMetrics: DisplayMetrics
    var countDownTimer: Hourglass? = null
lateinit var audioDao: AudioDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_audio)
        //exoPlayer=CustomExoPlayer(this)
        exoAudioPlayer = findViewById(R.id.songExoPlayer)
        //    exoPlayer.resetVideoView()
        displayMetrics = DisplayMetrics()
audioDao=(application as AVPlayerApplication).database.audioDao()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        audioList = intent.extras?.get(Constants.TAG_DATA) as ArrayList<Audio>
        position = intent.getIntExtra(Constants.TAG_POSITION, 0)
        exoAudioPlayer.playVideo(this, audioList[position].path)
        binding.title.text = audioList[position].displayName
        binding.songExoPlayer.layoutParams.height = (displayMetrics.widthPixels * 0.8).toInt()
        binding.songExoPlayer.layoutParams.width = (displayMetrics.widthPixels * 0.8).toInt()
        binding.backBtn.setOnClickListener { finish() }
        binding.play.setOnClickListener {
            exoAudioPlayer.togglePlay(this)
        }
        binding.forward.setOnClickListener {
            nextSong()
        }
        binding.backward.setOnClickListener {
            previousSong()
        }
        binding.liked.setOnClickListener {
            val audio=audioList[position]
            if(audio.like.isEmpty() || audio.like=="un_like"){
                audio.like="liked"
            }else
                audio.like="un_like"
            audioDao.update(audio)
            setliked()
        }
        setliked()
    }

    fun setliked(){
        val audio= audioDao.getAudioFromId(audioList[position].id)[0]
        if(audio.like == "liked"){
            binding.liked.setImageDrawable(getDrawable(R.drawable.liked))
        }else{
            binding.liked.setImageDrawable(getDrawable(R.drawable.un_like))
        }
    }

    fun resettimer(){
        if(countDownTimer!=null){
            countDownTimer!!.stopTimer()
            binding.runningTime.text=getString(R.string.time_zero)
            binding.totalTime.text=getString(R.string.time_zero)
        }
    }
    fun previousSong(){

        if(position>0) {
            position--
            resettimer()
            setliked()
            exoAudioPlayer.onPausePlayer()
            exoAudioPlayer.resetVideoView()
            binding.title.text = audioList[position].displayName
            exoAudioPlayer.playVideo(this@PlayAudioActivity, audioList[position].path)
        }
    }

    fun nextSong(){

        if(audioList.size-1>position) {
            exoAudioPlayer.onPausePlayer()
            position++
            setliked()
            exoAudioPlayer.resetVideoView()
            resettimer()
            binding.title.text=audioList[position].displayName
            exoAudioPlayer.playVideo(this@PlayAudioActivity, audioList[position].path)
        }
    }

    fun setTimer(duration: Long){
        val seconds1=duration/1000
        binding.totalTime.text="${AppUtils.calculateMinutes(seconds1)} : ${AppUtils.calculateSeconds(seconds1)}"
        binding.totalTime.tag="$seconds1"
        binding.runningProgress.max=seconds1.toInt()

        countDownTimer= object : Hourglass(duration,1000) {
            override fun onTimerTick(timeRemaining: Long) {
                runOnUiThread {
                    val seconds=timeRemaining/1000
                    val remaingTime=binding.totalTime.tag.toString().toLong()-seconds
                    binding.runningProgress.progress=remaingTime.toInt()
                    binding.runningTime.text="${AppUtils.calculateMinutes(remaingTime)} : ${AppUtils.calculateSeconds(remaingTime)}"
                }
            }

            override fun onTimerFinish() {
                runOnUiThread {
                    binding.runningTime.text = getString(R.string.time_zero)
                    binding.totalTime.text = getString(R.string.time_zero)
                }
            }

        }
        countDownTimer!!.startTimer()

    }


}