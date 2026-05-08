package com.example.dsm104_test3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dsm104_test3.adapter.GestionAdapter
import com.example.dsm104_test3.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class GestionRecursosActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: GestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_recursos)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAdd = findViewById(R.id.fabAdd)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        adapter = GestionAdapter(this, emptyList(),
            onEdit = { recurso ->
                val intent = Intent(this, AddEditRecursoActivity::class.java)
                intent.putExtra("recurso_id", recurso.id)
                startActivity(intent)
            },
            onDelete = { recurso ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_delete_title))
                    .setMessage(getString(R.string.confirm_delete_message))
                    .setPositiveButton(getString(R.string.btn_confirm)) { _, _ -> deleteRecurso(recurso.id) }
                    .setNegativeButton(getString(R.string.btn_cancel), null)
                    .show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditRecursoActivity::class.java))
        }

        loadRecursos()
    }

    override fun onResume() {
        super.onResume()
        loadRecursos()
    }

    private fun loadRecursos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getRecursos()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    adapter.updateData(lista)
                    tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
                } else {
                    Toast.makeText(this@GestionRecursosActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GestionRecursosActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun deleteRecurso(id: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteRecurso(id)
                if (response.isSuccessful) {
                    Toast.makeText(this@GestionRecursosActivity, getString(R.string.success_deleted), Toast.LENGTH_SHORT).show()
                    loadRecursos()
                } else {
                    Toast.makeText(this@GestionRecursosActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GestionRecursosActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
