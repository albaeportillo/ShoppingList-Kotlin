package com.aportillo.shoppinglistkotlin.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true) var itemId: Long?,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "price") var price: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "intCategory") var intCategory: Int,
    @ColumnInfo(name = "status") var status: Boolean,
    @ColumnInfo(name = "quantity") var quantity: Int,
    @ColumnInfo(name = "userId") var userId: String,
    @ColumnInfo(name="random") var random: String
) : Serializable