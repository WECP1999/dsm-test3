package com.example.dsm104_test3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dsm104_test3.adapter.RecursoAdapter
import com.example.dsm104_test3.network.RetrofitClient
import com.example.dsm104_test3.util.SessionManager
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        session = SessionManager(this)
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        loadFavoritos()
    }

    private fun loadFavoritos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val usersRes = RetrofitClient.apiService.getUsers()
                val recursosRes = RetrofitClient.apiService.getRecursos()

                if (usersRes.isSuccessful && recursosRes.isSuccessful) {
                    val user = usersRes.body()?.find { it.id == session.getUserId() }
                    val favoriteIds = user?.favoritos ?: emptyList()
                    val allRecursos = recursosRes.body() ?: emptyList()
                    val favoritos = allRecursos.filter { it.id in favoriteIds }

                    val adapter = RecursoAdapter(this@FavoritosActivity, favoritos) { recurso ->
                        val intent = Intent(this@FavoritosActivity, RecursoDetailActivity::class.java)
                        intent.putExtra("recurso_id", recurso.id)
                        startActivity(intent)
                    }
                    recyclerView.layoutManager = LinearLayoutManager(this@FavoritosActivity)
                    recyclerView.adapter = adapter

                    tvEmpty.visibility = if (favoritos.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (favoritos.isEmpty()) View.GONE else View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@FavoritosActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
