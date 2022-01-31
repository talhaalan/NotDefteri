package com.example.notebook.adapter

import android.content.Context

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController

import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.R
import com.example.notebook.fragments.HomeFragmentDirections
import com.example.notebook.fragments.UpdateNoteFragmentArgs
import com.example.notebook.models.Note


class NoteAdapter(val context: Context,val note: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        var textBody: TextView = itemView.findViewById(R.id.tvNoteBody)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.note_layout_viewholder, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val note = note[position]
        holder.textTitle.text = note.noteTitle
        holder.textBody.text = note.noteBody

        holder.itemView.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToUpdateNoteFragment(note)
            it.findNavController().navigate(directions)
        }

    }

    override fun getItemCount(): Int {
        return note.size
    }

}