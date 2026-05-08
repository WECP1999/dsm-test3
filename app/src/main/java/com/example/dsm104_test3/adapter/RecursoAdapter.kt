package com.example.dsm104_test3.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dsm104_test3.R
import com.example.dsm104_test3.model.Recurso

class RecursoAdapter(
    private val context: Context,
    private var items: List<Recurso>,
    private val onClick: (Recurso) -> Unit
) : RecyclerView.Adapter<RecursoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivImagen)
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvBadgeTipo: TextView = view.findViewById(R.id.tvBadgeTipo)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recurso, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recurso = items[position]
        holder.tvTitulo.text = recurso.titulo
        holder.tvBadgeTipo.text = recurso.tipo.uppercase()
        holder.tvDescripcion.text = recurso.descripcion
        holder.tvRating.text = String.format("%.1f / 5.0", recurso.rating)
        holder.tvBadgeTipo.setBackgroundColor(getBadgeColor(recurso.tipo))

        Glide.with(context)
            .load(recurso.imagen)
            .placeholder(R.drawable.bg_image_placeholder)
            .error(R.drawable.bg_image_placeholder)
            .centerCrop()
            .into(holder.ivImagen)

        holder.itemView.setOnClickListener { onClick(recurso) }
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
