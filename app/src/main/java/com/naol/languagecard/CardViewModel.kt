package com.naol.languagecard

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


data class CardData(
    val id: Int,
    val wordFirstLang: String,
    val wordSecondLang: String,
    val sentenceFirstLang: String,
    val sentenceSecondLang: String
)


class CardViewModel(private val context: Context) : ViewModel() {
    private val _cards = mutableStateListOf<CardData>()
    val cards: List<CardData> = _cards

    init {
        _cards.addAll(loadCards().toMutableStateList())
    }

    fun removeCard(card: CardData) {
        _cards.remove(card)
    }

    private fun loadCards(): List<CardData> {
        val gson = Gson()
        val inputStream =
            context.resources.openRawResource(R.raw.sm1_new_kap1) // Assuming you have this file in res/raw
        return inputStream.use {
            val jsonString = it.readBytes().toString(Charsets.UTF_8)
            val type = object : TypeToken<List<CardData>>() {}.type
            gson.fromJson<List<CardData>>(jsonString, type)
        }
    }

}