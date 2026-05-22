package com.example.fueltracker_android.ui.screens.home

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fueltracker_android.R
import com.example.fueltracker_android.data.AppDependencies
import com.example.fueltracker_android.databinding.FragmentHomeBinding
import com.example.fueltracker_android.domain.model.Refuel
import com.example.fueltracker_android.domain.model.Stats
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var breatheAnimator: Animator? = null

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.factory(AppDependencies.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Сразу очищаем все XML-placeholder значения
        clearPlaceholders()

        startEnterAnimations()
        startBreatheAnimation()
        setupListeners()
        observeViewModel()
        observeNavResult()
    }

    private fun observeNavResult() {
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refuel_added")
            ?.observe(viewLifecycleOwner) { added ->
                if (added == true) {
                    // Сбрасываем флаг, чтобы не сработал повторно
                    findNavController()
                        .currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("refuel_added", false)

                    viewModel.loadData()
                }
            }
    }

    // Сбрасываем все захардкоженные значения из XML до реальных данных
    private fun clearPlaceholders() {
        binding.tvConsumption.text = "— л/100 км"
        binding.tvLastDate.text = "—"
        binding.tvLastTotal.text = "—"
        binding.tvLastOdometer.text = "—"
        binding.tvLastLiters.text = "—"
        binding.tvMonthSpent.text = "—"
        binding.tvMonthConsumption.text = "—"
    }

    private fun startEnterAnimations() {
        listOf(
            binding.fuelGaugeBlock,
            binding.cardLastRefuel,
            binding.cardMonthSpent,
            binding.cardMonthConsumption,
            binding.btnRefuel
        ).forEachIndexed { i, v ->
            AnimatorInflater
                .loadAnimator(requireContext(), R.animator.anim_fade_slide_up)
                .apply {
                    startDelay = i * 80L
                    setTarget(v)
                    start()
                }
        }
    }

    private fun startBreatheAnimation() {
        breatheAnimator = AnimatorInflater
            .loadAnimator(requireContext(), R.animator.anim_breathe)
            .apply {
                setTarget(binding.btnGlow)
                start()
            }
    }

    private fun setupListeners() {
        binding.btnRefuel.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                AnimatorInflater
                    .loadAnimator(requireContext(), R.animator.anim_button_press)
                    .apply {
                        setTarget(v)
                        start()
                    }
            }
            false
        }

        binding.btnRefuel.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addRefuel)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastRefuel.collect { state ->
                when (state) {
                    is UiState.Loading -> showLastRefuelLoading()
                    is UiState.Success -> showLastRefuel(state.data)
                    is UiState.Error   -> showLastRefuelError(state.message)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collect { state ->
                when (state) {
                    is UiState.Loading -> Unit
                    is UiState.Success -> showSummary(state.data)
                    is UiState.Error   -> Unit
                }
            }
        }
    }

    private fun showLastRefuelLoading() {
        // Placeholder уже выставлен в clearPlaceholders() — ничего не делаем
    }

    private fun showLastRefuel(refuel: Refuel?) {
        if (refuel == null) {
            // Нет заправок — показываем пустое состояние
            binding.tvLastDate.text    = "Заправок пока нет"
            binding.tvLastTotal.text   = ""
            binding.tvLastOdometer.text = ""
            binding.tvLastLiters.text  = ""
            binding.tvConsumption.text = "— л/100 км"
            binding.fuelGaugeView.setConsumption(0f)
            return
        }

        // Форматируем дату из ISO строки "2025-05-14T..." → "14.05.2025"
        val dateFormatted = formatDate(refuel.createdAt)

        binding.tvLastDate.text    = dateFormatted
        binding.tvLastTotal.text   = "${refuel.totalCost.toInt()} ₽"
        binding.tvLastOdometer.text = "${refuel.odometer} км"
        binding.tvLastLiters.text  = "${refuel.liters} л"

        if (refuel.consumption != null) {
            binding.fuelGaugeView.setConsumption(refuel.consumption.toFloat())
            binding.tvConsumption.text = "${refuel.consumption} л/100 км"
        } else {
            // Первая заправка — расход ещё не известен
            binding.fuelGaugeView.setConsumption(0f)
            binding.tvConsumption.text = "— л/100 км"
        }
    }

    private fun showLastRefuelError(message: String) {
        binding.tvLastDate.text = "Ошибка загрузки"
        binding.tvLastTotal.text = ""
        binding.tvLastOdometer.text = ""
        binding.tvLastLiters.text = ""
    }

    private fun showSummary(summary: Stats.Summary) {
        binding.tvMonthSpent.text = "${summary.totalSpent.toInt()} ₽"
        binding.tvMonthConsumption.text = summary.avgConsumption
            ?.toString() ?: "—"
    }

    // "2025-05-14T12:34:56" → "14.05.2025"
    private fun formatDate(isoDate: String): String {
        return try {
            val parts = isoDate.substring(0, 10).split("-")
            "${parts[2]}.${parts[1]}.${parts[0]}"
        } catch (e: Exception) {
            isoDate
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        breatheAnimator?.cancel()
        breatheAnimator = null
        _binding = null
    }
}