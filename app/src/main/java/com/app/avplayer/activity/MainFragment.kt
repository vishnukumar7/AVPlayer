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
import com.app.avplayer.adapter.AVPlayerAdapter
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
    lateinit var adapter: AVPlayerAdapter
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
        adapter = AVPlayerAdapter(requireActivity(),albumListItem,Constants.ALBUM_TYPE,screenWidth)
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
}