package com.naol.languagecard

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class CardData(
    val id: Int,
    val wordFirstLang: String,
    val wordSecondLang: String,
    val sentenceFirstLang: String,
    val sentenceSecondLang: String
)

class CardViewModel(private val context: Context) : ViewModel()  {
    private val _cards = MutableStateFlow<List<CardData>>(emptyList())
    val cards: StateFlow<List<CardData>> = _cards

    init {
        _cards.value = loadCards()
    }

    fun removeCard(card: CardData) {
        _cards.value = _cards.value.filter { it.id != card.id }
    }

    private fun loadCards(): List<CardData> {
        val gson = Gson()
        val inputStream = context.resources.openRawResource(R.raw.sm1_new_kap1)
        return inputStream.use {
            val jsonString = it.readBytes().toString(Charsets.UTF_8)
            val type = object : TypeToken<List<CardData>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }

}