package com.kranin.application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.kranin.application.R
import com.kranin.application.model.ItemProject

class ProjectAdapter(
    private val items: List<ItemProject>,
    private val onDelete: (ItemProject) -> Unit,
    private val onOpen: (ItemProject) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.projectName)
        val deleteBtn: Button = view.findViewById(R.id.openBtn)
        val card: MaterialCardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = items[position]
        holder.nameText.text = project.name

        // Удаление
        holder.deleteBtn.setOnClickListener {
            onDelete(project)
        }

        holder.card.setOnClickListener {
            if (!project.isEmpty) {
                onOpen(project)
            }
        }
    }



    override fun getItemCount(): Int = items.size
}
