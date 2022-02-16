package com.app.avplayer.model.files

import androidx.room.*
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.files.Files
import kotlinx.coroutines.flow.Flow

@Dao
interface FilesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(files: Files)

    @Update
    fun update(files: Files)

    @Query("SELECT * FROM files")
    fun getAll(): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND display_name not like '.%' AND display_name not like '(invalid)'")
    fun getFolderAll(name: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by display_name ASC")
    fun getFolderNameASC(name: String, filesValue: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by display_name DESC")
    fun getFolderNameDESC(name: String, filesValue: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by date_added ASC")
    fun getFolderDateAddedASC(name: String, filesValue: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by date_added DESC")
    fun getFolderDateAddedDESC(name: String, filesValue: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by size ASC")
    fun getFolderSizeASC(name: String, filesValue: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by size DESC")
    fun getFolderSizeDESC(name: String, filesValue: String): Flow<MutableList<Files>>

    @Query("SELECT * FROM files WHERE folder_name=:name AND is_files=:filesValue AND display_name not like '.%' AND display_name not like '(invalid)' order by display_name ASC")
    fun getFolder(name: String, filesValue: String): Flow<MutableList<Files>>

}