package com.example.fueltracker_android.ui.screens.auth

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fueltracker_android.R
import com.example.fueltracker_android.data.AppDependencies
import com.example.fueltracker_android.databinding.FragmentLoginBinding
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModel.factory(AppDependencies.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetState()

        startEntranceAnimations()
        startGlowAnimation()
        setupNavigation()
        observeViewModel()
    }

    // ── Анимации появления ────────────────────────────────────────────────────

    private fun startEntranceAnimations() {
        val enterAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_auth_enter)
        binding.logoGroup.startAnimation(enterAnim)

        binding.authCard.postDelayed({
            if (isAdded) {
                binding.authCard.startAnimation(
                    AnimationUtils.loadAnimation(requireContext(), R.anim.anim_auth_enter)
                )
            }
        }, 120L)
    }

    private fun startGlowAnimation() {
        AnimatorInflater.loadAnimator(requireContext(), R.animator.anim_breathe)
            .apply { setTarget(binding.btnGlow); start() }
    }

    // ── Навигация и клики ─────────────────────────────────────────────────────

    private fun setupNavigation() {
        // Таб и ссылка → RegisterFragment
        binding.tabRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Кнопка «Войти» → запрос к серверу
        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                shakeEmailField()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }
    }

    // ── Наблюдение за состоянием ──────────────────────────────────────────────

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is UiState.Loading -> setLoadingState(true)

                    is UiState.Success -> {
                        // Авторизация прошла — переходим в основное приложение
                        setLoadingState(false)
                        findNavController().navigate(R.id.action_login_to_home)
                    }

                    is UiState.Error -> {
                        setLoadingState(false)
                        showError(state.message)
                    }

                    null -> setLoadingState(false)
                }
            }
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun setLoadingState(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Вход..." else "Войти"
        binding.btnLogin.alpha = if (loading) 0.7f else 1f
    }

    private fun showError(message: String) {
        // Показываем ошибку под полем email + shake-анимация
        shakeEmailField()
        binding.tilEmail.error = message
    }

    private fun shakeEmailField() {
        binding.tilEmail.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.anim_shake)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
