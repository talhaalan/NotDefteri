package com.example.notebook.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.notebook.api.RetrofitInstance
import com.example.notebook.databinding.ActivityMainBinding
import com.example.notebook.repository.NoteRepository
import com.example.notebook.viewmodel.NoteViewModel
import com.example.notebook.viewmodel.ViewModelProviderFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //setSupportActionBar(binding.toolbar)

        val noteRepository = NoteRepository(RetrofitInstance.getInstance())
        val viewModelProviderFactory = ViewModelProviderFactory(noteRepository)
        noteViewModel = ViewModelProvider(this,viewModelProviderFactory)[NoteViewModel::class.java]

    }

}