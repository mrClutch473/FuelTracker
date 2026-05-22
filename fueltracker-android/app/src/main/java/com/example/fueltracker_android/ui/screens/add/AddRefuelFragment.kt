package com.example.fueltracker_android.ui.screens.add

import android.animation.AnimatorInflater
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fueltracker_android.R
import com.example.fueltracker_android.data.AppDependencies
import com.example.fueltracker_android.databinding.FragmentAddRefuelBinding
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.launch

class AddRefuelFragment : Fragment() {

    private var _binding: FragmentAddRefuelBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddRefuelViewModel by viewModels {
        AddRefuelViewModel.factory(AppDependencies.repository)
    }

    // Флаг чтобы не запускать anim_save_btn_enable повторно
    private var isSaveEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRefuelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка изначально неактивна
        binding.btnSave.isEnabled = false
        binding.btnSave.alpha = 0.4f

        setupListeners()
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupListeners() {
        // ── Крестик в хедере — слайд вниз через popBackStack
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        // ── Отмена
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        // ── Сохранить
        binding.btnSave.setOnClickListener {
            val liters = binding.etLiters.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener
            val price = binding.etPrice.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener
            val odometer = binding.etOdometer.text.toString().toIntOrNull()
                ?: return@setOnClickListener
            val note = binding.etNote.text.toString().takeIf { it.isNotBlank() }

            viewModel.addRefuel(liters, price, odometer, note)
        }
    }

    private fun setupTextWatchers() {
        // Общий watcher — пересчитывает итог и проверяет валидность полей
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                updateTotal()
                updateSaveButton()
            }
        }

        binding.etLiters.addTextChangedListener(watcher)
        binding.etPrice.addTextChangedListener(watcher)
        binding.etOdometer.addTextChangedListener(watcher)
    }

    // ── Живой расчёт суммы с анимацией
    private fun updateTotal() {
        val liters = binding.etLiters.text.toString().toDoubleOrNull()
        val price  = binding.etPrice.text.toString().toDoubleOrNull()

        if (liters != null && price != null && liters > 0 && price > 0) {
            val total = liters * price

            // Обновляем текст
            binding.tvTotal.text    = "${total.toInt()} ₽"
            binding.tvPerLiter.text = "${String.format("%.2f", price)} ₽/л"

            // ── anim_total_update: мигание/пульс при изменении суммы
            AnimatorInflater
                .loadAnimator(requireContext(), R.animator.anim_total_update)
                .apply {
                    setTarget(binding.tvTotal)
                    start()
                }
        } else {
            binding.tvTotal.text    = "0 ₽"
            binding.tvPerLiter.text = "— ₽/л"
        }
    }

    // ── Включение/выключение кнопки Save с анимацией
    private fun updateSaveButton() {
        val litersOk   = binding.etLiters.text.toString().toDoubleOrNull()?.let { it > 0 } == true
        val priceOk    = binding.etPrice.text.toString().toDoubleOrNull()?.let { it > 0 } == true
        val odometerOk = binding.etOdometer.text.toString().toIntOrNull()?.let { it > 0 } == true

        val allValid = litersOk && priceOk && odometerOk

        if (allValid && !isSaveEnabled) {
            // ── Поля заполнены → плавно активируем кнопку
            isSaveEnabled = true
            binding.btnSave.isEnabled = true
            AnimatorInflater
                .loadAnimator(requireContext(), R.animator.anim_save_btn_enable)
                .apply {
                    setTarget(binding.btnSave)
                    start()
                }

        } else if (!allValid && isSaveEnabled) {
            // ── Поля стали невалидны → гасим кнопку обратно
            isSaveEnabled = false
            binding.btnSave.isEnabled = false
            binding.btnSave.animate()
                .alpha(0.4f)
                .setDuration(200)
                .start()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.btnSave.isEnabled = false
                        binding.btnSave.alpha = 0.6f
                    }
                    is UiState.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Заправка сохранена!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController()
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refuel_added", true)

                        findNavController().popBackStack()
                    }
                    is UiState.Error -> {
                        // Возвращаем кнопку если ошибка
                        binding.btnSave.isEnabled = true
                        binding.btnSave.alpha = 1f
                        Toast.makeText(
                            requireContext(),
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}