package com.app.avplayer.helper

import android.content.Context
import androidx.room.Room
import com.app.avplayer.model.audio.AudioDao
import dagger.Provides
import javax.inject.Singleton

object DatabaseClient {

    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room
            .databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "avplayer"
            )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()


}