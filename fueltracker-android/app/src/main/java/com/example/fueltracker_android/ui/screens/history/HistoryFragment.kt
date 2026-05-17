package com.example.fueltracker_android.ui.screens.history

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fueltracker_android.R
import com.example.fueltracker_android.data.ApiService
import com.example.fueltracker_android.data.FuelRepository
import com.example.fueltracker_android.databinding.FragmentHistoryBinding
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.factory(FuelRepository(ApiService.create()))
    }

    private lateinit var adapter: RefuelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToDelete()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = RefuelAdapter { id -> viewModel.deleteRefuel(id) }
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val deleteBackground = Paint().apply {
            color = Color.parseColor("#CC3333")
        }
        val cornerRadius = 16f * resources.displayMetrics.density

        // Иконка корзины — убедись что ic_delete есть в drawable,
        // если нет — замени на другой ic_ из проекта
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)

        val callback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_ID.toInt()) return

                val refuel = adapter.currentList[position]

                // ── Collapse анимация → потом удаление
                adapter.collapseAndDelete(viewHolder) {
                    viewModel.deleteRefuel(refuel.id)
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val swipeProgress = (-dX / itemView.width).coerceIn(0f, 1f)

                if (dX < 0) {
                    // ── Красный фон с закруглёнными углами
                    val bgRect = RectF(
                        itemView.right + dX,
                        itemView.top.toFloat() + 4f,
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat() - 4f
                    )
                    canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, deleteBackground)

                    // ── Иконка корзины появляется при свайпе
                    deleteIcon?.let { icon ->
                        val iconSize = (24 * resources.displayMetrics.density).toInt()
                        val iconMargin = (20 * resources.displayMetrics.density).toInt()
                        val iconTop = itemView.top + (itemView.height - iconSize) / 2
                        val iconLeft = itemView.right - iconMargin - iconSize
                        val iconRight = itemView.right - iconMargin
                        val iconBottom = iconTop + iconSize

                        icon.setTint(Color.WHITE)
                        icon.alpha = (swipeProgress * 255).toInt()
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(canvas)
                    }
                }

                super.onChildDraw(
                    canvas, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive
                )
            }

            // Порог свайпа — 35% ширины элемента
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.35f
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.rvHistory)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.refuels.collect { state ->
                when (state) {
                    is UiState.Loading -> { /* loading */ }
                    is UiState.Success -> {
                        adapter.submitList(state.data)
                        binding.emptyState.visibility =
                            if (state.data.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is UiState.Error -> {
                        binding.emptyState.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}