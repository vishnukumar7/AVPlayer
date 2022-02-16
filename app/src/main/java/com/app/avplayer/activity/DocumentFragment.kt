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
import com.app.avplayer.model.document.Document
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide

class DocumentFragment : Fragment() {


    lateinit var adapter: DocumentAdapter
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
        adapter = DocumentAdapter(requireActivity(), listItem)
        binding.audioRecyclerView.adapter = adapter
        (requireActivity() as MainActivity).avpViewModel.documentList.observe(viewLifecycleOwner){
            it?.let {
                listItem.clear()
                listItem.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
    }


    class DocumentAdapter(private var context: Context, private var itemList: ArrayList<Document>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemBinding = ListItemBinding.bind(itemView.rootView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return DocumentViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder as DocumentViewHolder
            val document = itemList[position]
            viewHolder.itemBinding.itemText.text = document.displayName
            when (document.mimeType) {
                "application/pdf" -> {
                    Glide.with(context).load(R.drawable.pdf).into(holder.itemBinding.albumArt)
                }
                "text/plain" -> {
                    Glide.with(context).load(R.drawable.text).into(holder.itemBinding.albumArt)
                }
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                    Glide.with(context).load(R.drawable.docx).into(holder.itemBinding.albumArt)
                }
            }

            viewHolder.itemBinding.mainLay.setOnClickListener {

                when (document.mimeType) {
                    "application/pdf" -> {

                    }
                    "text/plain" -> {
                        val documentActivity = Intent(context, DocumentActivity::class.java)
                        documentActivity.putExtra(Constants.TAG_DATA, document.path)
                        documentActivity.putExtra(Constants.TAG_FROM, "text")
                        documentActivity.putExtra(Constants.TAG_TITLE, document.displayName)
                        context.startActivity(documentActivity)
                    }
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {

                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

    }
}