package com.app.avplayer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.avplayer.AVPlayerApplication
import com.app.avplayer.helper.AVPViewModel
import com.app.avplayer.helper.AppDatabase

open class BaseActivity : AppCompatActivity() {

    val avpViewModel: AVPViewModel by viewModels {
        AVPViewModel.AVPViewModelFactory((application as AVPlayerApplication).appRepository)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


}