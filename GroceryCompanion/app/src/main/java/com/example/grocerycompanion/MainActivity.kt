package com.example.grocerycompanion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.grocerycompanion.databinding.ActivityMainBinding
import com.example.grocerycompanion.ui.item.ItemListFragment
import com.example.grocerycompanion.ui.list.ShoppingListFragment

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        supportFragmentManager.commit {
            replace(b.fragmentContainer.id, ItemListFragment()) // start on list
        }
        b.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_item -> {
                    supportFragmentManager.commit {
                        replace(b.fragmentContainer.id, ItemListFragment())
                    }
                    true
                }
                R.id.menu_list -> {
                    supportFragmentManager.commit {
                        replace(b.fragmentContainer.id, ShoppingListFragment())
                    }
                    true
                }
                else -> false
            }
        }

        b.bottomNav.selectedItemId = R.id.menu_item
    }
}