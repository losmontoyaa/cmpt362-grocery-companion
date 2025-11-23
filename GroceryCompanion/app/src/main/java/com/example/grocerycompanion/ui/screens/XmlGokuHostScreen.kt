package com.example.grocerycompanion.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.example.grocerycompanion.R
import com.example.grocerycompanion.databinding.ActivityMainBinding
import com.example.grocerycompanion.ui.item.ItemListFragment
import com.example.grocerycompanion.ui.list.ShoppingListFragment

/**
 * Hosts Goku's *entire* XML flow (bottom nav + all fragment pages).
 * This mirrors his original MainActivity behavior inside Compose.
 */
@Composable
fun XmlGokuHostScreen(onExit: () -> Unit = {}) {

    // This handles system back to return to StartUpScreen
    BackHandler(enabled = true) { onExit() }
    AndroidViewBinding(ActivityMainBinding::inflate) {
        val activity = root.context as FragmentActivity

        // set initial fragment once
        if (activity.supportFragmentManager.findFragmentById(fragmentContainer.id) == null) {
            activity.supportFragmentManager.commit {
                replace(fragmentContainer.id, ItemListFragment(), "itemList")
            }
            bottomNav.selectedItemId = R.id.menu_item
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item -> {
                    activity.supportFragmentManager.commit {
                        replace(fragmentContainer.id, ItemListFragment(), "itemList")
                        addToBackStack(null)
                    }
                    true
                }
                R.id.menu_list -> {
                    activity.supportFragmentManager.commit {
                        replace(fragmentContainer.id, ShoppingListFragment(), "shoppingList")
                        addToBackStack(null)
                    }
                    true
                }
                else -> false
            }
        }

        // Optional: handle toolbar/back button here
    }
}
