package com.app.avplayer.model.video

import androidx.room.*
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.video.Video
import kotlinx.coroutines.flow.Flow


@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(video: Video)

    @Update
    fun update(video: Video)

    @Query("SELECT * FROM video")
    fun getAll(): Flow<MutableList<Video>>

    @Query("DELETE FROM Video")
    fun delete()

}