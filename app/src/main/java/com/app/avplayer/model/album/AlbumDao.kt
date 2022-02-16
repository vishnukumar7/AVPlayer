package com.app.avplayer.model.album

import androidx.room.*
import com.app.avplayer.model.album.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(album: Album)

    @Update
    fun update(album: Album)


    @Query("SELECT * FROM Album")
    fun getAll(): Flow<MutableList<Album>>

    @Query("SELECT * FROM Album WHERE id=:id LIMIT 1")
    fun getAlbumFromId(id: String): List<Album>

}