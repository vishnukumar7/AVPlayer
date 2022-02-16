package com.app.avplayer.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityImageBinding
import com.app.avplayer.databinding.ImageViewBinding
import com.app.avplayer.model.gallery.Gallery

import com.app.avplayer.utils.Constants
import com.bumptech.glide.Glide
import java.io.File

class ImageActivity : BaseActivity() {

    lateinit var binding: ActivityImageBinding
    var listItem= ArrayList<Gallery>()
    lateinit var adapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        //  requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_image)
        avpViewModel.getListFromTitle(intent.getStringExtra(Constants.TAG_TITLE)!!)
       // val list =
          //  galleryDao.getList(intent.getStringExtra(Contants.TAG_TITLE)!!) as ArrayList<Gallery>
        adapter = ViewPagerAdapter(this, listItem)
        avpViewModel.galleryTitleList.observe(this){
            it?.let {
                listItem.clear()
                listItem.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
        binding.viewPager.adapter = adapter
        binding.info.setOnClickListener {
            val intent = Intent(this@ImageActivity, InformationActivity::class.java)
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_IMAGES)
            intent.putExtra(Constants.TAG_DATA, listItem[binding.viewPager.currentItem])
            startActivity(intent)
        }

    }

    inner class ViewPagerAdapter(
        private var context: Context,
        private var numOfImages: ArrayList<Gallery>
    ) : PagerAdapter() {

        var layoutInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        override fun getCount(): Int {
            return numOfImages.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj as LinearLayout
        }


        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding: ImageViewBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.image_view, container, false)
            Glide.with(context).load(File(numOfImages[position].data)).placeholder(R.drawable.image)
                .error(R.drawable.image).into(binding.imageView)
            container.addView(binding.root)
            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as LinearLayout)
        }

    }
}