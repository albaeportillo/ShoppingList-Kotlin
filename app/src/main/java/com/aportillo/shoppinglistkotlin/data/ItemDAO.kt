package com.aportillo.shoppinglistkotlin.data

import androidx.room.*

@Dao
interface ItemDAO {
    @Query("SELECT * FROM item  WHERE userId =(:userId)")
    fun getAllItems(userId: String): List<Item>

    @Query("DELETE FROM item")
    fun deleteAll()

    @Insert
    fun insertItem(item: Item): Long

    @Delete
    fun deleteItem(item: Item)

    @Update
    fun updateItem(item: Item)
}