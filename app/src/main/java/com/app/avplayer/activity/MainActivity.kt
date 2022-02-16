package com.app.avplayer.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityFragmentMainBinding
import com.app.avplayer.utils.AppUtils
import com.google.android.material.navigation.NavigationBarView
import java.io.File


class MainActivity : BaseActivity(), NavigationBarView.OnItemSelectedListener,
    View.OnClickListener {
    lateinit var binding: ActivityFragmentMainBinding
    private var mContent: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_fragment_main)
        binding.bottomNavigation.setOnItemSelectedListener(this)
        binding.viewLayout.setOnClickListener(this)
binding.backBtn.setOnClickListener {
    val fileFragment=mContent as FilesFragment
    fileFragment.onNextFile(File(fileFragment.currentPath).parent!!)
}
        loadFragment(MainFragment())
    }


    private fun loadFragment(fragment: Fragment) {
        // load fragment
        mContent=fragment
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.song -> {
                binding.viewLayout.visibility=View.VISIBLE
                binding.backBtn.visibility=View.GONE
                loadFragment(MainFragment())
            }

            R.id.video -> {
                binding.backBtn.visibility=View.GONE
                binding.viewLayout.visibility = View.VISIBLE
                loadFragment(VideoFragment())
            }

            R.id.gallery -> {
                binding.backBtn.visibility=View.GONE
                binding.viewLayout.visibility = View.GONE
                loadFragment(GalleryFragment())
            }

            R.id.document -> {
                binding.backBtn.visibility=View.GONE
                binding.viewLayout.visibility = View.GONE
                loadFragment(DocumentFragment())
            }

            R.id.files -> {
                binding.viewLayout.visibility = View.VISIBLE
                loadFragment(FilesFragment())
            }

            else -> {
                binding.backBtn.visibility=View.GONE
                binding.viewLayout.visibility=View.VISIBLE
                loadFragment(MainFragment())
            }

        }
        return true
    }

    override fun onBackPressed() {
        when {
            mContent is FilesFragment && (mContent as FilesFragment).currentPath != "/storage/emulated/0" -> {
                val fileFragment=mContent as FilesFragment
                fileFragment.onNextFile(File(fileFragment.currentPath).parent!!)
            }
            (mContent !is MainFragment) -> {
                loadFragment(MainFragment())
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(v: View?) {
        when (v!!.id) {
          /*  R.id.navBtn -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }*/
            R.id.viewLayout -> {
                when (mContent) {
                    is MainFragment -> {
                        AppUtils.VIEW_LAYOUT_LIST = !AppUtils.VIEW_LAYOUT_LIST
                        if(AppUtils.VIEW_LAYOUT_LIST)
                            binding.viewLayout.setImageDrawable(resources.getDrawable(R.drawable.grid_view,null))
                        else
                            binding.viewLayout.setImageDrawable(getDrawable(R.drawable.ic_baseline_list_24))
                        (mContent as MainFragment).getList(1)
                    }
                    is VideoFragment -> {
                        AppUtils.VIEW_LAYOUT_VIDEO_LIST = !AppUtils.VIEW_LAYOUT_VIDEO_LIST
                        if (AppUtils.VIEW_LAYOUT_VIDEO_LIST)
                            binding.viewLayout.setImageDrawable(getDrawable(R.drawable.grid_view))
                        else
                            binding.viewLayout.setImageDrawable(getDrawable(R.drawable.ic_baseline_list_24))
                        (mContent as VideoFragment).getList(1)
                    }
                    is FilesFragment -> {
                        AppUtils.VIEW_LAYOUT_VIDEO_LIST = !AppUtils.VIEW_LAYOUT_VIDEO_LIST
                        if (AppUtils.VIEW_LAYOUT_VIDEO_LIST)
                            binding.viewLayout.setImageDrawable(getDrawable(R.drawable.grid_view))
                        else
                            binding.viewLayout.setImageDrawable(getDrawable(R.drawable.ic_baseline_list_24))
                        (mContent as FilesFragment).getList()
                    }
                }
            }
        }
    }
}