package com.example.notebook.adapter

import android.content.Context
import android.graphics.drawable.Drawable

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.findNavController

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.notebook.R
import com.example.notebook.fragments.HomeFragmentDirections
import com.example.notebook.fragments.UpdateNoteFragmentArgs
import com.example.notebook.models.Note


class NoteAdapter(val context: Context,val note: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        var textBody: TextView = itemView.findViewById(R.id.tvNoteBody)
        var imageView: ImageView = itemView.findViewById(R.id.imageView)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.note_layout_viewholder, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val note = note[position]
        holder.textTitle.text = note.noteTitle
        holder.textBody.text = note.noteBody


        if (note.noteImage != "") {
            Glide.with(context).load(note.noteImage).listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }

            }).into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
            holder.progressBar.visibility = View.GONE
        }



        holder.itemView.setOnClickListener {
            val directions = HomeFragmentDirections.actionHomeFragmentToUpdateNoteFragment(note)
            it.findNavController().navigate(directions)
        }

    }

    override fun getItemCount(): Int {
        return note.size
    }

}