package com.example.grocerycompanion.repo

import com.example.grocerycompanion.model.ShoppingListItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Shopping list lives under:
 *
 *  shoppingLists/{userId}/items/{itemId} {
 *      qty: Long
 *  }
 */
class FirebaseShoppingListRepo(
    private val userId: String,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val itemsRef = db.collection("shoppingLists")
        .document(userId)
        .collection("items")

    fun streamList(): Flow<List<ShoppingListItem>> = callbackFlow {
        val listener = itemsRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            val list = snapshot?.documents?.mapNotNull { doc ->
                val qty = doc.getLong("qty")?.toInt() ?: return@mapNotNull null
                ShoppingListItem(
                    itemId = doc.id,
                    qty = qty
                )
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { listener.remove() }
    }

    suspend fun add(itemId: String, qty: Int) {
        val doc = itemsRef.document(itemId)
        db.runTransaction { tx ->
            val snap = tx.get(doc)
            val currentQty = snap.getLong("qty")?.toInt() ?: 0
            val newQty = currentQty + qty
            tx.set(doc, mapOf("qty" to newQty))
        }.await()
    }

    suspend fun remove(itemId: String) {
        itemsRef.document(itemId).delete().await()
    }

    suspend fun setQty(itemId: String, qty: Int) {
        if (qty <= 0) {
            remove(itemId)
        } else {
            itemsRef.document(itemId).set(mapOf("qty" to qty)).await()
        }
    }
}
