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
import com.app.avplayer.model.album.Album
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import kotlin.math.roundToInt


class MainFragment : Fragment() {


    var screenWidth: Int=0
    var albumListItem = ArrayList<Album>()
    lateinit var adapter: AudioAdapter
    lateinit var binding: ActivityMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       binding=DataBindingUtil.inflate(LayoutInflater.from(container!!.context),R.layout.activity_main,container,false)
       return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.audioRecyclerView.setHasFixedSize(false)
        val metrics=DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val r = resources
        screenWidth = (width* .45).roundToInt()
        adapter = AudioAdapter(requireActivity(),albumListItem,screenWidth)
         getList()
        binding.audioRecyclerView.adapter = adapter

        (requireActivity() as MainActivity).avpViewModel.albumList.observe(viewLifecycleOwner){
            it?.let {
                albumListItem.clear()
                albumListItem.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
      //  (activity as MainActivity).binding.bottomNavigation.selectedItemId=R.id.song
    }

    fun getList(type : Int =0){
        if (AppUtils.VIEW_LAYOUT_LIST)
            binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        else
            binding.audioRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        if(type==1) {
            binding.audioRecyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }



    class AudioAdapter(private var context: Context,
                       private var allAlbum: ArrayList<Album>, private var screenWidth:Int
    ):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        class AudioListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = ListItemBinding.bind(itemView.rootView)
        }

        class AudioGridtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = GridItemBinding.bind(itemView.rootView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(AppUtils.VIEW_LAYOUT_LIST){
                AudioListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
            } else AudioGridtViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
           if(holder is AudioListViewHolder){
               val album = allAlbum[position]
               holder.itemBinding.itemText.text = album.album
               Glide.with(context)
                   .load(AppUtils.getAlbumArtBitmap(context, album.albumId.toLong()))
                   .placeholder(R.drawable.music)
                   .error(R.drawable.music).into(
                       holder.itemBinding.albumArt
                   )
               holder.itemBinding.mainLay.setOnClickListener {
                   val intent = Intent(context, FileActivity::class.java)
                   intent.putExtra(Constants.TAG_FROM, "Main")
                   intent.putExtra(Constants.TAG_ALBUM_ID, album.albumId)
                   intent.putExtra(Constants.TAG_TITLE, album.album)
                   context.startActivity(intent)
               }
           }else if(holder is AudioGridtViewHolder){
               val album=allAlbum[position]
               holder.itemBinding.mainLay.layoutParams.width = screenWidth
               holder.itemBinding.mainLay.layoutParams.height=screenWidth
               holder.itemBinding.itemText.text =album.album
               Glide.with(context).load(AppUtils.getAlbumArtBitmap(context, album.albumId.toLong()))
                   .placeholder(R.drawable.music_1024).error(R.drawable.music_1024).into(
                   holder.itemBinding.albumArt
               )
               holder.itemBinding.mainLay.setOnClickListener {
                   val intent = Intent(context, FileActivity::class.java)
                   intent.putExtra(Constants.TAG_FROM, "Main")
                   intent.putExtra(Constants.TAG_ALBUM_ID, album.albumId)
                   intent.putExtra(Constants.TAG_TITLE, album.album)
                   context.startActivity(intent)
               }
           }
        }

        override fun getItemCount(): Int {
            return allAlbum.size
        }

    }
}