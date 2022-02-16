package com.app.avplayer.model.audio

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Audio")
data class Audio(

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String = "",

    @ColumnInfo(name = "file_path")
    var path: String = "",

    @ColumnInfo(name = "date_added")
    var dateAdded: String="",

    @Ignore
    var imagePath: Bitmap? = null,

    @ColumnInfo(name = "album_id")
    var albumId: String = "",

    @ColumnInfo(name = "song_name")
    var displayName: String = "",

    @ColumnInfo(name = "album_name")
    var album: String = "",

    @ColumnInfo(name = "like")
    var like: String = "",

    @ColumnInfo
    var size: String = "",

    @ColumnInfo
    var duration: String = "") : Serializable {
}
