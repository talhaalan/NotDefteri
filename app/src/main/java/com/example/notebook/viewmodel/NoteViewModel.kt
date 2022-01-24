package com.example.notebook.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.notebook.api.RetrofitInstance
import com.example.notebook.models.Note
import com.example.notebook.repository.NoteRepository
import kotlinx.coroutines.*
import java.lang.Exception

class NoteViewModel constructor(private val noteRepository: NoteRepository) : ViewModel() {

    private var _note = MutableLiveData<Note>()
    val notes: LiveData<Note>
    get() = _note

/*
    fun getData() {
        viewModelScope.launch {
            try {
                _note.value = RetrofitInstance.getInstance().getNotes("id")
            } catch (e: Exception) {
                Log.e("error",e.message.toString())
            }
        }
    }

 */


}