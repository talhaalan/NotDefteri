package com.example.notebook.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.notebook.R
import com.example.notebook.databinding.FragmentUpdateNoteBinding
import com.example.notebook.models.Note
import com.example.notebook.view.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random

class UpdateNoteFragment : Fragment() {

    private var _binding : FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val args : UpdateNoteFragmentArgs by navArgs()
    private lateinit var currentNote : Note
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var storageReference: StorageReference
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

        setUpToolbar()

        currentNote = args.note!!
        auth = FirebaseAuth.getInstance()
        binding.etNoteTitleUpdate.setText(currentNote.noteTitle)
        binding.etNoteBodyUpdate.setText(currentNote.noteBody)
        if (currentNote.noteImage != "") {
            binding.noteImage.visibility = View.VISIBLE
            Glide.with(this).load(currentNote.noteImage).into(binding.noteImage)
        }

        val myRef = database.reference
        storageReference = FirebaseStorage.getInstance().reference

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

        val random = Random.nextInt(9999999)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri : Uri? = result.data?.data
                println("result : " + imageUri)

                val fireRef : StorageReference = storageReference.child("NoteImages").child("photo${random}.jpg")
                imageUri?.let { uri ->
                    binding.noteImage.visibility = View.VISIBLE
                    Glide.with(requireContext()).load(uri).into(binding.noteImage)
                    fireRef.putFile(uri).addOnSuccessListener {
                        if (it.task.isSuccessful) {
                            binding.progressBar.visibility = View.GONE
                            fireRef.downloadUrl.addOnSuccessListener { iuri ->

                                binding.fabUpdate.setOnClickListener {
                                    val title = binding.etNoteTitleUpdate.text.toString().trim()
                                    val body = binding.etNoteBodyUpdate.text.toString().trim()
                                    val image = iuri.toString()

                                    if (title.isNotEmpty()) {
                                        val note = Note(currentNote.noteId,title,body,image)
                                        val updateMap = hashMapOf<String,Any>()
                                        updateMap["noteTitle"] = title
                                        updateMap["noteBody"] = body
                                        updateMap["noteImage"] = image
                                        myRef.child(auth.currentUser!!.uid).child("Notes").child(currentNote.noteId).updateChildren(updateMap)

                                        findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)

                                    }


                                }
                            }
                        } else if (it.task.isCanceled) {
                            binding.progressBar.visibility = View.GONE
                            binding.noteImage.visibility = View.GONE
                        }
                    }

                }

            }
        }


        binding.fabAddImage.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            startForResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
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

    private fun setUpToolbar() {
        binding.toolbar.title = getString(R.string.update_note)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}