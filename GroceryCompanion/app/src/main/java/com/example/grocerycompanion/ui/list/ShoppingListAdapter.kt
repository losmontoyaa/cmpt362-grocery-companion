package com.example.grocerycompanion.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerycompanion.data.InMemoryDataSource
import com.example.grocerycompanion.databinding.RowListItemBinding
import com.example.grocerycompanion.model.ShoppingListItem

class ShoppingListAdapter(
    private val onRemove: (itemId: String) -> Unit,
    private val onQtyChange: (itemId: String, qty: Int) -> Unit
) : ListAdapter<ShoppingListItem, ShoppingListAdapter.VH>(DIFF) {

    // per-item recommendation map (ItemPickUi is defined top-level in this package)
    private var recs: Map<String, ItemPickUi> = emptyMap()

    fun setRecommendations(map: Map<String, ItemPickUi>) {
        recs = map
        notifyDataSetChanged()
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ShoppingListItem>() {
            override fun areItemsTheSame(o: ShoppingListItem, n: ShoppingListItem) =
                o.itemId == n.itemId
            override fun areContentsTheSame(o: ShoppingListItem, n: ShoppingListItem) = o == n
        }
    }

    inner class VH(val b: RowListItemBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(RowListItemBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)

        val meta = InMemoryDataSource.items[item.itemId]
        val displayName = when {
            meta == null -> item.itemId
            meta.brand.isNotBlank() -> "${meta.name} (${meta.brand})"
            else -> meta.name
        }
        h.b.tvItemName.text = displayName

        val pick = recs[item.itemId]
        h.b.tvBestStore.text = if (pick != null)
            "Best: ${pick.storeName} — $${"%.2f".format(pick.price)}"
        else
            ""

        h.b.tvQty.text = item.qty.toString()
        h.b.btnPlus.setOnClickListener { onQtyChange(item.itemId, item.qty + 1) }
        h.b.btnMinus.apply {
            isEnabled = item.qty > 1
            setOnClickListener { if (item.qty > 1) onQtyChange(item.itemId, item.qty - 1) }
        }
        h.b.btnRemove.setOnClickListener { onRemove(item.itemId) }
    }
}
