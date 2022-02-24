package com.app.avplayer.helper

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.avplayer.model.album.Album
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.document.Document
import com.app.avplayer.model.files.Files
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.video.Video
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import java.io.File

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

    fun getFileList(path: String,orderBy: String,sortBy: String): MutableLiveData<ArrayList<Files>> {
        when(orderBy) {
            "Name" -> {
                return if (sortBy == "ASC") {
                    val task=appDatabase.filesDao().getFolderNameASC(path, "false") as ArrayList<Files>
                    task.addAll(appDatabase.filesDao().getFolderNameASC(path, "true"))
                    val result=MutableLiveData<ArrayList<Files>>()
                    result.value=task
                    result
                } else {
                    val task=appDatabase.filesDao().getFolderNameDESC(path, "false") as ArrayList<Files>
                    task.addAll(appDatabase.filesDao().getFolderNameDESC(path, "true"))
                    val result=MutableLiveData<ArrayList<Files>>()
                    result.value=task
                    result

                }
            }

            "Time" -> {
                return if (sortBy == "ASC") {
                    val task=appDatabase.filesDao().getFolderDateAddedASC(path, "false") as ArrayList<Files>
                    task.addAll(appDatabase.filesDao().getFolderDateAddedASC(path, "true"))
                    val result=MutableLiveData<ArrayList<Files>>()
                    result.value=task
                    result

                } else {
                    val task=appDatabase.filesDao().getFolderDateAddedDESC(path, "false") as ArrayList<Files>
                    task.addAll(appDatabase.filesDao().getFolderDateAddedDESC(path, "true"))
                    val result=MutableLiveData<ArrayList<Files>>()
                    result.value=task
                    result
                }
            }

            "Size" -> {
                return if (sortBy == "ASC") {
                    val task=appDatabase.filesDao().getFolderSizeASC(path, "false") as ArrayList<Files>
                    task.addAll(appDatabase.filesDao().getFolderSizeASC(path, "true"))
                    val result=MutableLiveData<ArrayList<Files>>()
                    result.value=task
                    result

                } else {
                    val task=appDatabase.filesDao().getFolderSizeDESC(path, "false") as ArrayList<Files>
                    task.addAll(appDatabase.filesDao().getFolderSizeDESC(path, "true"))
                    val result=MutableLiveData<ArrayList<Files>>()
                    result.value=task
                    result
                }
            }
            else -> {
                val task=appDatabase.filesDao().getFolder(path, "false") as ArrayList<Files>
                task.addAll(appDatabase.filesDao().getFolder(path, "true"))
                val result=MutableLiveData<ArrayList<Files>>()
                result.value=task
                return result
            }
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAudioAlbumList(albumId: String): Flow<MutableList<Audio>> {
        return appDatabase.audioDao().getAudioFromAlbumId(albumId)
    }



}