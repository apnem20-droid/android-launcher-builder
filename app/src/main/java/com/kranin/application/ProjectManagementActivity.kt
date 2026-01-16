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
import androidx.core.content.FileProvider
class ProjectManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.project_edit)

        val projectName = intent.getStringExtra("projectName")
        findViewById<TextView>(R.id.projectNameText).text = projectName


        val downloadBtn = findViewById<MaterialButton>(R.id.downloadBtn)

        downloadBtn.setOnClickListener {
            downloadApk(projectName);
        }

    }

    private fun downloadApk(projectName: String?) {
        if (projectName.isNullOrEmpty()) return

        val apkUrl = "http://l.bindhost.com/download/bind361.apk"
        val apkFileName = "bind361.apk"
        Log.e("APK_DOWNLOAD", "Начинаю скачивание")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(apkUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProjectManagementActivity, "Ошибка при скачивании", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir != null && !downloadsDir.exists()) downloadsDir.mkdirs()
                val apkFile = File(downloadsDir, apkFileName)

                connection.inputStream.use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.flush()
                    }
                }

                if (!apkFile.exists() || apkFile.length() == 0L) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProjectManagementActivity, "Ошибка записи APK", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProjectManagementActivity, "APK скачан: $apkFileName", Toast.LENGTH_SHORT).show()

                    // Запуск установки APK
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            this@ProjectManagementActivity,
                            "${packageName}.fileprovider",
                            apkFile
                        ).also { _ ->
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    } else {
                        Uri.fromFile(apkFile)
                    }

                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    startActivity(intent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProjectManagementActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
