package com.app.avplayer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityMainBinding
import com.app.avplayer.databinding.GridItemBinding
import com.app.avplayer.databinding.ListItemBinding
import com.app.avplayer.model.video.Video
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt

class VideoFragment : Fragment(){

    var screenWidth: Int=0
    lateinit var adapter: VideoAdapter
    lateinit var binding: ActivityMainBinding
    var videoListItem = ArrayList<Video>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding=DataBindingUtil.inflate(LayoutInflater.from(container!!.context),
            R.layout.activity_main,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.audioRecyclerView.setHasFixedSize(false)
        var metrics= DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val r = resources
        screenWidth = (width* .45).roundToInt()
        adapter = VideoAdapter(
            requireActivity(),
            videoListItem,
            screenWidth
        )
       getList()
        binding.audioRecyclerView.adapter = adapter
        (requireActivity() as MainActivity).avpViewModel.videoList.observe(viewLifecycleOwner){
            it?.let {
                videoListItem.clear()
                videoListItem.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }

    }

    fun getList(type: Int =0){
        if (AppUtils.VIEW_LAYOUT_VIDEO_LIST)
            binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        else
            binding.audioRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        if(type==1) {
            binding.audioRecyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }


    class VideoAdapter(private var context: Context,private var videoList: ArrayList<Video>,private var screenWidth: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(AppUtils.VIEW_LAYOUT_VIDEO_LIST) VideoListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)) else
                VideoGridViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.grid_item,parent,false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is VideoListViewHolder) {
                val video = videoList[position]
                Glide.with(context).load(File(video.data)).placeholder(R.drawable.video)
                    .error(R.drawable.video).fitCenter()
                    .into(holder.listItemBinding.albumArt)
                holder.listItemBinding.itemText.text = video.title
                holder.listItemBinding.itemSize.text = Constants.getSize(video.size.toLong())
                holder.listItemBinding.duration.text =
                    Constants.clockLength(video.duration.toLong(), true)
                holder.listItemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, VideoPlayActivity::class.java)
                    intent.putExtra(Constants.TAG_DATA, video)
                    context.startActivity(intent)
                }
            }else if(holder is VideoGridViewHolder) {
                holder.itemGridBinding.mainLay.layoutParams.width = screenWidth
                holder.itemGridBinding.mainLay.layoutParams.height = screenWidth
                val video = videoList[position]
                Glide.with(context).load(File(video.data)).error(R.drawable.music_logo).fitCenter()
                    .into(holder.itemGridBinding.albumArt)
                holder.itemGridBinding.itemText.text = video.title
            }
        }

        override fun getItemCount(): Int {
            return videoList.size
        }

        class VideoGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemGridBinding= GridItemBinding.bind(itemView)
        }

        class VideoListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val listItemBinding= ListItemBinding.bind(itemView)
        }

    }
}