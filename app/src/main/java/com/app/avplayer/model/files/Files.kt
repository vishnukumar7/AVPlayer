package com.app.avplayer.model.files

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Files(

    @ColumnInfo(name = "display_name")
    var displayName: String = "",

    @ColumnInfo(name = "is_files")
    var files: String = "",

    @ColumnInfo(name = "folder_name")
    var folderName: String = "",

    @ColumnInfo(name = "mime_type")
    var mimeType: String = "",

    @ColumnInfo(name = "date_added")
    var dateAdded: String="",

    @PrimaryKey
    @ColumnInfo
    var path: String = "",

    @ColumnInfo(name = "num_of_items")
    var noOfItems: String = "",

    @ColumnInfo
    var size: String = ""
): Serializable{

}