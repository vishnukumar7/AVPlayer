package com.app.avplayer.model.gallery

import androidx.room.*
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.gallery.Gallery
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(gallery: Gallery)

    @Update
    fun update(gallery: Gallery)

    @Query("SELECT * FROM gallery ORDER BY date_added ASC")
    fun getAll(): Flow<MutableList<Gallery>>

    @Query("SELECT * FROM gallery where bucketDisplayName=:name ORDER BY date_added DESC")
    fun getList(name: String): List<Gallery>

}