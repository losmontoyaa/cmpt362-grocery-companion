package com.example.grocerycompanion.ui.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerycompanion.databinding.RowStorePriceBinding
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.Store

class StorePriceAdapter(
    private val onQuickAdd: (storeId: String) -> Unit
) : ListAdapter<Pair<Price, Store>, StorePriceAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Pair<Price, Store>>() {
            override fun areItemsTheSame(
                o: Pair<Price, Store>,
                n: Pair<Price, Store>
            ) = o.first.itemId == n.first.itemId && o.first.storeId == n.first.storeId

            override fun areContentsTheSame(
                o: Pair<Price, Store>,
                n: Pair<Price, Store>
            ) = o == n
        }
    }

    inner class VH(val b: RowStorePriceBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(RowStorePriceBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val (price, store) = getItem(pos)
        h.b.tvStoreName.text = store.name
        h.b.tvPrice.text = "$${"%.2f".format(price.price)} ${price.unit}"
        h.b.tvBadge.apply {
            text = if (price.isDeal) "DEAL" else ""
            alpha = if (price.isDeal) 1f else 0f
        }
        h.b.btnQuickAdd.setOnClickListener { onQuickAdd(store.id) }
    }
}
