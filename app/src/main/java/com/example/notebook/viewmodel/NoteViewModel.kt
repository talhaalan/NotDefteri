package com.example.notebook.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notebook.adapter.NoteAdapter
import com.example.notebook.api.NetworkResponse
import com.example.notebook.fragments.HomeFragment.Companion.TAG
import com.example.notebook.models.Note
import com.example.notebook.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import java.util.ArrayList

class NoteViewModel constructor(private val noteRepository: NoteRepository) : ViewModel() {


    var job : Job? = null
    lateinit var auth : FirebaseAuth
    lateinit var database : DatabaseReference
    var noteArrayList = MutableLiveData<ArrayList<Note>>()
    lateinit var noteList : ArrayList<Note>

    fun getData(uid: String,noteId : String) {
        database = FirebaseDatabase.getInstance().getReference("Notes")
        auth = FirebaseAuth.getInstance()

        noteList = ArrayList()

        job = CoroutineScope(Dispatchers.IO).launch {
            val response = noteRepository.getAllNotes(uid,noteId)
            withContext(Dispatchers.Main) {
                when (response) {
                    is NetworkResponse.Success -> {
                        val note : Note = response.body
                        noteList.add(0,note)
                        noteArrayList.postValue(noteList)

                    }
                    is NetworkResponse.ApiError -> {
                        Log.d(TAG,"ApiError ${response.body.message}")
                    }
                    is NetworkResponse.NetworkError -> {
                        Log.d(TAG,"NetworkError")
                    }
                    is NetworkResponse.UnknownError -> {
                        Log.d(TAG,"UnknownError")
                    }
                }
            }
        }
    }


/*
    fun getNote () : LiveData<ArrayList<Note>> {
        noteList = ArrayList()
        auth = FirebaseAuth.getInstance()
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = noteRepository.getAllNoteList()
            withContext(Dispatchers.Main) {
                response.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        noteList.clear()
                        for (s in snapshot.children) {
                            val note: Note? = s.getValue(Note::class.java)
                            if (note != null) {
                                noteList.add(0, note)
                            }
                            println("note: " + noteArrayList)
                            noteArrayList.postValue(noteList)
                            isLoadingNote.value = false
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
        return noteArrayList
    }

 */
    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }




}