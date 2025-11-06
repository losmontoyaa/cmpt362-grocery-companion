package com.example.grocerycompanion.ui.item

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerycompanion.data.InMemoryDataSource
import com.example.grocerycompanion.databinding.FragmentItemListBinding
import com.example.grocerycompanion.databinding.RowItemSmallBinding

class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null
    private val b get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?) =
        FragmentItemListBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val items = InMemoryDataSource.items.values.toList()
        b.rvItems.layoutManager = LinearLayoutManager(requireContext())
        b.rvItems.adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(p: ViewGroup, v: Int) =
                VH(RowItemSmallBinding.inflate(layoutInflater, p, false))
            override fun onBindViewHolder(h: VH, pos: Int) {
                val it = items[pos]
                h.b.tvName.text = it.name
                h.b.tvBrand.text = it.brand
                h.itemView.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(
                            requireActivity().findViewById<View>(com.example.grocerycompanion.R.id.fragmentContainer).id,
                            ItemDetailFragment().apply {
                                arguments = Bundle().apply { putString("itemId", items[pos].id) }
                            }
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }
            override fun getItemCount() = items.size
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
    class VH(val b: RowItemSmallBinding) : RecyclerView.ViewHolder(b.root)
}