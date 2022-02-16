package com.app.avplayer.model.album

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Album")
data class Album(

    @PrimaryKey
    @ColumnInfo
    var id: String = "",

    @ColumnInfo
    var album: String = "",

    @ColumnInfo
    var albumId: String = "",

    @Ignore
    var albumArt: Bitmap? = null,

    @ColumnInfo
    var numSongs: String = "",

    @ColumnInfo
    var artist: String = "",

    @ColumnInfo
    var numSongByArtist: String = "",

    @ColumnInfo
    var artistId: String = "",

    @ColumnInfo
    var artistKey: String = "",

    ){}