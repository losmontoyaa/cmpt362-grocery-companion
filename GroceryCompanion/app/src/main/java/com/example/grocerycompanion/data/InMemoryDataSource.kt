package com.example.grocerycompanion.data

import com.example.grocerycompanion.model.*


 /* THIS WAS USED FOR INITIAL TESTING, SAFE TO IGNORE */
object InMemoryDataSource {
    val items = listOf(
        Item(
            id = "demo-item-1", name = "Rotisserie Chicken", brand = "Kirkland",
            barcode = "1234567890123", imgUrl = "https://picsum.photos/600/400?1",
            category = "Prepared Foods", avgRating = 4.4, ratingsCount = 120
        ),
        Item(
            id = "demo-item-2", name = "Bananas (1kg)", brand = "Dole",
            barcode = "8901234567890", imgUrl = "https://picsum.photos/600/400?2",
            category = "Produce", avgRating = 4.2, ratingsCount = 87
        ),
        Item(
            id = "demo-item-3", name = "2% Milk (4L)", brand = "Dairyland",
            barcode = "6501230987654", imgUrl = "https://picsum.photos/600/400?3",
            category = "Dairy", avgRating = 4.6, ratingsCount = 231
        )
    ).associateBy { it.id }

    val stores = listOf(
        Store("store-costco","Costco Metrotown","Burnaby, BC",49.2276,-122.9991),
        Store("store-walmart","Walmart Lougheed","Burnaby, BC",49.2488,-122.8970),
        Store("store-superstore","Real Canadian Superstore","Burnaby, BC",49.2481,-122.9800)
    ).associateBy { it.id }

    val prices = listOf(
        // Chicken
        Price("demo-item-1","store-costco",7.99,"each",1700000000000L,true,"manual"),
        Price("demo-item-1","store-walmart",8.97,"each",1700500000000L,false,"manual"),
        // Bananas
        Price("demo-item-2","store-superstore",1.49,"/kg",1700600000000L,false,"manual"),
        Price("demo-item-2","store-walmart",1.59,"/kg",1700605000000L,false,"manual"),
        // Milk
        Price("demo-item-3","store-superstore",5.49,"4L",1700610000000L,true,"manual"),
        Price("demo-item-3","store-costco",5.29,"4L",1700620000000L,false,"manual")
    )

    val shoppingList = mutableListOf<ShoppingListItem>()
}
