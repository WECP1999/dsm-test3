package com.example.dsm104_test3

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.dsm104_test3.model.Recurso
import com.example.dsm104_test3.network.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddEditRecursoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var etTitulo: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var etTipo: TextInputEditText
    private lateinit var etEnlace: TextInputEditText
    private lateinit var etImagen: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var progressBar: ProgressBar

    private var recursoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_recurso)

        toolbar = findViewById(R.id.toolbar)
        etTitulo = findViewById(R.id.etTitulo)
        etDescripcion = findViewById(R.id.etDescripcion)
        etTipo = findViewById(R.id.etTipo)
        etEnlace = findViewById(R.id.etEnlace)
        etImagen = findViewById(R.id.etImagen)
        btnGuardar = findViewById(R.id.btnGuardar)
        progressBar = findViewById(R.id.progressBar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recursoId = intent.getStringExtra("recurso_id")
        if (recursoId != null) {
            supportActionBar?.title = getString(R.string.edit_recurso_title)
            loadRecurso(recursoId!!)
        } else {
            supportActionBar?.title = getString(R.string.add_recurso_title)
        }

        btnGuardar.setOnClickListener { saveRecurso() }
    }

    private fun loadRecurso(id: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getRecurso(id)
                if (response.isSuccessful) {
                    val r = response.body() ?: return@launch
                    etTitulo.setText(r.titulo)
                    etDescripcion.setText(r.descripcion)
                    etTipo.setText(r.tipo)
                    etEnlace.setText(r.enlace)
                    etImagen.setText(r.imagen)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditRecursoActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun saveRecurso() {
        val titulo = etTitulo.text?.toString()?.trim() ?: ""
        val descripcion = etDescripcion.text?.toString()?.trim() ?: ""
        val tipo = etTipo.text?.toString()?.trim() ?: ""
        val enlace = etEnlace.text?.toString()?.trim() ?: ""
        val imagen = etImagen.text?.toString()?.trim() ?: ""

        if (titulo.isEmpty() || descripcion.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        val recurso = Recurso(
            titulo = titulo,
            descripcion = descripcion,
            tipo = tipo,
            enlace = enlace,
            imagen = imagen
        )

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = if (recursoId != null) {
                    RetrofitClient.apiService.updateRecurso(recursoId!!, recurso)
                } else {
                    RetrofitClient.apiService.createRecurso(recurso)
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@AddEditRecursoActivity, getString(R.string.success_saved), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddEditRecursoActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditRecursoActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnGuardar.isEnabled = !loading
    }
}
