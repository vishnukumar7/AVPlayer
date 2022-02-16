package com.app.avplayer.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityInformationBinding
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.app.avplayer.utils.Constants.TAG_IMAGES

class InformationActivity : BaseActivity() {

    lateinit var binding: ActivityInformationBinding
    lateinit var gallery: Gallery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_information)
        if (intent.extras != null && intent.getStringExtra(Constants.TAG_FROM).equals(TAG_IMAGES)) {
            gallery = intent.extras?.get(Constants.TAG_DATA) as Gallery
            binding.fileName.text = gallery.displayName
            binding.filePath.text = gallery.data
            binding.dateCreated.text= AppUtils.getDateTimeFromStamp(gallery.dateAdded)
            binding.size.text = Constants.getSize(gallery.size.toLong())
        }
        binding.backBtn.setOnClickListener { finish() }
    }
}