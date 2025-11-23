package com.example.grocerycompanion.ui.item

import android.os.Bundle
import android.view.*

import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.grocerycompanion.databinding.FragmentItemDetailBinding
import com.example.grocerycompanion.repo.*
import com.example.grocerycompanion.util.ViewModelFactory


class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    private val storePriceAdapter = StorePriceAdapter(onQuickAdd = {
        Toast.makeText(requireContext(), "Added to list", Toast.LENGTH_SHORT).show()
    })

    private val vm: ItemViewModel by viewModels {
        val itemId = requireArguments().getString("itemId") ?: "demo-item-1"
        ViewModelFactory {
            ItemViewModel(
                itemId = itemId,
                itemRepo = ItemRepo(),
                priceRepo = PriceRepo(),
                storeRepo = StoreRepo(),
                listRepo = ShoppingListRepo()
            )
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)

        // RecyclerView needs a LayoutManager
        binding.rvPrices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPrices.adapter = storePriceAdapter

        // NumberPicker needs bounds to be usable
        binding.qtyStepper.minValue = 1
        binding.qtyStepper.maxValue = 99
        binding.qtyStepper.wrapSelectorWheel = true
        binding.qtyStepper.value = 1

        // Add-to-list button
        binding.btnAddToList.setOnClickListener {
            val qty = binding.qtyStepper.value
            vm.addToList(qty)
            Toast.makeText(requireContext(), "Added $qty to list", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}