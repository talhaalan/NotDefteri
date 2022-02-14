package com.example.notebook.fragments


import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notebook.R
import com.example.notebook.databinding.FragmentNewNoteBinding
import com.example.notebook.models.Note
import com.google.firebase.database.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import android.R.string
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.test.core.app.ApplicationProvider
import java.io.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.random.Random

class NewNoteFragment : Fragment(R.layout.fragment_new_note) {

    private var _binding : FragmentNewNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageReference: StorageReference
    var database = FirebaseDatabase.getInstance()
    private lateinit var auth : FirebaseAuth
    private lateinit var startForResult: ActivityResultLauncher<Intent>

    var image = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(true)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewNoteBinding.inflate(inflater,container,false)

        setUpToolbar()

        val myRef = database.reference
        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val random = Random.nextInt(9999999)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result: ActivityResult ->
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

                                binding.fabAddNote.setOnClickListener {

                                    val noteTitle = binding.etNoteTitle.text.toString().trim()
                                    val noteBody = binding.etNoteBody.text.toString().trim()
                                    val noteImage = iuri.toString()

                                    val id : String = myRef.push().key.toString()

                                    val note = Note(id,noteTitle,noteBody,noteImage)

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
                                        notesJson.put("noteImage",noteImage)

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
            startForResult.launch(Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }


        binding.fabAddNote.setOnClickListener {


            val noteTitle = binding.etNoteTitle.text.toString().trim()
            val noteBody = binding.etNoteBody.text.toString().trim()
            val noteImage = ""
            val times = System.currentTimeMillis()

            val id : String = myRef.push().key.toString()

            if (noteTitle.isNotEmpty() && noteBody.isNotEmpty()) {
                val note = Note(id,noteTitle,noteBody,noteImage,times.toString())
                myRef.child(auth.currentUser!!.uid).child("Notes").child(id).setValue(note).addOnCompleteListener { task->
                    if (task.isSuccessful) {
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

                mCreateAndSaveFile(".json",jsonStr)


                findNavController().navigate(R.id.action_newNoteFragment_to_homeFragment)

            } else {
                Snackbar.make(it,"Lütfen başlık ve açıklama girin",Snackbar.LENGTH_LONG).setAction("Tamam"
                ) {}.show()
            }
        }

        return binding.root
    }

    private fun setUpToolbar() {
        binding.toolbar.title = getString(R.string.add_note)
        binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_newNoteFragment_to_homeFragment)
        }
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