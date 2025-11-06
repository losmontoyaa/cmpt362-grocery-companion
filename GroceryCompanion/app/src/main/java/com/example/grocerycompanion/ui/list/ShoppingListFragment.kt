package com.example.grocerycompanion.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grocerycompanion.databinding.FragmentShoppingListBinding
import com.example.grocerycompanion.repo.PriceRepo
import com.example.grocerycompanion.repo.ShoppingListRepo
import com.example.grocerycompanion.repo.StoreRepo
import com.example.grocerycompanion.util.ViewModelFactory
import kotlinx.coroutines.launch

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private val adapter = ShoppingListAdapter(
        onRemove = { itemId ->
            viewLifecycleOwner.lifecycleScope.launch { vm.remove(itemId) }
        },
        onQtyChange = { itemId, qty ->
            viewLifecycleOwner.lifecycleScope.launch { vm.setQty(itemId, qty) }
        }
    )

    private val vm: ShoppingListViewModel by viewModels {
        ViewModelFactory {
            ShoppingListViewModel(
                listRepo = ShoppingListRepo(),
                priceRepo = PriceRepo(),
                storeRepo = StoreRepo()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // RecyclerView setup
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        // Stream shopping list
        viewLifecycleOwner.lifecycleScope.launch {
            vm.list.collect { items ->
                adapter.submitList(items)
            }
        }

        // Recommend button: show per-store totals + per-item cheapest picks
        binding.btnRecommend.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                // 1) Totals by store
                val totals = vm.computePerStoreTotals()
                binding.tvTotals.text =
                    if (totals.isEmpty()) "No prices yet" else totals.joinToString("\n") {
                        val totalStr = "$" + "%.2f".format(it.total)
                        val distStr = it.distanceKm?.let { d -> " (${String.format("%.1f", d)} km)" } ?: ""
                        "${it.storeName}: $totalStr$distStr"
                    }

                // 2) Per-item cheapest (UI-ready) → adapter
                val perItemUi = vm.computePerItemCheapestUi()
                adapter.setRecommendations(perItemUi)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
