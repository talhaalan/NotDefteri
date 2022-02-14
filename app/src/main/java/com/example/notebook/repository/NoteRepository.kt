package com.example.notebook.repository

import com.example.notebook.api.ApiInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class NoteRepository constructor(private val apiInterface: ApiInterface) {
    lateinit var auth : FirebaseAuth
    lateinit var database : DatabaseReference

    suspend fun getAllNotes(uid:String,noteId: String) = apiInterface.getNotes(uid,noteId)

    suspend fun getAllTodo(uid:String,id: String) = apiInterface.getTodo(uid,id)

}