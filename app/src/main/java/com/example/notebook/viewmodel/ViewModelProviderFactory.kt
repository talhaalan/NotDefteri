package com.example.notebook.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notebook.repository.NoteRepository
import java.lang.IllegalArgumentException

class ViewModelProviderFactory(private val noteRepository: NoteRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            NoteViewModel(this.noteRepository) as T
        } else {
            throw IllegalArgumentException("Not found")
        }
    }

}