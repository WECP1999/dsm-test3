package com.example.dsm104_test3

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.dsm104_test3.model.Recurso
import com.example.dsm104_test3.network.RetrofitClient
import com.example.dsm104_test3.util.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class RecursoDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var ivImagen: ImageView
    private lateinit var tvBadgeTipo: TextView
    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvRatingPromedio: TextView
    private lateinit var layoutRating: LinearLayout
    private lateinit var ratingBar: RatingBar
    private lateinit var btnCalificar: Button
    private lateinit var btnOpenLink: Button
    private lateinit var fabFavorito: FloatingActionButton
    private lateinit var session: SessionManager

    private var recurso: Recurso? = null
    private var isFavorito = false
    private var usuarioFavoritos: List<String> = emptyList()
    private var usuarioRatings: Map<String, Float> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recurso_detail)

        session = SessionManager(this)
        toolbar = findViewById(R.id.toolbar)
        ivImagen = findViewById(R.id.ivImagen)
        tvBadgeTipo = findViewById(R.id.tvBadgeTipo)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        tvRatingPromedio = findViewById(R.id.tvRatingPromedio)
        layoutRating = findViewById(R.id.layoutRating)
        ratingBar = findViewById(R.id.ratingBar)
        btnCalificar = findViewById(R.id.btnCalificar)
        btnOpenLink = findViewById(R.id.btnOpenLink)
        fabFavorito = findViewById(R.id.fabFavorito)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val recursoId = intent.getStringExtra("recurso_id") ?: return
        loadData(recursoId)
    }

    private fun loadData(recursoId: String) {
        lifecycleScope.launch {
            try {
                val rRes = RetrofitClient.apiService.getRecurso(recursoId)
                val uRes = RetrofitClient.apiService.getUsers()

                if (rRes.isSuccessful) {
                    recurso = rRes.body()
                    bindRecurso()
                }

                if (uRes.isSuccessful) {
                    val user = uRes.body()?.find { it.id == session.getUserId() }
                    usuarioFavoritos = user?.favoritos ?: emptyList()
                    usuarioRatings = user?.ratings ?: emptyMap()
                    isFavorito = usuarioFavoritos.contains(recursoId)
                    updateFabIcon()

                    val prevRating = usuarioRatings[recursoId] ?: 0f
                    if (prevRating > 0) ratingBar.rating = prevRating
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecursoDetailActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindRecurso() {
        val r = recurso ?: return
        supportActionBar?.title = r.titulo
        tvTitulo.text = r.titulo
        tvDescripcion.text = r.descripcion
        tvBadgeTipo.text = r.tipo.uppercase()
        tvBadgeTipo.setBackgroundColor(getBadgeColor(r.tipo))
        tvRatingPromedio.text = String.format("%.1f ★ (%d votos)", r.rating, r.totalRatings)

        Glide.with(this)
            .load(r.imagen)
            .placeholder(R.drawable.bg_image_placeholder)
            .error(R.drawable.bg_image_placeholder)
            .centerCrop()
            .into(ivImagen)

        // Solo estudiantes pueden calificar y poner favorito
        if (session.isDocente()) {
            layoutRating.visibility = View.GONE
            fabFavorito.visibility = View.GONE
        }

        btnOpenLink.setOnClickListener {
            if (r.enlace.isNotEmpty()) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.enlace)))
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            }
        }

        fabFavorito.setOnClickListener { toggleFavorito() }
        btnCalificar.setOnClickListener { submitRating() }
    }

    private fun toggleFavorito() {
        val r = recurso ?: return
        val newFavoritos = if (isFavorito) {
            usuarioFavoritos.filter { it != r.id }
        } else {
            usuarioFavoritos + r.id
        }

        lifecycleScope.launch {
            try {
                val users = RetrofitClient.apiService.getUsers().body() ?: emptyList()
                val user = users.find { it.id == session.getUserId() } ?: return@launch
                val updated = user.copy(favoritos = newFavoritos)
                val res = RetrofitClient.apiService.updateUser(session.getUserId(), updated)
                if (res.isSuccessful) {
                    isFavorito = !isFavorito
                    usuarioFavoritos = newFavoritos
                    updateFabIcon()
                    val msg = if (isFavorito) R.string.success_favorito else R.string.success_quitar_favorito
                    Toast.makeText(this@RecursoDetailActivity, getString(msg), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecursoDetailActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitRating() {
        val r = recurso ?: return
        val stars = ratingBar.rating
        if (stars == 0f) {
            Toast.makeText(this, getString(R.string.rating_hint), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val users = RetrofitClient.apiService.getUsers().body() ?: emptyList()
                val user = users.find { it.id == session.getUserId() } ?: return@launch

                val prevRating = usuarioRatings[r.id] ?: 0f
                val newRatings = usuarioRatings.toMutableMap().apply { put(r.id, stars) }
                val updatedUser = user.copy(ratings = newRatings)
                RetrofitClient.apiService.updateUser(session.getUserId(), updatedUser)

                // Recalcular promedio del recurso
                val oldTotal = r.totalRatings
                val oldSum = r.rating * oldTotal
                val newTotal = if (prevRating == 0f) oldTotal + 1 else oldTotal
                val newSum = if (prevRating == 0f) oldSum + stars else oldSum - prevRating + stars
                val newAvg = if (newTotal > 0) newSum / newTotal else stars

                val updatedRecurso = r.copy(rating = newAvg, totalRatings = newTotal)
                RetrofitClient.apiService.updateRecurso(r.id, updatedRecurso)
                recurso = updatedRecurso
                usuarioRatings = newRatings
                tvRatingPromedio.text = String.format("%.1f ★ (%d votos)", newAvg, newTotal)
                Toast.makeText(this@RecursoDetailActivity, getString(R.string.success_rating), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@RecursoDetailActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFabIcon() {
        fabFavorito.setImageResource(
            if (isFavorito) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
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
