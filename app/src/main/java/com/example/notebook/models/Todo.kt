package com.example.notebook.models

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Todo (
    val id : String = "",
    val title : String = "",
    val check : Boolean = false
        ) : Parcelable