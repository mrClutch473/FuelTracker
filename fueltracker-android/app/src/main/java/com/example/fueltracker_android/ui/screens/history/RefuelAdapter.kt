package com.example.fueltracker_android.ui.screens.history

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fueltracker_android.R
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
            // ── Дата: "2026-05-22T18:53:42" → "22 мая 2026"
            binding.tvDate.text = formatDate(refuel.createdAt)

            binding.tvLiters.text   = "${refuel.liters} л"
            binding.tvTotal.text    = "${refuel.totalCost.toInt()} ₽"
            binding.tvOdometer.text = "${refuel.odometer} км"
            binding.tvConsumption.text = refuel.consumption
                ?.let { "${it} л/100" } ?: "—"

            // ── Заметка: показываем строку только если есть текст
            if (!refuel.note.isNullOrBlank()) {
                binding.noteRow.visibility = View.VISIBLE
                binding.tvNote.text = refuel.note
            } else {
                binding.noteRow.visibility = View.GONE
            }

            // ── Цветная полоска-индикатор по расходу топлива
            val indicatorRes = when {
                refuel.consumption == null  -> R.drawable.bg_indicator_amber // первая заправка
                refuel.consumption <= 8.0   -> R.drawable.bg_indicator_green  // хороший расход
                refuel.consumption <= 12.0  -> R.drawable.bg_indicator_amber  // средний
                else                        -> R.drawable.bg_indicator_red    // высокий
            }
            binding.viewIndicator.setBackgroundResource(indicatorRes)
        }

        /**
         * Форматирует ISO-строку даты в читаемый вид.
         * "2026-05-22T18:53:42" → "22 мая 2026"
         */
        private fun formatDate(isoDate: String): String {
            return try {
                val datePart = isoDate.substring(0, 10)   // "2026-05-22"
                val parts    = datePart.split("-")
                val day      = parts[2].toInt()
                val monthIdx = parts[1].toInt() - 1
                val year     = parts[0]

                val months = arrayOf(
                    "января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"
                )
                "$day ${months[monthIdx]} $year"
            } catch (e: Exception) {
                isoDate
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Refuel>() {
        override fun areItemsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem == newItem
    }
}
