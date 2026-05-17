package com.example.fueltracker_android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.example.fueltracker_android.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    fun setValue(value: String) {
        binding.tvValue.text = value
    }

    fun setLabel(label: String) {
        binding.tvLabel.text = label
    }
}
