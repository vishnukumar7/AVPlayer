package com.app.avplayer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
import com.app.avplayer.model.video.Video
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt

class VideoFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    var screenWidth: Int=0
    lateinit var adapter: AVPlayerAdapter
    lateinit var binding: ActivityMainBinding
    var videoListItem = ArrayList<Video>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding=DataBindingUtil.inflate(LayoutInflater.from(container!!.context),
            R.layout.activity_main,container,false)
        return binding.root
    }

    fun showPopup(view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.inflate(R.menu.menu_list_1)
        popupMenu.show()
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
        adapter = AVPlayerAdapter(
            requireActivity(),
            videoListItem,
            Constants.VIDEO_TYPE,
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


        binding.optionsMenu.setOnClickListener { showPopup(it) }

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

    /**
     * This method will be invoked when a menu item is clicked if the item
     * itself did not already handle the event.
     *
     * @param item the menu item that was clicked
     * @return `true` if the event was handled, `false`
     * otherwise
     */
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.list_view -> {
                AppUtils.VIEW_LAYOUT_VIDEO_LIST = true
                binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.audioRecyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }

            R.id.grid_view -> {
                AppUtils.VIEW_LAYOUT_VIDEO_LIST = false
                binding.audioRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.audioRecyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
        return true
    }
}