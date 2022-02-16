package com.app.avplayer.model.audio

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {

    @Query("SELECT * from audio")
    fun getAll() : Flow<MutableList<Audio>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(audio: Audio)

    @Update
    fun update(audio: Audio)


    @Query("SELECT * from audio where id=:id limit 1")
    fun getAudioFromId(id: String) : List<Audio>

    @Query("SELECT * from audio where album_id=:albumId")
    fun getAudioFromAlbumId(albumId: String) : Flow<MutableList<Audio>>

    @Query("SELECT * FROM audio WHERE `like`='liked'")
    fun getLikedAudio(): Flow<MutableList<Audio>>

    @Ignore
    fun insertOrUpdate(audio: Audio, like: String){
        val audioList: List<Audio> =getAudioFromId(audio.id)
        when {
            audioList.isNotEmpty() -> {

                update(audio)
            }
            else -> insert(audio)
        }
    }

}