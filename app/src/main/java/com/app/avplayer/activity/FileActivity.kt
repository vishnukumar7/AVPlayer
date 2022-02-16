package com.app.avplayer.activity

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.avplayer.R
import com.app.avplayer.adapter.AVPlayerAdapter
import com.app.avplayer.databinding.ActivityFileBinding
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.utils.Constants
import kotlin.math.roundToInt

class FileActivity : BaseActivity() {

    companion object {
        private val TAG: String = "FileActivity"
        private var TITLE: String = ""
    }
    var listItem =ArrayList<Gallery>()
    lateinit var adapter: AVPlayerAdapter
    lateinit var binding: ActivityFileBinding
    var audioListItem=ArrayList<Audio>()
    var screenWidth: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file)
        if (intent.getStringExtra(Constants.TAG_FROM).equals("main", true)) {
            adapter = AVPlayerAdapter(this, audioListItem,Constants.AUDIO_TYPE)
            binding.audioRecyclerView.setHasFixedSize(false)
            binding.audioRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.audioRecyclerView.adapter = adapter
            binding.title.text = intent.getStringExtra(Constants.TAG_TITLE)


            avpViewModel.audioAlbumList.observe(this){
                it?.let {
                    audioListItem.clear()
                    audioListItem.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }

        } else if (intent.getStringExtra(Constants.TAG_FROM).equals("Images", true)) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val width = metrics.widthPixels
            val r = resources
            screenWidth = (width * .35).roundToInt()
            Log.d(TAG, "onCreate: $screenWidth")
            TITLE = intent.getStringExtra(Constants.TAG_TITLE)!!
            adapter = AVPlayerAdapter(this, listItem ,Constants.IMAGE_TYPE, screenWidth, title = TITLE)

            binding.audioRecyclerView.setHasFixedSize(false)
            binding.audioRecyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.audioRecyclerView.adapter = adapter
            binding.title.text = intent.getStringExtra(Constants.TAG_TITLE)
            avpViewModel.galleryTitleList.observe(this){
                it?.let {
                    listItem.clear()
                    listItem.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }

        }

        binding.backBtn.setOnClickListener { finish() }
    }
}