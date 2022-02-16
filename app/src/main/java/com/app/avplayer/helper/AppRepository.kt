package com.app.avplayer.helper

import androidx.annotation.WorkerThread
import com.app.avplayer.model.album.Album
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.document.Document
import com.app.avplayer.model.files.Files
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.video.Video
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

class AppRepository(private var appDatabase: AppDatabase) {

    val albumList: Flow<MutableList<Album>> = appDatabase.albumDao().getAll()

    val documentList: Flow<MutableList<Document>> = appDatabase.documentDao().getAll()
    val filesList: Flow<MutableList<Files>> = appDatabase.filesDao().getAll()
    val galleryList: Flow<MutableList<Gallery>> = appDatabase.galleryDao().getAll()
    val videoList: Flow<MutableList<Video>> = appDatabase.videoDao().getAll()

    //audio
    val audioList: Flow<MutableList<Audio>> = appDatabase.audioDao().getAll()
val audioListLiked: Flow<MutableList<Audio>> =appDatabase.audioDao().getLikedAudio()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(audio: Audio){
        appDatabase.audioDao().insert(audio)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(album: Album){
        appDatabase.albumDao().insert(album)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(document: Document){
        appDatabase.documentDao().insert(document)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(files: Files){
        appDatabase.filesDao().insert(files)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(gallery: Gallery){
        appDatabase.galleryDao().insert(gallery)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(video: Video){
        appDatabase.videoDao().insert(video)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(video: Video){
        appDatabase.videoDao().update(video)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(audio: Audio){
        appDatabase.audioDao().update(audio)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(album: Album){
        appDatabase.albumDao().update(album)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(document: Document){
        appDatabase.documentDao().update(document)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(files: Files){
        appDatabase.filesDao().update(files)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(gallery: Gallery){
        appDatabase.galleryDao().update(gallery)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getListFromTitle(title: String): Flow<MutableList<Gallery>> {
        return appDatabase.galleryDao().getList(title)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAudioList(path: String,orderBy: String,sortBy: String): Flow<MutableList<Files>> {
        when(orderBy) {
            "Name" -> {
                return if (sortBy == "ASC") {
                    merge(
                        appDatabase.filesDao().getFolderNameASC(path, "false"),
                        appDatabase.filesDao().getFolderNameASC(path, "true")
                    )
                } else {
                    merge(
                        appDatabase.filesDao().getFolderNameDESC(path, "false"),
                        appDatabase.filesDao().getFolderNameDESC(path, "true")
                    )
                }
            }

            "Time" -> {
                return if (sortBy == "ASC") {
                    merge(
                        appDatabase.filesDao().getFolderDateAddedASC(path, "false"),
                        appDatabase.filesDao().getFolderDateAddedASC(path, "true")
                    )
                } else {
                    merge(
                        appDatabase.filesDao().getFolderDateAddedDESC(path, "false"),
                        appDatabase.filesDao().getFolderDateAddedDESC(path, "true")
                    )
                }
            }

            "Size" -> {
                return if (sortBy == "ASC") {
                    merge(
                        appDatabase.filesDao().getFolderSizeASC(path, "false"),
                        appDatabase.filesDao().getFolderSizeASC(path, "true")
                    )
                } else {
                    merge(
                        appDatabase.filesDao().getFolderSizeDESC(path, "false"),
                        appDatabase.filesDao().getFolderSizeDESC(path, "true")
                    )
                }
            }
            else -> {
                return merge(
                    appDatabase.filesDao().getFolder(path, "false"),
                    appDatabase.filesDao().getFolder(path, "true")
                )
            }
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAudioAlbumList(albumId: String): Flow<MutableList<Audio>> {
        return appDatabase.audioDao().getAudioFromAlbumId(albumId)
    }



}