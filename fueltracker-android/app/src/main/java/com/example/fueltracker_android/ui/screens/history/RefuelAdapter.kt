package com.example.fueltracker_android.ui.screens.history

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fueltracker_android.databinding.ItemRefuelBinding
import com.example.fueltracker_android.domain.model.Refuel

class RefuelAdapter(
    private val onDelete: (Int) -> Unit
) : ListAdapter<Refuel, RefuelAdapter.RefuelViewHolder>(DiffCallback()) {

    // Позиции которые уже анимировались — не анимируем повторно при скролле назад
    private val animatedPositions = HashSet<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefuelViewHolder {
        val binding = ItemRefuelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RefuelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RefuelViewHolder, position: Int) {
        holder.bind(getItem(position))

        // ── Stagger анимация: только при первом появлении элемента
        if (!animatedPositions.contains(position)) {
            animatedPositions.add(position)
            holder.itemView.apply {
                alpha = 0f
                translationY = 40f * resources.displayMetrics.density // 40dp → px
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay(position * 60L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    // ── Collapse анимация перед удалением (вызывается из ItemTouchHelper)
    fun collapseAndDelete(
        viewHolder: RecyclerView.ViewHolder,
        onAnimationEnd: () -> Unit
    ) {
        val itemView = viewHolder.itemView
        val initialHeight = itemView.height

        ValueAnimator.ofInt(initialHeight, 0).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                itemView.layoutParams.height = anim.animatedValue as Int
                itemView.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd()
                }
            })
            start()
        }
    }

    // Сброс при новом списке — чтобы анимация сработала для новых данных
    override fun submitList(list: List<Refuel>?) {
        animatedPositions.clear()
        super.submitList(list)
    }

    inner class RefuelViewHolder(private val binding: ItemRefuelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(refuel: Refuel) {
            binding.tvDate.text       = refuel.createdAt
            binding.tvLiters.text     = "${refuel.liters} л"
            binding.tvTotal.text      = "${refuel.totalCost.toInt()} ₽"
            binding.tvOdometer.text   = "${refuel.odometer} км"
            binding.tvConsumption.text = refuel.consumption
                ?.let { "$it л/100" } ?: "—"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Refuel>() {
        override fun areItemsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem == newItem
    }
}