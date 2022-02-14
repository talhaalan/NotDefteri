package com.example.notebook.fragments

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notebook.R
import com.example.notebook.databinding.FragmentHomeBinding
import com.example.notebook.models.Note
import com.example.notebook.viewmodel.NoteViewModel

import com.example.notebook.view.MainActivity
import android.view.LayoutInflater

import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import java.io.*
import java.util.*
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList
import com.example.notebook.adapter.NoteAdapter


class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var database: DatabaseReference
    var backupName: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var noteList: ArrayList<Note>

    companion object {
        val TAG = "retrofit"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setHasOptionsMenu(true)

        getPermission()



    }

    private fun getPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { _: Boolean ->

            }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                {}
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel = (activity as MainActivity).noteViewModel

        setUpToolbar()





        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun setUpToolbar() {
        binding.toolbar.title = "Notlar"
        binding.toolbar.inflateMenu(R.menu.home_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.home_save -> {
                    auth = FirebaseAuth.getInstance()

                    val request = DownloadManager
                        .Request(Uri.parse("https://notes-f3388-default-rtdb.europe-west1.firebasedatabase.app/${auth.currentUser!!.uid}.json"))
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    request.setTitle("yedek.json")
                    request.setDescription("File is downloading...")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "/backup/yedek.json"
                    )

                    val manager =
                        activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                    manager.enqueue(request)
                    true
                }
                R.id.home_read -> {
                    auth = FirebaseAuth.getInstance()

                    var selectFile = Intent(Intent.ACTION_GET_CONTENT)
                    selectFile.type = "application/json"
                    selectFile = Intent.createChooser(selectFile, "Choose a file")
                    startForResult.launch(selectFile)
                    true
                }
                R.id.home_exit -> {

                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(getString(R.string.exit))
                        setMessage(getString(R.string.exit_message))
                        setPositiveButton(
                            getString(R.string.exit),
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                auth = FirebaseAuth.getInstance()
                                sharedPreferences = requireActivity().getSharedPreferences(
                                    "sharedPref",
                                    Context.MODE_PRIVATE
                                )
                                editor = sharedPreferences.edit()
                                editor.putBoolean("rememberMe", false)
                                editor.apply()
                                editor.apply()
                                auth.signOut()

                                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                            })
                        setNegativeButton(getString(R.string.cancel), null)
                    }.create().show()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)



        binding.fabAddTodo.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_newTodoFragment)
        }

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_newNoteFragment)
        }

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageUri: Uri = result.data?.data!!

                    val path = imageUri.path
                    if (imageUri.toString().startsWith("content://")) {
                        var myCursor: Cursor? = null

                        try {
                            myCursor = requireActivity().contentResolver.query(
                                imageUri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                backupName = myCursor.getString(
                                    myCursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                                )
                                println("myC: " + backupName)

                            }
                        } finally {
                            myCursor?.close()
                        }


                    }
                    if (path != null) {
                        //imagePath = path.replace(path.toString(),"yedek.json")
                        backupName?.let { getPath(it) }
                    }


                }
            }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        getData()


        /*
         val query = FirebaseDatabase.getInstance()
            .reference
            .child(auth.currentUser!!.uid)
            .child("Notes")
            .limitToLast(50)



        val options: FirebaseRecyclerOptions<Note?> = FirebaseRecyclerOptions.Builder<Note>()
            .setQuery(query,Note::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter =
            object : FirebaseRecyclerAdapter<Note?, NoteViewHolder?>(options) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {

                    val mView: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.note_layout_viewholder, parent, false)
                    return NoteViewHolder(mView)
                }


                override fun onBindViewHolder(p0: NoteViewHolder, p1: Int, p2: Note) {
                    val noteId = getRef(p1).key.toString()

                    val pos =  p0.adapterPosition

                    println("noteId: " + noteId)

                    val service = RetrofitInstance.getInstance()

                    GlobalScope.launch {
                        val response = service.getNotes(auth.currentUser!!.uid,noteId)

                        when(response) {
                            is NetworkResponse.Success -> {

                                Log.d(TAG,"noteTitle ${response.body.noteTitle}")
                                //getDatas(response.body.noteId,response.body.noteTitle,response.body.noteBody)

                                database = FirebaseDatabase.getInstance().reference
                                    .child(auth.currentUser!!.uid).child("Notes")
                                database.child(noteId).addValueEventListener(object :ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        binding.recyclerView.visibility = View.VISIBLE

                                        val title = response.body.noteTitle
                                        val body = response.body.noteBody

                                        println("resbody: " + response.body)

                                        val note = Note(noteId,title,body)
                                        val arrayListNote = arrayListOf(note)

                                        val adapter = NoteAdapter(requireContext(),arrayListNote)

                                        //p0.disableProgress(true)
                                        p0.setNoteTitle(note.noteTitle)
                                        p0.setNoteBody(note.noteBody)

                                        p0.itemView.setOnClickListener {
                                            val direction = HomeFragmentDirections.actionHomeFragmentToUpdateNoteFragment(p2)
                                            it.findNavController().navigate(direction)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            }
                            is NetworkResponse.ApiError -> Log.d(TAG,"ApiError ${response.body.message}")
                            is NetworkResponse.NetworkError -> Log.d(TAG,"NetworkError")
                            is NetworkResponse.UnknownError -> Log.d(TAG,"UnknownError")
                        }
                    }



                }
            }
         */
        //binding.recyclerView.adapter = adapter


        return binding.root
    }

    private fun getData() {
        val noteViewModel = (activity as MainActivity).noteViewModel
        database.child(auth.currentUser!!.uid).child("Notes").orderByChild("time")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val key = i.key.toString()

                        noteViewModel.noteArrayList.observe(
                            viewLifecycleOwner,
                            androidx.lifecycle.Observer {

                                binding.recyclerView.apply {
                                    layoutManager = StaggeredGridLayoutManager(
                                        2,
                                        StaggeredGridLayoutManager.VERTICAL
                                    )
                                    setHasFixedSize(true)
                                }
                                it.let { note ->
                                    val noteAdapter = NoteAdapter(requireContext(), note)
                                    //Log.d(TAG,"noteTitle ${response.body.noteTitle}")
                                    binding.recyclerView.adapter = noteAdapter
                                }

                            })
                        binding.recyclerView.adapter?.notifyDataSetChanged()
                        noteViewModel.getData(auth.currentUser!!.uid, key)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun getPath(backupName: String) {
        println("imagep: " + backupName)
        if (backupName.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val file = File("/storage/emulated/0/Download/backup", backupName)
                val fis = FileInputStream(file)
                val size = fis.available()
                val buffer = ByteArray(size)
                fis.read(buffer)
                fis.close()
                val mResponse = String(buffer)
                println("data Res " + mResponse)
                val jsonMap = Gson().fromJson<Any>(
                    mResponse,
                    object : TypeToken<HashMap<String, Any>>() {}.type
                )

                val rootRef = FirebaseDatabase.getInstance().reference.child(auth.currentUser!!.uid)
                rootRef.setValue(jsonMap)
                requireActivity().recreate()
            } else {
                val folder: File =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                println("folder1: " + folder)
                val files = File(
                    requireContext().getExternalFilesDir(null)!!.absolutePath,
                    "/Download/backup"
                )
                println("folder2: " + files)
                try {
                    val file = File(folder, backupName)
                    val fis = FileInputStream(file)
                    val size = fis.available()
                    val buffer = ByteArray(size)
                    fis.read(buffer)
                    fis.close()
                    val mResponse = String(buffer)
                    println("data Res " + mResponse)
                    val jsonMap = Gson().fromJson<Any>(
                        mResponse,
                        object : TypeToken<HashMap<String, Any>>() {}.type
                    )

                    val rootRef = FirebaseDatabase.getInstance().reference
                    rootRef.setValue(jsonMap)
                    requireActivity().recreate()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}