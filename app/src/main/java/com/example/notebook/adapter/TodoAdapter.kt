package com.example.notebook.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.notebook.R
import com.example.notebook.models.Todo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TodoAdapter(val context: Context,val todoList : ArrayList<Todo>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    lateinit var listener: ItemClickListener
    lateinit var database: FirebaseDatabase
    lateinit var auth : FirebaseAuth

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox = itemView.findViewById<AppCompatCheckBox>(R.id.checkbox)
        val textViewTodo = itemView.findViewById<AppCompatTextView>(R.id.textViewTodo)
        val cardView = itemView.findViewById<CardView>(R.id.cardView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.todo_view_holder,parent,false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {

        database = FirebaseDatabase.getInstance()
        val todo: Todo = todoList[position]

        val myRef = database.reference
        auth = FirebaseAuth.getInstance()
        holder.textViewTodo.text = todo.title
        holder.checkBox.isChecked = todo.check
        if (todo.check) {
            holder.textViewTodo.paintFlags = holder.textViewTodo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        holder.cardView.setOnClickListener {
            listener.onCurrentTodoClick(todo)
        }

        holder.checkBox.setOnCheckedChangeListener { compoundButton, checked ->
            listener.onItemClick(todo)
            if (checked) {
                val map = hashMapOf<String,Any>()
                map["check"] = true
                myRef.child(auth.currentUser!!.uid).child("Todo").child(todo.id).updateChildren(map)
            } else {
                val map = hashMapOf<String,Any>()
                map["check"] = false
                myRef.child(auth.currentUser!!.uid).child("Todo").child(todo.id).updateChildren(map)
            }

        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    interface ItemClickListener {
        fun onItemClick(list: Todo)
        fun onCurrentTodoClick(list: Todo)
    }

}