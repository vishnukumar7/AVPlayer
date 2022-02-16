package com.app.avplayer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityFileBinding
import com.app.avplayer.databinding.FileGalleryGridItemBinding
import com.app.avplayer.databinding.ListItemBinding
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt

class FileActivity : BaseActivity() {

    companion object {
        private val TAG: String = "FileActivity"
        private var TITLE: String = ""
    }
    var listItem =ArrayList<Gallery>()
    lateinit var audioAdapter: AudioAdapter
    lateinit var binding: ActivityFileBinding
    lateinit var imageAdapter: ImageAdapter
    var audioListItem=ArrayList<Audio>()
    var screenWidth: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file)
        if (intent.getStringExtra(Constants.TAG_FROM).equals("main", true)) {
            audioAdapter = AudioAdapter(this, audioListItem)
            binding.audioRecyclerView.setHasFixedSize(false)
            binding.audioRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.audioRecyclerView.adapter = audioAdapter
            binding.title.text = intent.getStringExtra(Constants.TAG_TITLE)


            avpViewModel.audioAlbumList.observe(this){
                it?.let {
                    audioListItem.clear()
                    audioListItem.addAll(it)
                    audioAdapter.notifyDataSetChanged()
                }
            }

        } else if (intent.getStringExtra(Constants.TAG_FROM).equals("Images", true)) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val width = metrics.widthPixels
            val r = resources
            screenWidth = (width * .35).roundToInt()
            Log.d(TAG, "onCreate: $screenWidth")
            imageAdapter = ImageAdapter(this, listItem , screenWidth)
            TITLE = intent.getStringExtra(Constants.TAG_TITLE)!!
            binding.audioRecyclerView.setHasFixedSize(false)
            binding.audioRecyclerView.layoutManager = GridLayoutManager(this, 3)
            binding.audioRecyclerView.adapter = imageAdapter
            binding.title.text = intent.getStringExtra(Constants.TAG_TITLE)
            avpViewModel.galleryTitleList.observe(this){
                it?.let {
                    listItem.clear()
                    listItem.addAll(it)
                    imageAdapter.notifyDataSetChanged()
                }
            }

        }

        binding.backBtn.setOnClickListener { finish() }
    }

    class ImageAdapter(
        private var context: Context,
        private var itemList: ArrayList<Gallery>,
        private var screenWidth: Int
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.file_gallery_grid_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ImageViewHolder) {
                //   holder.gridItemBinding.mainLay.layoutParams.width = screenWidth
                holder.gridItemBinding.mainLay.layoutParams.height = screenWidth
                Log.d(TAG, "onBindViewHolder: position $position")
                Glide.with(context)
                    .load(File(itemList[position].data))
                    .placeholder(R.drawable.image).error(R.drawable.image).into(
                        holder.gridItemBinding.albumArt
                    )
                holder.gridItemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, ImageActivity::class.java)
                    intent.putExtra(Constants.TAG_TITLE, TITLE)
                    context.startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val gridItemBinding = FileGalleryGridItemBinding.bind(itemView.rootView)
        }

    }

    class AudioAdapter(
        private var context: Context,
        private var itemList: ArrayList<Audio>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = ListItemBinding.bind(itemView.rootView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AudioViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder as AudioViewHolder
            val audio = itemList[position]
            viewHolder.itemBinding.itemText.text = audio.displayName
            val bitmap = AppUtils.getAlbumArtBitmap(context, audio.albumId.toLong())
            Glide.with(context).load(bitmap).error(R.drawable.media)
                .into(viewHolder.itemBinding.albumArt)
            viewHolder.itemBinding.mainLay.setOnClickListener {
                val intent = Intent(context, PlayAudioActivity::class.java)
                intent.putExtra(Constants.TAG_DATA, itemList)
                intent.putExtra(Constants.TAG_POSITION, position)
                context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

    }
}