package com.kranin.application

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AddItemLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.add_project);

        val btnAddProject = findViewById<MaterialButton>(R.id.btn_AddProject)

        btnAddProject.setOnClickListener {
            val projectName = findViewById<TextInputEditText>(R.id.editProjectName)
                .text
                .toString()
                .trim()
            val projectJson = findViewById<TextInputEditText>(R.id.editHost)
                .text
                .toString()
                .trim()
            val projectVk = findViewById<TextInputEditText>(R.id.editVk)
                .text
                .toString()
                .trim()
            val projectTG = findViewById<TextInputEditText>(R.id.editTg)
                .text
                .toString()
                .trim()
            val projectDS = findViewById<TextInputEditText>(R.id.editDs)
                .text
                .toString()
                .trim()
            onAddProjectClick(projectName, projectJson, projectVk, projectTG, projectDS)
        }
    }

    private fun onAddProjectClick(
        projectName: String,
        projectJson: String,
        projectVk: String,
        projectTG: String,
        projectDS: String
    ) {
        createProjectFolder(projectName)

        sendPost(projectJson, projectVk, projectTG, projectDS, projectName)

        //Toast.makeText(this, "Проект создан", Toast.LENGTH_SHORT).show()
    }

    private fun createProjectFolder(projectName: String) {
        val projectBaseDir = File(getExternalFilesDir(null), "project")
        val dir = File(projectBaseDir, projectName)

        if (!dir.exists()) {
            val ok = dir.mkdirs()
            if (ok) {
                //Toast.makeText(this, "Папка $projectName создана: ${dir.absolutePath}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось создать папку", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Папка $projectName уже существует", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendPost(
        projectJson: String,
        projectVk: String,
        projectTG: String,
        projectDS: String,
        projectName: String
    ) {
        val client = OkHttpClient()
        val url = "http://192.168.0.14:5000/projects/create"
        val json = JSONObject().apply {
            put("name", projectName)
            put("json", projectJson)
            put("vk", projectVk)
            put("tg", projectTG)
            put("ds", projectDS)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@AddItemLauncher,
                        "Ошибка отправки: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && respBody != null) {
                        try {
                            val json = JSONObject(respBody)
                            val message = json.optString("message", "Нет сообщения")

                            Toast.makeText(
                                this@AddItemLauncher,
                                message,
                                Toast.LENGTH_LONG
                            ).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@AddItemLauncher, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 3000)
                        } catch (_: Exception) {
                            Toast.makeText(
                                this@AddItemLauncher,
                                "Ошибка парсинга ответа",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@AddItemLauncher,
                            "Ошибка сервера: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        })
    }


}