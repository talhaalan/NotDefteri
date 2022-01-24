package com.example.notebook.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notebook.R
import com.example.notebook.databinding.FragmentLoginBinding
import com.example.notebook.databinding.FragmentRegisterBinding
import com.example.notebook.databinding.FragmentUpdateNoteBinding
import com.example.notebook.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater,container,false)

        auth = FirebaseAuth.getInstance()

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

        val rem = sharedPreferences.getBoolean("rememberMe",false)
        if (rem) {
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }

        binding.textViewRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {

                if (it.isSuccessful) {
                    if (binding.checkbox.isChecked) {
                        editor.putBoolean("rememberMe",true)
                        editor.apply()
                        editor.commit()
                    }
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(),"Bilgilerinizi kontrol edin..",Toast.LENGTH_LONG).show()
                }
            }
        }

        return binding.root
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}