package com.app.avplayer.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.avplayer.R
import com.app.avplayer.adapter.AVPlayerAdapter
import com.app.avplayer.databinding.FragmentFilesBinding
import com.app.avplayer.databinding.OptionItemBinding
import com.app.avplayer.helper.OnFileClickChanged
import com.app.avplayer.model.files.Files
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FilesFragment : Fragment(), OnFileClickChanged, AdapterView.OnItemSelectedListener {
val TAG="FilesFragment"
    lateinit var binding: FragmentFilesBinding
    var screenWidth: Int = 0
    lateinit var dialog: Dialog
    lateinit var itemBinding: OptionItemBinding
var listItem= ArrayList<Files>()
    lateinit var fileAdapter: AVPlayerAdapter

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.audioRecyclerView.setHasFixedSize(false)
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        screenWidth = (width * .45).roundToInt()
        fileAdapter = AVPlayerAdapter(requireActivity(), listItem,Constants.FILES_TYPE, screenWidth, this)
        (requireActivity() as MainActivity).avpViewModel.filesList.observe(viewLifecycleOwner){
            it?.let {
                listItem.clear()
                listItem.addAll(it)
                fileAdapter.notifyDataSetChanged()
            }
        }
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

             getList()


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
       CoroutineScope(Dispatchers.Main).launch {
           (activity as MainActivity).avpViewModel.getFileList(value,orderText,sortText).observe(viewLifecycleOwner){
               it?.let {
                   Log.d(TAG, "getList: ")
                   listItem.clear()
                   listItem.addAll(it)
                   fileAdapter.notifyDataSetChanged()
               }
           }
       }
        if (AppUtils.VIEW_LAYOUT_LIST)
            binding.audioRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        else
            binding.audioRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.audioRecyclerView.adapter = fileAdapter
    }

    override fun onNextFile(value: String) {
        currentPath=value
        getList(value)
    }

    override fun onShowProgress() {
        optionsItemShow()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
       sortText=sortBy[position]
        getList()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}