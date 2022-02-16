package com.app.avplayer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityMainBinding
import com.app.avplayer.databinding.ListItemBinding
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide

class SongFragment : Fragment() {


    lateinit var adapter: AudioAdapter
    lateinit var binding: ActivityMainBinding
    var audioListItem=ArrayList<Audio>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(container!!.context),
            R.layout.activity_main, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.audioRecyclerView.setHasFixedSize(false)
        adapter = AudioAdapter(requireActivity(), audioListItem)
        binding.audioRecyclerView.adapter = adapter





    }


    class AudioAdapter(
        private var context: Context,
        private var itemList: ArrayList<Audio>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            viewHolder.itemBinding.duration.text =
                Constants.clockLength(audio.duration.toLong(), false)
            viewHolder.itemBinding.itemSize.text = Constants.getSize(audio.size.toLong())
            Glide.with(context)
                .load(AppUtils.getAlbumArtBitmap(context, audio.albumId.toLong()))
                .error(R.drawable.music_logo).into(viewHolder.itemBinding.albumArt)
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