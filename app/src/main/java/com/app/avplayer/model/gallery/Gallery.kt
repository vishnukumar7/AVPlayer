package com.app.avplayer.model.gallery

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity
data class Gallery(

    @PrimaryKey
    @ColumnInfo
    var id: String = "",

    @ColumnInfo
    var size: String = "",

    @ColumnInfo
    var displayName: String = "",

    @ColumnInfo
    var bucketDisplayName: String = "",

    @ColumnInfo
    var data: String = "",

    @ColumnInfo(name = "date_taken")
    var dateTaken: String="",

    @ColumnInfo(name = "date_modify")
    var dateModify:String ="",

    @ColumnInfo(name = "date_added")
    var dateAdded: String ="",

    @ColumnInfo(name = "date_expires")
    var dateExpired: String=""
): Serializable{

}