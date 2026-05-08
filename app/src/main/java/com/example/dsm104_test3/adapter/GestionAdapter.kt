package com.example.dsm104_test3.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dsm104_test3.R
import com.example.dsm104_test3.model.Recurso

class GestionAdapter(
    private val context: Context,
    private var items: List<Recurso>,
    private val onEdit: (Recurso) -> Unit,
    private val onDelete: (Recurso) -> Unit
) : RecyclerView.Adapter<GestionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvBadgeTipo: TextView = view.findViewById(R.id.tvBadgeTipo)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recurso_gestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recurso = items[position]
        holder.tvTitulo.text = recurso.titulo
        holder.tvBadgeTipo.text = recurso.tipo.uppercase()
        holder.tvDescripcion.text = recurso.descripcion
        holder.tvBadgeTipo.setBackgroundColor(getBadgeColor(recurso.tipo))

        holder.btnEdit.setOnClickListener { onEdit(recurso) }
        holder.btnDelete.setOnClickListener { onDelete(recurso) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Recurso>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun getBadgeColor(tipo: String): Int {
        return when (tipo.lowercase().trim()) {
            "libro" -> Color.parseColor("#1565C0")
            "video" -> Color.parseColor("#C62828")
            "artículo", "articulo" -> Color.parseColor("#2E7D32")
            "tutorial" -> Color.parseColor("#6A1B9A")
            else -> Color.parseColor("#37474F")
        }
    }
}
