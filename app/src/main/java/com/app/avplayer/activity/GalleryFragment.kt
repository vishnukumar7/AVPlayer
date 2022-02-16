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
import com.app.avplayer.databinding.GalleryGridItemBinding
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.gallery.GalleryData
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt

class GalleryFragment : Fragment() {

    lateinit var adapter: ImageAdapter
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
        adapter = ImageAdapter(
            requireActivity(),
            listItem,
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
    }


    class ImageAdapter(
        private var context: Context,

        private var allAlbum: ArrayList<GalleryData>,
        private var screenWidth: Int
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = GalleryGridItemBinding.bind(itemView.rootView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return GridViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.gallery_grid_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is GridViewHolder) {
                holder.itemBinding.mainLay.layoutParams.width = screenWidth
                holder.itemBinding.mainLay.layoutParams.height = screenWidth
                holder.itemBinding.itemText.text = allAlbum[position].bucketDisplayName
                holder.itemBinding.numOfImages.text = allAlbum[position].imageCount.toString()
                 Glide.with(context)
                     .load(File(allAlbum[position].lastPath))
                     .placeholder(R.drawable.image).fitCenter().error(R.drawable.image).into(
                         holder.itemBinding.albumArt
                     )
                holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, FileActivity::class.java)
                    intent.putExtra(Constants.TAG_FROM, "Images")
                    intent.putExtra(Constants.TAG_TITLE, allAlbum[position].bucketDisplayName)
                    context.startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int {
            return allAlbum.size
        }

    }
}