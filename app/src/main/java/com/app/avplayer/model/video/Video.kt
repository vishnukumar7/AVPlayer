package com.app.avplayer.model.video

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Video(

    @PrimaryKey
    @ColumnInfo
    var id: String = "",

    @ColumnInfo
    var duration: String = "",

    @ColumnInfo
    var bucketDisplay: String = "",

    @ColumnInfo(name = "date_added")
    var dateAdded: String = "",

    @ColumnInfo
    var display: String = "",

    @ColumnInfo
    var mimeType: String = "",

    @ColumnInfo
    var data: String = "",

    @ColumnInfo
    var size: String = "",

    @ColumnInfo
    var title: String = "",


    @ColumnInfo
    var relativePath: String = ""

) : Serializable {}
