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
import com.app.avplayer.adapter.AVPlayerAdapter
import com.app.avplayer.databinding.ActivityMainBinding
import com.app.avplayer.databinding.ListItemBinding
import com.app.avplayer.model.document.Document
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide

class DocumentFragment : Fragment() {


    lateinit var adapter: AVPlayerAdapter
    lateinit var binding: ActivityMainBinding
    var listItem=ArrayList<Document>()

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
        adapter = AVPlayerAdapter(requireActivity(), listItem,Constants.DOCUMENT_TYPE)
        binding.audioRecyclerView.adapter = adapter
        (requireActivity() as MainActivity).avpViewModel.documentList.observe(viewLifecycleOwner){
            it?.let {
                listItem.clear()
                listItem.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
    }
}