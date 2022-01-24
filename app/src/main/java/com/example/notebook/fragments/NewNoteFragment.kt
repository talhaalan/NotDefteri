package com.example.notebook.fragments

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notebook.R
import com.example.notebook.api.NetworkResponse
import com.example.notebook.api.RetrofitInstance
import com.example.notebook.databinding.FragmentNewNoteBinding
import com.example.notebook.models.Note
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.google.firebase.database.DatabaseReference
import com.google.firebase.FirebaseError

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import android.R.string
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import java.io.*
import java.lang.StringBuilder
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import java.lang.Exception
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.firebase.auth.FirebaseAuth

class NewNoteFragment : Fragment(R.layout.fragment_new_note) {

    private var _binding : FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

    var database = FirebaseDatabase.getInstance()

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewNoteBinding.inflate(inflater,container,false)

        val myRef = database.reference
        auth = FirebaseAuth.getInstance()
        binding.fabAddNote.setOnClickListener {
            val noteTitle = binding.etNoteTitle.text.toString().trim()
            val noteBody = binding.etNoteBody.text.toString().trim()


            val id : String = myRef.push().key.toString()

            val note = Note(id,noteTitle,noteBody)

            myRef.child(auth.currentUser!!.uid).child("Notes").child(id).setValue(note).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Not kaydedildi.", Toast.LENGTH_SHORT).show()
                }
            }

            val notesJson = JSONObject()
            try {
                notesJson.put("uid",auth.currentUser!!.uid)
                notesJson.put("noteId",id)
                notesJson.put("noteTitle",noteTitle)
                notesJson.put("noteBody",noteBody)

            } catch (e : JSONException) {
                e.printStackTrace()
            }

            val jsonArray = JSONArray()
            jsonArray.put(notesJson)

            val notesObj = JSONObject()
            notesObj.put("Notes", jsonArray)

            val jsonStr: String = notesObj.toString()


            println("jsonString: $jsonStr")


            /*
            val id: String = database.push().key.toString()

            database = FirebaseDatabase.getInstance().reference

            val note = Note(noteTitle,noteBody)
            database.child("Notes").child(id).setValue(note)
             */

            mCreateAndSaveFile(".json",jsonStr)


            findNavController().navigate(R.id.action_newNoteFragment_to_homeFragment)
        }


        return binding.root
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun mReadJsonData(params: String) {
        try {
            val f = File("/data/data/" + requireContext().packageName.toString() + "/" + params)
            val `is` = FileInputStream(f)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            val mResponse = String(buffer)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun mCreateAndSaveFile(params: String, mJsonResponse: String?) {
        try {
            val file =
                FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
            file.write(mJsonResponse)
            file.flush()
            file.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}