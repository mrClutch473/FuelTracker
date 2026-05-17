package com.example.fueltracker_android.ui.screens.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fueltracker_android.R
import com.example.fueltracker_android.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startSplashAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                findNavController().navigate(R.id.action_splash_to_home)
            }
        }, 2500)
    }

    private fun startSplashAnimations() {

        // ── 1. Lottie иконка: fade-in + небольшой scale-up (0→100ms задержки нет)
        binding.lottieSplashIcon.apply {
            scaleX = 0.6f
            scaleY = 0.6f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }

        // ── 2. Текстовая группа: появляется снизу через 400ms
        binding.textGroup.apply {
            translationY = 30f
            postDelayed({
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }, 400)
        }

        // ── 3. Прогресс-бар: scaleX 0 → 1 слева направо, задержка 300ms
        binding.progressFill.apply {
            pivotX = 0f
            postDelayed({
                ObjectAnimator.ofFloat(this, "scaleX", 0f, 1f).apply {
                    duration = 1500
                    interpolator = DecelerateInterpolator()
                    start()
                }
            }, 300)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}