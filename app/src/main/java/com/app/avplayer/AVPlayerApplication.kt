package com.app.avplayer

import android.app.Application
import com.app.avplayer.helper.AppRepository
import com.app.avplayer.helper.DatabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AVPlayerApplication : Application() {

        private val applicationScope= CoroutineScope(SupervisorJob())

        val database by lazy { DatabaseClient.getInstance(this) }

        val appRepository by lazy { AppRepository(database) }

}