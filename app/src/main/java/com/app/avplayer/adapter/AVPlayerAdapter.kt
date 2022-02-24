package com.app.avplayer.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.avplayer.R
import com.app.avplayer.activity.*
import com.app.avplayer.databinding.FileGalleryGridItemBinding
import com.app.avplayer.databinding.GalleryGridItemBinding
import com.app.avplayer.databinding.GridItemBinding
import com.app.avplayer.databinding.ListItemBinding
import com.app.avplayer.helper.OnFileClickChanged
import com.app.avplayer.model.album.Album
import com.app.avplayer.model.audio.Audio
import com.app.avplayer.model.document.Document
import com.app.avplayer.model.files.Files
import com.app.avplayer.model.gallery.Gallery
import com.app.avplayer.model.gallery.GalleryData
import com.app.avplayer.model.video.Video
import com.app.avplayer.utils.AppUtils
import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File

class AVPlayerAdapter(
    private var context: Context,
    private var listItem: ArrayList<*>,
    private var type: Int,
    private var screenWidth: Int = 0, private var onFileClickChanged: OnFileClickChanged? = null,private var title:String=""
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val listView by lazy { LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false) }
        val gridView by lazy { LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false) }
        when (type) {
            Constants.DOCUMENT_TYPE -> return DocumentViewHolder(listView)
            Constants.AUDIO_TYPE -> return AudioViewHolder(listView)

            Constants.ALBUM_TYPE -> return if (AppUtils.VIEW_LAYOUT_LIST) { AudioListViewHolder(listView)
            } else AudioGridtViewHolder(gridView)


            Constants.VIDEO_TYPE -> return if (AppUtils.VIEW_LAYOUT_VIDEO_LIST) VideoListViewHolder(listView) else
                VideoGridViewHolder(gridView)

            Constants.GALLERY_TYPE -> return GalleryGridViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.gallery_grid_item, parent, false)
            )
            Constants.FILES_TYPE -> return if (AppUtils.VIEW_LAYOUT_LIST) {
                FilesListViewHolder(listView)
            } else FilesGridtViewHolder(gridView)
            Constants.IMAGE_TYPE -> return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.file_gallery_grid_item, parent, false))

        }

        return AudioViewHolder(listView)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DocumentViewHolder -> {
                val document = listItem[position] as Document
                holder.itemBinding.itemText.text = document.displayName
                when (document.mimeType) {
                    "application/pdf" -> {
                        Glide.with(context).load(R.drawable.pdf).into(holder.itemBinding.albumArt)
                    }
                    "text/plain" -> {
                        Glide.with(context).load(R.drawable.text).into(holder.itemBinding.albumArt)
                    }
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                        Glide.with(context).load(R.drawable.docx).into(holder.itemBinding.albumArt)
                    }
                }

                holder.itemBinding.mainLay.setOnClickListener {

                    when (document.mimeType) {
                        "application/pdf" -> {

                        }
                        "text/plain" -> {
                            val documentActivity = Intent(context, DocumentActivity::class.java)
                            documentActivity.putExtra(Constants.TAG_DATA, document.path)
                            documentActivity.putExtra(Constants.TAG_FROM, "text")
                            documentActivity.putExtra(Constants.TAG_TITLE, document.displayName)
                            context.startActivity(documentActivity)
                        }
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {

                        }
                    }
                }
            }
            is AudioViewHolder -> {
                val audio = listItem[position] as Audio
                holder.itemBinding.itemText.text = audio.displayName
                holder.itemBinding.duration.text =
                    Constants.clockLength(audio.duration.toLong(), false)
                holder.itemBinding.itemSize.text = Constants.getSize(audio.size.toLong())
                Glide.with(context)
                    .load(AppUtils.getAlbumArtBitmap(context, audio.albumId.toLong()))
                    .error(R.drawable.music_logo).into(holder.itemBinding.albumArt)
                holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, PlayAudioActivity::class.java)
                    intent.putExtra(Constants.TAG_DATA, listItem as ArrayList<Audio>)
                    intent.putExtra(Constants.TAG_POSITION, position)
                    context.startActivity(intent)
                }
            }
            is AudioListViewHolder -> {
                val album = listItem[position] as Album
                holder.itemBinding.itemText.text = album.album
                Glide.with(context)
                    .load(AppUtils.getAlbumArtBitmap(context, album.albumId.toLong()))
                    .placeholder(R.drawable.music)
                    .error(R.drawable.music).into(
                        holder.itemBinding.albumArt
                    )
                holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, FileActivity::class.java)
                    intent.putExtra(Constants.TAG_FROM, "Main")
                    intent.putExtra(Constants.TAG_ALBUM_ID, album.albumId)
                    intent.putExtra(Constants.TAG_TITLE, album.album)
                    context.startActivity(intent)
                }
            }
            is AudioGridtViewHolder -> {
                val album = listItem[position] as Album
                holder.itemBinding.mainLay.layoutParams.width = screenWidth
                holder.itemBinding.mainLay.layoutParams.height = screenWidth
                holder.itemBinding.itemText.text = album.album
                Glide.with(context)
                    .load(AppUtils.getAlbumArtBitmap(context, album.albumId.toLong()))
                    .placeholder(R.drawable.music_1024).error(R.drawable.music_1024).into(
                        holder.itemBinding.albumArt
                    )
                holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, FileActivity::class.java)
                    intent.putExtra(Constants.TAG_FROM, "Main")
                    intent.putExtra(Constants.TAG_ALBUM_ID, album.albumId)
                    intent.putExtra(Constants.TAG_TITLE, album.album)
                    context.startActivity(intent)
                }
            }
            is VideoGridViewHolder -> {
                holder.itemGridBinding.mainLay.layoutParams.width = screenWidth
                holder.itemGridBinding.mainLay.layoutParams.height = screenWidth
                val video = listItem[position] as Video
                Glide.with(context).load(File(video.data)).error(R.drawable.music_logo).fitCenter()
                    .into(holder.itemGridBinding.albumArt)
                holder.itemGridBinding.itemText.text = video.title
            }
            is VideoListViewHolder -> {
                val video = listItem[position] as Video
                Glide.with(context).load(File(video.data)).placeholder(R.drawable.video)
                    .error(R.drawable.video).fitCenter()
                    .into(holder.listItemBinding.albumArt)
                holder.listItemBinding.itemText.text = video.title
                holder.listItemBinding.itemSize.text = Constants.getSize(video.size.toLong())
                holder.listItemBinding.duration.text =
                    Constants.clockLength(video.duration.toLong(), true)
                holder.listItemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, VideoPlayActivity::class.java)
                    intent.putExtra(Constants.TAG_DATA, video)
                    context.startActivity(intent)
                }
            }
            is GalleryGridViewHolder -> {
                val galleryData = listItem[position] as GalleryData
                holder.itemBinding.mainLay.layoutParams.width = screenWidth
                holder.itemBinding.mainLay.layoutParams.height = screenWidth
                holder.itemBinding.itemText.text = galleryData.bucketDisplayName
                holder.itemBinding.numOfImages.text = galleryData.imageCount.toString()
                Glide.with(context)
                    .load(File(galleryData.lastPath))
                    .placeholder(R.drawable.image).fitCenter().error(R.drawable.image).into(
                        holder.itemBinding.albumArt
                    )
                holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, FileActivity::class.java)
                    intent.putExtra(Constants.TAG_FROM, "Images")
                    intent.putExtra(Constants.TAG_TITLE, galleryData.bucketDisplayName)
                    context.startActivity(intent)
                }
            }
            is FilesListViewHolder -> {
                val file = listItem[position] as Files
                holder.itemBinding.itemText.text = file.displayName
                Glide.with(context)
                    .load(File(file.path))
                    .placeholder(AppUtils().getMimetypeImages(file.mimeType))
                    .fitCenter()
                    .error(AppUtils().getMimetypeImages(file.mimeType)).into(
                        holder.itemBinding.albumArt
                    )
                holder.itemBinding.mainLay.setOnClickListener {
                    /* val intent = Intent(context, FileActivity::class.java)
                     intent.putExtra(Contants.TAG_FROM, "Main")
                     intent.putExtra(
                         Contants.TAG_DATA,
                         audioDao.getAudioFromAlbumId(album.albumId) as ArrayList<Audio>
                     )
                     intent.putExtra(Contants.TAG_ALBUM_ID, album.albumId)
                     intent.putExtra(Contants.TAG_TITLE, album.album)
                     context.startActivity(intent)*/
                    if (file.files == "false") {
                        onFileClickChanged?.onNextFile(file.folderName + File.separator + file.displayName)
                    }
                }
                holder.itemBinding.mainLay.setOnLongClickListener {
                    if (file.files == "true") {
                        onFileClickChanged?.onShowProgress()
                    }
                    true
                }
            }
            is FilesGridtViewHolder -> {
                val file = listItem[position] as Files
                holder.itemBinding.mainLay.layoutParams.width = screenWidth
                holder.itemBinding.mainLay.layoutParams.height = screenWidth
                holder.itemBinding.itemText.text = file.displayName
                Glide.with(context).load(File(file.path))
                    .placeholder(AppUtils().getMimetypeImages(file.mimeType))
                    .error(AppUtils().getMimetypeImages(file.mimeType)).into(
                        holder.itemBinding.albumArt
                    )
                /*holder.itemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, FileActivity::class.java)
                    intent.putExtra(Contants.TAG_FROM, "Main")
                    intent.putExtra(
                        Contants.TAG_DATA,
                        audioDao.getAudioFromAlbumId(album.albumId) as ArrayList<Audio>
                    )
                    intent.putExtra(Contants.TAG_ALBUM_ID, album.albumId)
                    intent.putExtra(Contants.TAG_TITLE, album.album)
                    context.startActivity(intent)
                }*/
            }

            is ImageViewHolder -> {
                var image= listItem[position] as Gallery

                holder.gridItemBinding.mainLay.layoutParams.height = screenWidth
                Glide.with(context)
                    .load(File(image.data))
                    .placeholder(R.drawable.image).error(R.drawable.image).into(
                        holder.gridItemBinding.albumArt
                    )
                holder.gridItemBinding.mainLay.setOnClickListener {
                    val intent = Intent(context, ImageActivity::class.java)
                    intent.putExtra(Constants.TAG_TITLE, title)
                    intent.putExtra(Constants.TAG_POSITION,position)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = ListItemBinding.bind(itemView.rootView)
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = ListItemBinding.bind(itemView.rootView)
    }

    class AudioListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = ListItemBinding.bind(itemView.rootView)
    }

    class AudioGridtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = GridItemBinding.bind(itemView.rootView)
    }

    class VideoGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemGridBinding = GridItemBinding.bind(itemView)
    }

    class VideoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listItemBinding = ListItemBinding.bind(itemView)
    }

    class GalleryGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = GalleryGridItemBinding.bind(itemView.rootView)
    }

    class FilesListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = ListItemBinding.bind(itemView.rootView)
    }

    class FilesGridtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemBinding = GridItemBinding.bind(itemView.rootView)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gridItemBinding = FileGalleryGridItemBinding.bind(itemView.rootView)
    }



}