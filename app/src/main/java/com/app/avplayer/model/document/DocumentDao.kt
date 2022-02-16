package com.app.avplayer.model.document

import androidx.room.*
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.document.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(document: Document)

    @Update
    fun update(document: Document)

    @Query("SELECT * FROM Document")
    fun getAll(): Flow<MutableList<Document>>
}