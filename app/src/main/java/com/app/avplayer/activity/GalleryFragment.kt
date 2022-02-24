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
import com.app.avplayer.databinding.GalleryGridItemBinding
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.gallery.GalleryData
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt

class GalleryFragment : Fragment() {

    lateinit var adapter: AVPlayerAdapter
    lateinit var binding: ActivityMainBinding
    var screenWidth: Int = 0
    var listItem= ArrayList<GalleryData>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(container!!.context),
            R.layout.activity_main,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.audioRecyclerView.setHasFixedSize(false)
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val r = resources
        screenWidth = (width * .3).roundToInt()
        adapter = AVPlayerAdapter(
            requireActivity(),
            listItem,
            Constants.GALLERY_TYPE,
            screenWidth
        )
        binding.audioRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.audioRecyclerView.adapter = adapter
        (requireActivity() as MainActivity).avpViewModel.galleryList.observe(viewLifecycleOwner){
            it?.let {
                listItem.clear()
                listItem.addAll(AppUtils.getGalleryAlbum(it as ArrayList<Gallery>))
                adapter.notifyDataSetChanged()
            }
        }
        binding.optionsMenu.visibility=View.GONE
    }
}