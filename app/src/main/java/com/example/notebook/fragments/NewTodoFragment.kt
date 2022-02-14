package com.example.notebook.fragments


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notebook.R
import com.google.firebase.database.*
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import com.example.notebook.databinding.FragmentNewTodoBinding
import com.example.notebook.models.Todo
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notebook.adapter.TodoAdapter
import com.example.notebook.view.MainActivity
import com.google.android.material.snackbar.Snackbar

import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.ItemTouchHelper


class NewTodoFragment : Fragment(R.layout.fragment_new_todo) {

    private var _binding: FragmentNewTodoBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageReference: StorageReference
    lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    lateinit var todoList: ArrayList<Todo>
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewTodoBinding.inflate(inflater, container, false)

        setUpToolbar()
        database = FirebaseDatabase.getInstance()


        auth = FirebaseAuth.getInstance()
        val myRef = database.reference

        val viewModel = (activity as MainActivity).noteViewModel

        database.reference.child(auth.currentUser!!.uid).child("Todo")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val key = i.key.toString()

                        viewModel.todoArrayList.observe(viewLifecycleOwner, Observer {
                            println("it: " + it)
                            todoList = it
                            todoAdapter = TodoAdapter(requireContext(), todoList)


                            todoAdapter.listener = object : TodoAdapter.ItemClickListener {
                                override fun onItemClick(list: Todo) {

                                    findNavController().navigate(R.id.action_newTodoFragment_self)

                                }

                                override fun onCurrentTodoClick(list: Todo) {
                                    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                                    val lInflater = requireActivity().layoutInflater
                                    val dialogView = lInflater.inflate(R.layout.todo_alert_view, null)
                                    alertDialog.setView(dialogView)
                                    val alert: AlertDialog = alertDialog.create()
                                    val etTodoTitle = dialogView.findViewById<EditText>(R.id.etTodoTitle)
                                    val addTodo = dialogView.findViewById<ImageView>(R.id.add_todo)
                                    val close = dialogView.findViewById<AppCompatButton>(R.id.buttonClose)
                                    etTodoTitle.showKeyboard()

                                    etTodoTitle.append(list.title)

                                    alert.show()
                                    alert.window?.setGravity(Gravity.BOTTOM)

                                    addTodo.setOnClickListener {
                                        val title = etTodoTitle.text.toString().trim()
                                        val map = hashMapOf<String,Any>()
                                        map["title"] = title

                                        myRef.child(auth.currentUser!!.uid).child("Todo").child(list.id).updateChildren(map)
                                        alert.cancel()
                                        findNavController().navigate(R.id.action_newTodoFragment_self)

                                    }

                                    close.setOnClickListener {
                                        alert.cancel()
                                    }
                                }

                            }

                            binding.recyclerView.setHasFixedSize(true)
                            binding.recyclerView.layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                            binding.recyclerView.adapter = todoAdapter

                        })
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                        viewModel.getTodoData(auth.currentUser!!.uid, key)
                    }

                    ItemTouchHelper(object :
                        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean {

                            return false
                        }

                        override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                        ) {

                            val deletedTodo: Todo =
                                todoList[viewHolder.adapterPosition]

                            val position = viewHolder.adapterPosition

                            myRef.child(auth.currentUser!!.uid).child("Todo").child(deletedTodo.id).removeValue()

                            todoList.removeAt(viewHolder.adapterPosition)

                            todoAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                            Snackbar.make(
                                binding.recyclerView,"Silindi: "+ deletedTodo.title,
                                Snackbar.LENGTH_LONG
                            ).setAction("Geri al") {
                                todoList.add(position, deletedTodo)
                                myRef.child(auth.currentUser!!.uid).child("Todo")
                                    .child(deletedTodo.id).setValue(deletedTodo)
                                todoAdapter.notifyItemInserted(position)
                            }.show()
                        }

                    }).attachToRecyclerView(binding.recyclerView)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("error",error.message)
                }

            })

        binding.fabAddTodo.setOnClickListener {

            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val lInflater = this.layoutInflater
            val dialogView = lInflater.inflate(R.layout.todo_alert_view, null)
            alertDialog.setView(dialogView)
            val alert: AlertDialog = alertDialog.create()
            val etTodoTitle = dialogView.findViewById<EditText>(R.id.etTodoTitle)
            val addTodo = dialogView.findViewById<ImageView>(R.id.add_todo)
            val close = dialogView.findViewById<AppCompatButton>(R.id.buttonClose)
            etTodoTitle.showKeyboard()

            alert.show()
            alert.window?.setGravity(Gravity.BOTTOM)

            addTodo.setOnClickListener {
                val id: String = myRef.push().key.toString()
                val title = etTodoTitle.text.toString().trim()
                val check = false
                val todo = Todo(id, title, check)

                myRef.child(auth.currentUser!!.uid).child("Todo").child(id).setValue(todo)
                alert.cancel()
                findNavController().navigate(R.id.action_newTodoFragment_self)

            }

            close.setOnClickListener {
                alert.cancel()
            }
        }

        binding.fabSaveTodo.setOnClickListener {
            findNavController().navigate(R.id.action_newTodoFragment_to_homeFragment)
        }

        return binding.root
    }

    fun EditText.showKeyboard() {
        post {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    private fun setUpToolbar() {
        binding.toolbar.title = "Yapılacaklar listesi"
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_newTodoFragment_to_homeFragment)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}