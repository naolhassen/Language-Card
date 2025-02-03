package com.naol.languagecard.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.naol.languagecard.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CardData(
    val id: Int,
    val wordFirstLang: String,
    val wordSecondLang: String,
    val sentenceFirstLang: String,
    val sentenceSecondLang: String
)

class CardViewModel(private val context: Context) : ViewModel() {
    private val _cards = MutableStateFlow<List<CardData>>(emptyList())
    val cards: StateFlow<List<CardData>> = _cards

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> get() = _currentIndex

    init {
        _cards.value = loadCards()
    }

    fun swipeCard() {
        if (_currentIndex.value < _cards.value.size - 1) {
            _currentIndex.value += 1
        }
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