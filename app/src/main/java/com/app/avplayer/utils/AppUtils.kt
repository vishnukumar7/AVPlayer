package com.app.avplayer.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.app.avplayer.R
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
import com.app.avplayer.model.gallery.GalleryData
import com.app.avplayer.model.video.Video
import com.app.avplayer.model.video.VideoDao
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AppUtils {

    fun getMimetypeImages(mimeType: String): Int {
        if (mimeType.isEmpty())
            return R.drawable.folder
        when (mimeType) {
            "application/vnd.android.package-archive" -> {
                return R.drawable.docx
            }
            "image/jpeg" -> {
                return R.drawable.image
            }
            "image/png" -> {
                return R.drawable.image
            }
            "image/gif" -> {
                return R.drawable.image
            }

            "text/plain" -> {
                return R.drawable.text
            }

            "application/pdf" -> {
                return R.drawable.pdf
            }

            "audio/mp4" -> {
                return com.google.android.exoplayer2.R.drawable.exo_ic_audiotrack
            }
            "application/mp3" -> {
                return com.google.android.exoplayer2.R.drawable.exo_ic_audiotrack
            }
            "video/mp4" -> {
                return R.drawable.video
            }

        }


        return R.drawable.question
    }

    fun getImagesFile(mimeType: String,filesPath: String): Int {
        if (mimeType.isEmpty())
            return R.drawable.folder
        when (mimeType) {
            "application/vnd.android.package-archive" -> {
                return R.drawable.docx
            }
            "image/jpeg" -> {
                return R.drawable.image
            }
            "image/png" -> {
                return R.drawable.image
            }
            "image/gif" -> {
                return R.drawable.image
            }

            "text/plain" -> {
                return R.drawable.text
            }

            "application/pdf" -> {
                return R.drawable.pdf
            }

            "audio/mp4" -> {
                return com.google.android.exoplayer2.R.drawable.exo_ic_audiotrack
            }
            "application/mp3" -> {
                return com.google.android.exoplayer2.R.drawable.exo_ic_audiotrack
            }
            "video/mp4" -> {
                return R.drawable.video
            }

        }


        return R.drawable.question
    }

    companion object {
        var VIEW_LAYOUT_LIST = true
        var VIEW_LAYOUT_VIDEO_LIST = true
        val TAG = "AppUtils"

        @Synchronized
        fun saveAudioDB(context: Context, audioDao: AudioDao): Boolean {
            val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            val selectionArgsMp3 =
                arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3"))
            val audioCursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selectionMimeType,
                selectionArgsMp3,
                null
            )
            if (audioCursor != null) {
                if (audioCursor.moveToFirst()) {
                    do {
                        val idIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val albumIdIndex =
                            audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                        val displayNameIndex =
                            audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                        val albumIndex =
                            audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        val pathIndex =
                            audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                        val sizeIndex =
                            audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                        val durationIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        val dateAddedIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                        val audio = Audio()
                        audio.albumId = audioCursor.getString(albumIdIndex)
                        audio.id = audioCursor.getString(idIndex)
                        audio.displayName = audioCursor.getString(displayNameIndex)
                        audio.size = audioCursor.getString(sizeIndex)
                        audio.duration =
                            if (audioCursor.isNull(durationIndex)) "0" else audioCursor.getString(
                                durationIndex
                            )
                        audio.size =
                            if (audioCursor.isNull(sizeIndex)) "0" else audioCursor.getString(
                                sizeIndex
                            )
                        audio.dateAdded=
                            if (audioCursor.isNull(dateAddedIndex)) "" else audioCursor.getString(
                                dateAddedIndex
                            )
                        var albumText = audioCursor.getString(albumIndex)
                        if (albumText.contains("-"))
                            albumText = albumText.split("-")[0]
                        audio.album = albumText
                        audio.path = audioCursor.getString(pathIndex)
                        audioDao.insert(audio)
                    } while (audioCursor.moveToNext())
                }
            }
            audioCursor?.close()
            return true
        }

        fun saveDocumentDB(context: Context, documentDao: DocumentDao) {
            val cr = context.contentResolver
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            //   val mimeTypeAudio = MimeTypeMap.getSingleton().getMimeTypeFromExtension("audio/ogg")
            val selectionArgsPdf = arrayOf(
                "text/plain",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )
            val files = cr.query(uri, null, selectionMimeType, selectionArgsPdf, null)
            if (files != null) {
                if (files.count > 0 && files.moveToFirst()) {
                    val displayNameIndex =
                        files.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val mimeTypeIndex = files.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                    val sizeIndex = files.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val titleIndex = files.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                    val dataIndex = files.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val dateAddedIndex = files.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                    do {
                        val document = Document()
                        document.mimeType = files.getString(mimeTypeIndex)
                        document.size = files.getString(sizeIndex)
                        document.title = files.getString(titleIndex)
                        document.displayName = files.getString(displayNameIndex)
                        document.path = files.getString(dataIndex)
                        document.dateAdded =  if (files.isNull(dateAddedIndex)) "" else files.getString(
                            dateAddedIndex
                        )
                        documentDao.insert(document)
                    } while (files.moveToNext())
                }
                files.close()
            }

        }

        fun saveFilesDB(context: Context, filesDao: FilesDao) {
            val cr = context.contentResolver
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            //   val mimeTypeAudio = MimeTypeMap.getSingleton().getMimeTypeFromExtension("audio/ogg")
            val selectionArgsPdf = arrayOf(
                "text/plain",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )
            val files = cr.query(uri, null, null, null, null)
            if (files != null) {
                if (files.count > 0 && files.moveToFirst()) {
                    val displayNameIndex =
                        files.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val mimeTypeIndex = files.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                    val bucketDisplayIndex =
                        files.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                    val sizeIndex = files.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val titleIndex = files.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                    val dataIndex = files.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val dateAddedIndex = files.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                    do {
                        if (!(files.getString(dataIndex)
                                .equals("/storage/emulated") || files.getString(dataIndex)
                                .equals("/storage/emulated/0"))
                        ) {
                            val document = Files()
                            document.mimeType =
                                if (files.isNull(mimeTypeIndex)) "" else files.getString(
                                    mimeTypeIndex
                                )
                            document.size =
                                if (files.isNull(sizeIndex)) "" else files.getString(sizeIndex)

                            //    document.title = files.getString(titleIndex)
                            document.displayName = files.getString(displayNameIndex)
                            document.path = files.getString(dataIndex)
                            document.folderName = File(document.path).parent
                            document.files = File(document.path).isFile.toString()
                            document.dateAdded =  if (files.isNull(dateAddedIndex)) "" else files.getString(
                                dateAddedIndex
                            )
                            filesDao.insert(document)
                        }
                    } while (files.moveToNext())
                }
                files.close()
            }

        }

        fun saveDocumentDB1(context: Context) {
            var fileOutputStream: FileOutputStream? = null
            var writer: OutputStreamWriter? = null
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "config.txt")
            } else {
                File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "config.txt")
            }
            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                }
            }
            try {
                fileOutputStream = FileOutputStream(file)
                writer = OutputStreamWriter(fileOutputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val cr = context.contentResolver
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val wantedFile = arrayOf(
                "bucket_display_name",
                "_display_name",
                "mime_type",
                "_size",
                "title",
                "relative_path"
            )

            val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " +
                    MediaStore.Files.FileColumns.MIME_TYPE + "=?"
            //   val mimeTypeAudio = MimeTypeMap.getSingleton().getMimeTypeFromExtension("audio/ogg")
            val selectionArgsPdf = arrayOf(
                "text/plain",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )
            val files = cr.query(uri, null, selectionMimeType, selectionArgsPdf, null)
            if (files != null) {
                if (files.count > 0 && files.moveToFirst()) {
                    for (j in 0 until files.columnCount) {
                        if (wantedFile.contains(files.getColumnName(j)))
                            writer?.append(files.getColumnName(j) + "\t")
                    }
                    writer?.append("\n")
                    do {
                        for (j in 0 until files.columnCount) {
                            try {
                                if (wantedFile.contains(files.getColumnName(j)))
                                    writer?.append(
                                        if (files.isNull(j)) "-\t" else "${
                                            files.getString(
                                                j
                                            )
                                        }\t "
                                    )
                            } catch (e: Exception) {
                                e.toString()
                            }
                        }
                        writer?.append("\n")
                    } while (files.moveToNext())
                }
                files.close()
            }

            try {
                writer?.close()
                fileOutputStream?.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        /*fun writeFile(data:String){

            try {
                val fileOutputStream = FileOutputStream(file)
                val writer = OutputStreamWriter(fileOutputStream)
                writer.append("\n")
                writer.append(data)
                writer.close()
                fileOutputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
*/
        fun saveGalleryDB(context: Context, galleryDao: GalleryDao) {
            val galleryCursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (galleryCursor != null) {
                if (galleryCursor.count > 0 && galleryCursor.moveToFirst()) {
                    val idIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val displayNameIndex =
                        galleryCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val bucketDisplayNameIndex =
                        galleryCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val sizeIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    val albumIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val dateAddedIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    val dateExpiresIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        galleryCursor.getColumnIndex(MediaStore.Images.Media.DATE_EXPIRES)
                    } else {
                        -1
                    }
                    val dateModifyIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                    val dateTakenIndex = galleryCursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    do {
                        val gallery = Gallery()
                        gallery.id =
                            if (galleryCursor.isNull(idIndex)) "" else galleryCursor.getString(
                                idIndex
                            )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            gallery.dateExpired =
                                if (galleryCursor.isNull(dateExpiresIndex)) "" else galleryCursor.getString(
                                    dateExpiresIndex
                                )
                        }
                        gallery.size =
                            if (galleryCursor.isNull(sizeIndex)) "" else galleryCursor.getString(
                                sizeIndex
                            )

                        gallery.dateAdded =
                            if (galleryCursor.isNull(dateAddedIndex)) "" else galleryCursor.getString(
                                dateAddedIndex
                            )

                        gallery.dateModify =
                            if (galleryCursor.isNull(dateModifyIndex)) "" else galleryCursor.getString(
                                dateModifyIndex
                            )

                        gallery.dateTaken =
                            if (galleryCursor.isNull(dateTakenIndex)) "" else galleryCursor.getString(
                                dateTakenIndex
                            )
                        gallery.displayName =
                            if (galleryCursor.isNull(displayNameIndex)) "" else galleryCursor.getString(
                                displayNameIndex
                            )
                        gallery.data =
                            if (galleryCursor.isNull(albumIndex)) "" else galleryCursor.getString(
                                albumIndex
                            )
                        gallery.bucketDisplayName =
                            if (galleryCursor.isNull(bucketDisplayNameIndex)) "Internal Storage" else galleryCursor.getString(
                                bucketDisplayNameIndex
                            )
                        galleryDao.insert(gallery)
                    } while (galleryCursor.moveToNext())
                }
                galleryCursor.close()
            }
        }

        fun saveVideoDB(context: Context, videoDao: VideoDao) {
            val videoCursor: Cursor? = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (videoCursor != null) {
                if (videoCursor.count > 0 && videoCursor.moveToFirst()) {
                    val idIndex = videoCursor.getColumnIndex(MediaStore.Video.Media._ID)
                    val durationIndex =
                        videoCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val bucketDisplayIndex =
                        videoCursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    val displayIndex =
                        videoCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val mimeTypeIndex =
                        videoCursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                    val dataIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DATA)
                    val sizeIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                    val titleIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                    val dateAddedIndex =
                        videoCursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                    val relativePathIndex =
                        videoCursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                    do {
                        val video = Video()
                        video.id = videoCursor.getString(idIndex)
                        video.duration =
                            if (videoCursor.isNull(durationIndex)) "0" else videoCursor.getString(
                                durationIndex
                            )
                        video.bucketDisplay =
                            if (videoCursor.isNull(bucketDisplayIndex)) "" else videoCursor.getString(
                                bucketDisplayIndex
                            )
                        video.display =
                            if (videoCursor.isNull(displayIndex)) "" else videoCursor.getString(
                                displayIndex
                            )
                        video.mimeType =
                            if (videoCursor.isNull(mimeTypeIndex)) "" else videoCursor.getString(
                                mimeTypeIndex
                            )
                        video.data =
                            if (videoCursor.isNull(dataIndex)) "" else videoCursor.getString(
                                dataIndex
                            )
                        video.size =
                            if (videoCursor.isNull(sizeIndex)) "0" else videoCursor.getString(
                                sizeIndex
                            )
                        video.title =
                            if (videoCursor.isNull(titleIndex)) "" else videoCursor.getString(
                                titleIndex
                            )
                        video.dateAdded =
                            if (videoCursor.isNull(dateAddedIndex)) "" else videoCursor.getString(
                                dateAddedIndex
                            )
                        video.relativePath =
                            if (videoCursor.isNull(relativePathIndex)) "" else videoCursor.getString(
                                relativePathIndex
                            )
                        videoDao.insert(video)
                    } while (videoCursor.moveToNext())
                }
                videoCursor.close()
            }
        }

        fun getGalleryAlbum(galleryList: ArrayList<Gallery>): ArrayList<GalleryData> {
            val list  = ArrayList<GalleryData>()
            for (data in galleryList) {
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    map[galleryList[data].bucketDisplayName] = map.getOrDefault(galleryList[data].bucketDisplayName, 0) + 1
                }*/
                var flag=true
                for(value in 0 until list.size){
                    if(data.bucketDisplayName == list[value].bucketDisplayName){
                        flag=false
                        list[value].imageCount=list[value].imageCount+1
                        list[value].lastPath=data.data
                        break
                    }
                }
                if(flag){
                    val cur=GalleryData()
                    cur.bucketDisplayName=data.bucketDisplayName
                    cur.imageCount=1
                    cur.lastPath=data.data
                    list.add(cur)
                }
            }
            return list
        }

        fun getGalleryAlbum(galleryDao: GalleryDao): ArrayList<GalleryData> {
            val galleryList: ArrayList<Gallery> = galleryDao.getAll() as ArrayList<Gallery>
            val list  = ArrayList<GalleryData>()
            for (data in galleryList) {
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    map[galleryList[data].bucketDisplayName] = map.getOrDefault(galleryList[data].bucketDisplayName, 0) + 1
                }*/
                var flag=true
                for(value in 0 until list.size){
                    if(data.bucketDisplayName == list[value].bucketDisplayName){
                        flag=false
                        list[value].imageCount=list[value].imageCount+1
                        list[value].lastPath=data.data
                        break
                    }
                }
                if(flag){
                    val cur=GalleryData()
                    cur.bucketDisplayName=data.bucketDisplayName
                    cur.imageCount=1
                    cur.lastPath=data.data
                    list.add(cur)
                }
            }
            return list
        }

        fun getAlbum(context: Context, albumDao: AlbumDao) {
            val audioCursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                null,
                null,
                null,
                null
            )
            if (audioCursor != null) {
                if (audioCursor.count > 0 && audioCursor.moveToFirst()) {
                    val idIndex = audioCursor.getColumnIndex(MediaStore.Audio.Albums._ID)
                    val albumIndex = audioCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                    val albumIdIndex =
                        audioCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)
                    val numSongsIndex =
                        audioCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
                    val artistIndex = audioCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
                    val numSongByArtistIndex =
                        audioCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST)
                    val artistIdIndex =
                        audioCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST_ID)
                    val artistKeyIndex =
                        audioCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST_KEY)
                    do {
                        val album = Album()
                        album.album = audioCursor.getString(albumIndex)
                        album.albumId = audioCursor.getString(albumIdIndex)
                        album.artist = audioCursor.getString(artistIndex)
                        album.artistKey = audioCursor.getString(artistKeyIndex)
                        album.numSongs = audioCursor.getString(numSongsIndex)
                        album.numSongByArtist = audioCursor.getString(numSongByArtistIndex)
                        album.artistId = audioCursor.getString(artistIdIndex)
                        album.id = audioCursor.getString(idIndex)
                        albumDao.insert(album)
                    } while (audioCursor.moveToNext())
                }
                audioCursor.close()
            }
        }

        fun getAlbumArtBitmap(context: Context, albumId: Long): Bitmap? {
            val sArtworkUri: Uri = Uri
                .parse("content://media/external/audio/albumart")
            val albumArtUri: Uri = ContentUris.withAppendedId(sArtworkUri, albumId)

            Log.d(TAG, "getAlbumArtBitmap: $albumArtUri")
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, albumArtUri)
                bitmap = Bitmap.createScaledBitmap(bitmap, 30, 30, true)
                Log.d(TAG, "getAlbumArtBitmap: found")
            } catch (e: Exception) {
                Log.d(TAG, "getAlbumArtBitmap: not found")
            }
            return bitmap
        }


        fun getAlbumArt(context: Context, albumId: Long): String {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                MediaStore.Audio.Albums._ID + "=?",
                arrayOf(java.lang.String.valueOf(albumId)),
                null
            )

            if (cursor != null) {
                if (cursor.count > 0 && cursor.moveToFirst()) {
                    val albumArtIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                    return cursor.getString(albumArtIndex)
                }
                cursor.close()
            }
            return ""
        }

        fun calculateSeconds(duration: Long): String {
            val value = duration % 60
            if (value < 10) {
                return "0$value"
            }
            return "$value"
        }

        fun calculateMinutes(duration: Long): String {
            val value = duration / 60
            return "$value"
        }

        fun getDateTimeFromStamp(timeStamp: String): String? {
            return try {
                val simpleDateFormat = SimpleDateFormat("dd MMM yy hh:mm:ss", Locale.ENGLISH)
              //  simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")`
                val netDate = Date(timeStamp.toLong() * 1000)
                simpleDateFormat.format(netDate)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ""
            }
        }
    }
}