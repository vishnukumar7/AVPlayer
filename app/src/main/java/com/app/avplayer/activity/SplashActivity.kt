package com.app.avplayer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.app.avplayer.AVPlayerApplication
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivitySplashBinding
import com.app.avplayer.helper.AppDatabase
import com.app.avplayer.utils.AppUtils
import javax.inject.Inject


class SplashActivity : BaseActivity() {
    private val TAG="SplashActivity"
    lateinit var binding: ActivitySplashBinding
    companion object {
        val permission = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
      binding=DataBindingUtil.setContentView(this, R.layout.activity_splash)
appDatabase=(application as AVPlayerApplication).database

    }

    fun updateProgressValues(value: Int){
        runOnUiThread(Runnable {
            binding.progressCircular.setProgress(((value * 100)/6).toFloat(),true)
        })
    }

    @SuppressLint("StaticFieldLeak")
    inner class GetData(private var context: Context) : AsyncTask<Void, Int, Void>() {

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            Log.d(TAG, "onProgressUpdate: values : $values")

        }
        override fun doInBackground(vararg params: Void?): Void? {
            AppUtils.saveVideoDB(context, appDatabase.videoDao())
           updateProgressValues(1)
            AppUtils.getAlbum(context, appDatabase.albumDao())
            updateProgressValues(2)
            AppUtils.saveGalleryDB(context, appDatabase.galleryDao())
            updateProgressValues(3)
            AppUtils.saveDocumentDB(context, appDatabase.documentDao())
            updateProgressValues(4)
            AppUtils.saveFilesDB(context, appDatabase.filesDao())
            updateProgressValues(5)
            AppUtils.saveAudioDB(context, appDatabase.audioDao())
            updateProgressValues(6)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            openMainPage()
        }

    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(permission, 100)
        } else getImageFromExternal()
    }

    private fun getImageFromExternal() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                GetData(this).execute()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            GetData(this).execute()
        }
    }

    fun openMainPage() {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
}