package com.app.avplayer.helper

import android.util.Log
import androidx.lifecycle.*
import com.app.avplayer.model.album.Album
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.document.Document
import com.app.avplayer.model.files.Files
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.video.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AVPViewModel(private val appRepository: AppRepository) : ViewModel() {
    private val TAG = "AVPViewModel"

    val audioList: LiveData<MutableList<Audio>> = appRepository.audioList.asLiveData()
    var audioAlbumList: LiveData<MutableList<Audio>> = MutableLiveData()
    val audioListLiked: LiveData<MutableList<Audio>> = appRepository.audioList.asLiveData()
    val albumList: LiveData<MutableList<Album>> = appRepository.albumList.asLiveData()
    val documentList: LiveData<MutableList<Document>> = appRepository.documentList.asLiveData()
    var filesList: LiveData<MutableList<Files>> = appRepository.filesList.asLiveData()
    var filesLists: LiveData<MutableList<Files>> =MutableLiveData()
    val galleryList: LiveData<MutableList<Gallery>> = appRepository.galleryList.asLiveData()
    var galleryTitleList: LiveData<MutableList<Gallery>> = MutableLiveData()
    val videoList: LiveData<MutableList<Video>> = appRepository.videoList.asLiveData()

    fun insert(audio: Audio) = viewModelScope.launch {
        appRepository.insert(audio)
    }

    fun insert(document: Document) = viewModelScope.launch {
        appRepository.insert(document)
    }

    fun insert(files: Files) = viewModelScope.launch {
        appRepository.insert(files)
    }

    fun insert(gallery: Gallery) = viewModelScope.launch {
        appRepository.insert(gallery)
    }

    fun insert(video: Video) = viewModelScope.launch {
        appRepository.insert(video)
    }

    fun insert(album: Album) = viewModelScope.launch {
        appRepository.insert(album)
    }

    fun update(audio: Audio) = viewModelScope.launch {
        appRepository.update(audio)
    }

    fun update(document: Document) = viewModelScope.launch {
        appRepository.update(document)
    }

    fun update(files: Files) = viewModelScope.launch {
        appRepository.update(files)
    }

    fun update(gallery: Gallery) = viewModelScope.launch {
        appRepository.update(gallery)
    }

    fun update(video: Video) = viewModelScope.launch {
        appRepository.update(video)
    }

    fun update(album: Album) = viewModelScope.launch {
        appRepository.update(album)
    }

    fun getListFromTitle(title: String) = appRepository.getListFromTitle(title)

    fun getFileList(path: String, orderBy: String, sortBy: String)= appRepository.getFileList(path, orderBy, sortBy)

    fun getAudioAlbumList(albumId: String) = appRepository.getAudioAlbumList(albumId)


    class AVPViewModelFactory(private val appRepository: AppRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AVPViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AVPViewModel(appRepository) as T
            }
            throw IllegalArgumentException("Unknown VieModel Class")
        }
    }

}