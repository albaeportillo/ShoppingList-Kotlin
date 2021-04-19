package com.aportillo.shoppinglistkotlin.touch

interface ItemTouchHelperCallback {
    fun onDismissed(position: Int)
    fun onItemMoved(fromPosition: Int, toPosition: Int)
}