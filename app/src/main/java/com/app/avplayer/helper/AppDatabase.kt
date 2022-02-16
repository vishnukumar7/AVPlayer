package com.app.avplayer.helper

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.avplayer.model.album.Album
import com.app.avplayer.model.album.AlbumDao
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.audio.AudioDao
import com.app.avplayer.model.document.Document
import com.app.avplayer.model.document.DocumentDao
import com.app.avplayer.model.files.Files
import com.app.avplayer.model.files.FilesDao
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.gallery.GalleryDao
import com.app.avplayer.model.video.Video
import com.app.avplayer.model.video.VideoDao

@Database(
    entities = [Audio::class, Album::class, Video::class, Gallery::class, Document::class, Files::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun albumDao(): AlbumDao

    abstract fun videoDao(): VideoDao

    abstract fun galleryDao(): GalleryDao
    abstract fun documentDao(): DocumentDao

    abstract fun filesDao(): FilesDao
}