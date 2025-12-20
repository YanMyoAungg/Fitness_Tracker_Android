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
import com.example.fitnesstracker.data.remote.SessionManager
import com.example.fitnesstracker.databinding.FragmentLoginBinding
import com.example.fitnesstracker.ui.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.progressBar.isVisible = true
                authViewModel.login(email, password)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonGoToRegister.setOnClickListener {
            authViewModel.resetResults() // Clear any old results before switching
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        authViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe // Ignore null/reset states

            binding.progressBar.isVisible = false
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            
            if (result.success && result.userId != null) {
                sessionManager.saveUserId(result.userId)
                authViewModel.resetResults() // Clear result so it doesn't trigger again
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
