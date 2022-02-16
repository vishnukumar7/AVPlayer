package com.app.avplayer.model.document

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Document(

    @PrimaryKey
    @ColumnInfo
    var path: String = "",

    @ColumnInfo(name = "mime_type")
    var mimeType: String = "",

    @ColumnInfo
    var title: String = "",

    @ColumnInfo
    var size: String = "",

    @ColumnInfo
    var displayName: String = "",

    @ColumnInfo(name = "date_added")
    var dateAdded: String = ""
) {
}