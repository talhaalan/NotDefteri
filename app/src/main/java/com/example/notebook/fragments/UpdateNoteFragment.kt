package com.example.notebook.fragments

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notebook.R
import com.example.notebook.databinding.FragmentUpdateNoteBinding
import com.example.notebook.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class UpdateNoteFragment : Fragment() {

    private var _binding : FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val args : UpdateNoteFragmentArgs by navArgs()
    private lateinit var currentNote : Note
    var database = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateNoteBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentNote = args.note!!
        auth = FirebaseAuth.getInstance()
        binding.etNoteTitleUpdate.setText(currentNote.noteTitle)
        binding.etNoteBodyUpdate.setText(currentNote.noteBody)

        val myRef = database.reference

        binding.fabDelete.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.delete_note))
                setMessage(getString(R.string.delete_note_message))
                setPositiveButton(getString(R.string.delete), DialogInterface.OnClickListener { dialogInterface, i ->
                    myRef.child(auth.currentUser!!.uid).child("Notes").child(currentNote.noteId).removeValue()
                    findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)
                })
                setNegativeButton(getString(R.string.cancel),null)
            }.create().show()



        }

        binding.fabUpdate.setOnClickListener {
            val title = binding.etNoteTitleUpdate.text.toString().trim()
            val body = binding.etNoteBodyUpdate.text.toString().trim()

            if (title.isNotEmpty()) {
                val note = Note(currentNote.noteId,title,body)
                val updateMap = hashMapOf<String,Any>()
                updateMap["noteTitle"] = title
                updateMap["noteBody"] = body
                myRef.child(auth.currentUser!!.uid).child("Notes").child(currentNote.noteId).updateChildren(updateMap)

                findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)

            }


        }


    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}