package com.example.notebook.api

import com.example.notebook.models.Note
import com.example.notebook.models.Todo
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiInterface {

    //suspend fun getNotes() : NetworkResponse<Note,Error>

    @GET("/{uid}/Notes/{noteId}.json")
    suspend fun getNotes(@Path("uid") uid: String, @Path("noteId") noteId: String) : NetworkResponse<Note,Error>

    @GET("/{uid}/Todo/{id}.json")
    suspend fun getTodo(@Path("uid") uid: String, @Path("id") id: String) : NetworkResponse<Todo,Error>

}
