package com.example.notebook.adapter
import android.view.View
import android.widget.ProgressBar
import androidx.cardview.widget.CardView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.R


class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mView: View = itemView
    var textTitle: TextView
    var textBody: TextView


    fun setNoteTitle(title: String?) {
        textTitle.text = title
    }

    fun setNoteBody(body: String?) {
        textBody.text = body
    }

    init {
        textTitle = mView.findViewById(R.id.tvNoteTitle)
        textBody = mView.findViewById(R.id.tvNoteBody)
    }
}