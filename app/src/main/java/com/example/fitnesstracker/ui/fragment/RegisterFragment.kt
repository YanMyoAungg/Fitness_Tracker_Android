package com.example.fitnesstracker.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fitnesstracker.R
import com.example.fitnesstracker.databinding.FragmentRegisterBinding
import com.example.fitnesstracker.ui.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                binding.progressBar.isVisible = true
                authViewModel.register(username, email, password)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonGoToLogin.setOnClickListener {
            authViewModel.resetResults() // Clear old results before switching
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        authViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe // Ignore null/reset states

            binding.progressBar.isVisible = false
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            
            if (result.success) {
                authViewModel.resetResults() // Clear result so it doesn't trigger again
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
