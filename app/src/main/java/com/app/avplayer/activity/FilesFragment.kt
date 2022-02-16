package com.app.avplayer.activity

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.avplayer.R
import com.app.avplayer.databinding.FragmentFilesBinding
import com.app.avplayer.databinding.GridItemBinding
import com.app.avplayer.databinding.ListItemBinding
import com.app.avplayer.databinding.OptionItemBinding
import com.app.avplayer.helper.OnFileClickChanged
import com.app.avplayer.model.files.Files
import com.app.avplayer.utils.AppUtils
import com.bumptech.glide.Glide
import java.io.File
import kotlin.math.roundToInt

class FilesFragment : Fragment(), OnFileClickChanged, AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentFilesBinding
    var screenWidth: Int = 0
    lateinit var dialog: Dialog
    lateinit var itemBinding: OptionItemBinding
var listItem= ArrayList<Files>()
    lateinit var fileAdapter: FilesAdapter

    var currentPath: String="/storage/emulated/0"

    var sortBy = arrayOf(
        "Name","Time","Size"
    )

    var sortText="Name"
    var orderText="ASC"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_files, container, false)
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
        screenWidth = (width * .45).roundToInt()
        fileAdapter = FilesAdapter(requireActivity(), listItem, screenWidth, this)
        getList(currentPath)
        dialog= Dialog(requireContext())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        itemBinding=DataBindingUtil.inflate(LayoutInflater.from(requireContext()),R.layout.option_item,null,false)
        //itemBinding=DataBindingUtil.setContentView(requireActivity(),R.layout.options_item);
        dialog.setContentView(itemBinding.root)
        dialog.window?.setGravity(Gravity.BOTTOM)
        itemBinding.deleteItem.setOnClickListener {
            Toast.makeText(requireContext(), "delete", Toast.LENGTH_SHORT).show()
            optionsItemDismiss()
        }

        itemBinding.infoItem.setOnClickListener {
            Toast.makeText(requireContext(), "info", Toast.LENGTH_SHORT).show()
            optionsItemDismiss()
        }

        var arrayAdapter=ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,sortBy)
       arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(binding.sortBy)
        {
            adapter = arrayAdapter
            setSelection(0, false)
            onItemSelectedListener = this@FilesFragment
            prompt = "Select your filter"
            gravity = Gravity.CENTER
        }

        binding.orderBy.setOnClickListener {
            if(orderText == "ASC") {
                binding.orderBy.setImageResource(R.drawable.upward)
                orderText="DESC"
            }
            else {
                binding.orderBy.setImageResource(R.drawable.downward)
                orderText= "ASC"
            }

            (requireActivity() as MainActivity).avpViewModel.getAudioList(currentPath,sortText,orderText).observe(viewLifecycleOwner){
                it?.let {
                    listItem.clear()
                    listItem.addAll(it)
                    fileAdapter.notifyDataSetChanged()
                }
            }


        }


    }

    fun optionsItemShow(){
        if(!dialog.isShowing)
            dialog.show()
    }

    fun optionsItemDismiss(){
        if(dialog.isShowing)
            dialog.dismiss()
    }


    fun getList(value: String=currentPath) {
        (activity as MainActivity).binding.backBtn.visibility=if(currentPath == "/storage/emulated/0") View.GONE else View.VISIBLE
        (activity as MainActivity).avpViewModel.getAudioList(value,orderText,sortText)
        if (AppUtils.VIEW_LAYOUT_LIST)
            binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        else
            binding.audioRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.audioRecyclerView.adapter = fileAdapter

        fileAdapter.notifyDataSetChanged()
    }


    inner class FilesAdapter(
        private var context: Context,
        private var allFiles: ArrayList<Files>,
        private var screenWidth: Int,
        private var onFileClickChanged: OnFileClickChanged
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        inner class AudioListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = ListItemBinding.bind(itemView.rootView)
        }

        inner class AudioGridtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = GridItemBinding.bind(itemView.rootView)
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (AppUtils.VIEW_LAYOUT_LIST) {
                AudioListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                )
            } else AudioGridtViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is AudioListViewHolder) {
                val file = allFiles[position]
                holder.itemBinding.itemText.text = file.displayName
                Glide.with(context)
                    .load(File(file.path))
                    .placeholder(AppUtils().getMimetypeImages(file.mimeType))
                    .fitCenter()
                    .error(AppUtils().getMimetypeImages(file.mimeType)).into(
                        holder.itemBinding.albumArt
                    )
                holder.itemBinding.mainLay.setOnClickListener {
                    /* val intent = Intent(context, FileActivity::class.java)
                     intent.putExtra(Contants.TAG_FROM, "Main")
                     intent.putExtra(
                         Contants.TAG_DATA,
                         audioDao.getAudioFromAlbumId(album.albumId) as ArrayList<Audio>
                     )
                     intent.putExtra(Contants.TAG_ALBUM_ID, album.albumId)
                     intent.putExtra(Contants.TAG_TITLE, album.album)
                     context.startActivity(intent)*/
                    if (file.files == "false") {
                        onFileClickChanged.onNextFile(file.folderName + File.separator + file.displayName)
                    }
                }
                holder.itemBinding.mainLay.setOnLongClickListener {
                    if (file.files == "true") {
                        optionsItemShow()
                    }
                    true
                }
            } else if (holder is AudioGridtViewHolder) {
                val file = allFiles[position]
                holder.itemBinding.mainLay.layoutParams.width = screenWidth
                holder.itemBinding.mainLay.layoutParams.height = screenWidth
                holder.itemBinding.itemText.text = file.displayName
                Glide.with(context).load(File(file.path))
                    .placeholder(AppUtils().getMimetypeImages(file.mimeType)).error(AppUtils().getMimetypeImages(file.mimeType)).into(
                        holder.itemBinding.albumArt
                    )
                /*holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, FileActivity::class.java)
                    intent.putExtra(Contants.TAG_FROM, "Main")
                    intent.putExtra(
                        Contants.TAG_DATA,
                        audioDao.getAudioFromAlbumId(album.albumId) as ArrayList<Audio>
                    )
                    intent.putExtra(Contants.TAG_ALBUM_ID, album.albumId)
                    intent.putExtra(Contants.TAG_TITLE, album.album)
                    context.startActivity(intent)
                }*/
            }
        }

        override fun getItemCount(): Int {
            return allFiles.size
        }

    }

    override fun onNextFile(value: String) {
        currentPath=value
        getList(value)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
       sortText=sortBy[position]
        (requireActivity() as MainActivity).avpViewModel.getAudioList(currentPath,sortText,orderText)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}