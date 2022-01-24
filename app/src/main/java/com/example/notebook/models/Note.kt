package com.example.notebook.models

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Note(
    val noteId : String = "",
    val noteTitle : String = "",
    val noteBody : String = ""
    ) : Parcelable
