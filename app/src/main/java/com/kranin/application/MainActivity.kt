package com.kranin.application

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kranin.application.adapter.ProjectAdapter
import com.kranin.application.databinding.ActivityMainBinding
import com.kranin.application.model.ItemProject
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectAdapter
    private val projectList = mutableListOf<ItemProject>()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerViewProjects
        recyclerView.layoutManager = LinearLayoutManager(this)

        createProjectFolder()
        listProjects()

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddItemLauncher::class.java)
            startActivity(intent)
        }
    }

    private fun listProjects() {
        val projectBaseDir = File(getExternalFilesDir(null), "project")
        if (!projectBaseDir.exists()) projectBaseDir.mkdirs()

        val folders = projectBaseDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        projectList.clear()

        if (folders.isEmpty()) {
            projectList.add(ItemProject(name = "Нет проектов", isEmpty = true))
        } else {
            folders.forEach { folder ->
                projectList.add(ItemProject(folder.name))
            }
        }

        adapter = ProjectAdapter(
            projectList,
            onDelete = { project ->
                val dirToDelete = File(projectBaseDir, project.name)
                if (dirToDelete.exists()) {
                    dirToDelete.deleteRecursively()
                    listProjects()
                    Toast.makeText(this, "Проект ${project.name} удалён", Toast.LENGTH_SHORT).show()
                }
            },
            onOpen = { project ->
                val intent = Intent(this, ProjectManagementActivity::class.java)
                intent.putExtra("projectName", project.name) // передаём имя проекта
                startActivity(intent)
            }
        )


        recyclerView.adapter = adapter
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }



    private fun createProjectFolder() {
        val projectDir = File(getExternalFilesDir(null), "project")

        if (!projectDir.exists()) {
            val created = projectDir.mkdirs()
            if (created) {
                Log.d("PROJECT", "Папка project создана")
            } else {
                Log.e("PROJECT", "Ошибка создания папки")
            }
        } else {
            Log.d("PROJECT", "Папка project уже существует")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}