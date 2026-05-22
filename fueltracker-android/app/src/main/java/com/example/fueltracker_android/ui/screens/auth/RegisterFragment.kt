package com.example.fueltracker_android.ui.screens.auth

import android.animation.AnimatorInflater
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.fueltracker_android.databinding.FragmentRegisterBinding
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModel.factory(AppDependencies.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetState()

        startEntranceAnimations()
        startGlowAnimation()
        setupFormValidation()
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

    // ── Валидация формы (без серверного запроса) ──────────────────────────────

    private fun setupFormValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                // Сбрасываем серверные ошибки при редактировании
                binding.tilEmail.error = null
                binding.tilConfirmPassword.error = null
                validateForm()
            }
        }
        binding.etEmail.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)
        binding.etConfirmPassword.addTextChangedListener(watcher)
    }

    private fun validateForm() {
        val email    = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm  = binding.etConfirmPassword.text?.toString().orEmpty()

        val valid = email.isNotBlank()
            && password.length >= 6
            && confirm == password

        binding.btnRegister.isEnabled = valid
        binding.btnRegister.animate()
            .alpha(if (valid) 1f else 0.4f)
            .setDuration(200)
            .start()
    }

    // ── Навигация и клики ─────────────────────────────────────────────────────

    private fun setupNavigation() {
        // Таб и ссылка → LoginFragment
        binding.tabLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
        binding.tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        // Кнопка «Создать аккаунт» → запрос к серверу
        binding.btnRegister.setOnClickListener {
            val email    = binding.etEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            val confirm  = binding.etConfirmPassword.text?.toString().orEmpty()

            // Двойная проверка паролей перед отправкой
            if (confirm != password) {
                shakeConfirmPasswordField("Пароли не совпадают")
                return@setOnClickListener
            }

            viewModel.register(email, password)
        }
    }

    // ── Наблюдение за состоянием ──────────────────────────────────────────────

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is UiState.Loading -> setLoadingState(true)

                    is UiState.Success -> {
                        // Регистрация прошла — переходим в основное приложение
                        setLoadingState(false)
                        findNavController().navigate(R.id.action_register_to_home)
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
        binding.btnRegister.isEnabled = !loading
        binding.btnRegister.text = if (loading) "Регистрация..." else "Создать аккаунт"
        binding.btnRegister.alpha = if (loading) 0.7f else 1f
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirmPassword.isEnabled = !loading
    }

    private fun showError(message: String) {
        if (message.contains("email", ignoreCase = true) ||
            message.contains("зарегистрирован", ignoreCase = true)) {
            // Ошибка связана с email — показываем на поле email
            binding.tilEmail.error = message
            binding.tilEmail.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.anim_shake)
            )
        } else {
            // Общая ошибка — показываем на поле подтверждения пароля
            shakeConfirmPasswordField(message)
        }
    }

    private fun shakeConfirmPasswordField(errorMessage: String) {
        binding.tilConfirmPassword.error = errorMessage
        binding.tilConfirmPassword.startAnimation(
            AnimationUtils.loadAnimation(requireContext(), R.anim.anim_shake)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
