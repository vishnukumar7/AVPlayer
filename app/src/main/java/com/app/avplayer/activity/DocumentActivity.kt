package com.app.avplayer.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.app.avplayer.R
import com.app.avplayer.databinding.ActivityDocumentBinding
import com.app.avplayer.utils.Constants
import java.io.*

class DocumentActivity : BaseActivity() {

    lateinit var binding: ActivityDocumentBinding
    lateinit var list: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_document)
        if (intent.getStringExtra(Constants.TAG_FROM).equals("text")) {
            binding.textViewer.visibility = View.VISIBLE
            binding.textViewer.text = readFromFile(intent.getStringExtra(Constants.TAG_DATA)!!)
        } else if (intent.getStringExtra(Constants.TAG_FROM).equals("html")) {
            /* binding.htmlViewer.visibility=View.VISIBLE
             binding.htmlViewer.setMarkDownText(readFromFile(intent.getStringExtra(Contants.TAG_DATA)!!))*/
        }
        binding.backBtn.setOnClickListener { finish() }

        binding.title.text = intent.getStringExtra(Constants.TAG_TITLE)


    }

    private fun readFromFile(filepath: String): String {
        var ret = ""
        try {
            val inputStream: InputStream = FileInputStream(File(filepath))
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String? = ""
            val stringBuilder = StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append("\n").append(receiveString)
            }
            inputStream.close()
            ret = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }
        return ret
    }
}