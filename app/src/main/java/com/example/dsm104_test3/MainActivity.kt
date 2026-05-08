package com.example.dsm104_test3

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
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
import com.example.dsm104_test3.model.Recurso
import com.example.dsm104_test3.network.RetrofitClient
import com.example.dsm104_test3.util.SessionManager
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var etSearch: TextInputEditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: RecursoAdapter
    private lateinit var session: SessionManager

    private var allRecursos: List<Recurso> = emptyList()
    private var currentFilter = ""
    private var currentSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        session = SessionManager(this)
        toolbar = findViewById(R.id.toolbar)
        etSearch = findViewById(R.id.etSearch)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "${getString(R.string.recursos_title)} — ${session.getUsername()}"

        adapter = RecursoAdapter(this, emptyList()) { recurso ->
            val intent = Intent(this, RecursoDetailActivity::class.java)
            intent.putExtra("recurso_id", recurso.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupSearch()
        setupChipFilter()
        loadRecursos()
    }

    override fun onResume() {
        super.onResume()
        loadRecursos()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.menu_favoritos))
        if (session.isDocente()) {
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.menu_gestion))
        }
        menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.btn_logout))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                startActivity(Intent(this, FavoritosActivity::class.java))
                true
            }
            2 -> {
                startActivity(Intent(this, GestionRecursosActivity::class.java))
                true
            }
            3 -> {
                session.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentSearch = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupChipFilter() {
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                checkedIds.contains(R.id.chipLibro) -> "libro"
                checkedIds.contains(R.id.chipVideo) -> "video"
                checkedIds.contains(R.id.chipArticulo) -> "articulo"
                checkedIds.contains(R.id.chipTutorial) -> "tutorial"
                else -> ""
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        var filtered = allRecursos

        if (currentSearch.isNotEmpty()) {
            filtered = filtered.filter { r ->
                r.id.contains(currentSearch, ignoreCase = true) ||
                r.titulo.contains(currentSearch, ignoreCase = true) ||
                r.tipo.contains(currentSearch, ignoreCase = true)
            }
        }

        if (currentFilter.isNotEmpty()) {
            filtered = filtered.filter { r ->
                r.tipo.lowercase()
                    .replace("í", "i").replace("á", "a")
                    .contains(currentFilter.lowercase())
            }
        }

        adapter.updateData(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadRecursos() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getRecursos()
                if (response.isSuccessful) {
                    allRecursos = response.body() ?: emptyList()
                    applyFilters()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.GONE
        }
    }
}
