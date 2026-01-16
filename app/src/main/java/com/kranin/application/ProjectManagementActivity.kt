package com.kranin.application

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.FileProvider
class ProjectManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_edit)

        val projectName = intent.getStringExtra("projectName")
        findViewById<TextView>(R.id.projectNameText).text = projectName


        val downloadBtn = findViewById<MaterialButton>(R.id.downloadBtn)

        downloadBtn.setOnClickListener {
            downloadApkWithProgress(projectName);
        }

    }

    private fun downloadApkWithProgress(projectName: String?) {
        if (projectName.isNullOrEmpty()) return

        val apkUrl = "http://l.bindhost.com/$projectName/signed.apk"
        val apkFileName = "signed.apk"

        val progressBar = findViewById<ProgressBar>(R.id.downloadProgress)
        val downloadBtn = findViewById<MaterialButton>(R.id.downloadBtn)

        progressBar.progress = 0
        downloadBtn.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(apkUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val fileLength = connection.contentLength

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProjectManagementActivity, "Ошибка при скачивании", Toast.LENGTH_SHORT).show()
                        downloadBtn.isEnabled = true
                    }
                    return@launch
                }

                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir != null && !downloadsDir.exists()) downloadsDir.mkdirs()
                val apkFile = File(downloadsDir, apkFileName)

                connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val data = ByteArray(8 * 1024)
                        var total: Long = 0
                        var count: Int
                        while (input.read(data).also { count = it } != -1) {
                            output.write(data, 0, count)
                            total += count
                            if (fileLength > 0) {
                                val progress = (total * 100 / fileLength).toInt()
                                withContext(Dispatchers.Main) {
                                    progressBar.progress = progress
                                }
                            }
                        }
                        output.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    downloadBtn.isEnabled = true
                    Toast.makeText(this@ProjectManagementActivity, "APK скачан: $apkFileName", Toast.LENGTH_SHORT).show()

                    // Запуск установки
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            this@ProjectManagementActivity,
                            "${packageName}.fileprovider",
                            apkFile
                        ).also { _ -> intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    } else {
                        Uri.fromFile(apkFile)
                    }
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    startActivity(intent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    downloadBtn.isEnabled = true
                    Toast.makeText(this@ProjectManagementActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}
