package com.naol.languagecard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.naol.languagecard.ui.theme.LanguageCardTheme
import com.naol.languagecard.ui.theme.Pink40
import com.naol.languagecard.ui.theme.Purple40
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanguageCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioPlayer.release()
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier, viewModel: CardViewModel = koinViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    Box(modifier = modifier) {
        CardStack(
            cards = cards,
            onSwiped = { card -> viewModel.removeCard(card) },
        )
    }
}

@Composable
fun CardStack(
    cards: List<CardData>, onSwiped: (CardData) -> Unit, modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        cards.reversed().forEach { card ->
            SwipeableCard(
                cardData = card, onSwiped = onSwiped, modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun SwipeableCard(
    cardData: CardData,
    onSwiped: (CardData) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = -200f
    var isSwiped by remember { mutableStateOf(false) }
    var flipped by remember { mutableStateOf(false) }
    var isAudioLoading by remember { mutableStateOf(false) } // Loading state
    val context = LocalContext.current

    val wordAudio = if (!flipped) "${cardData.id}_w_ru.mp3" else "${cardData.id}_w_fi.mp3"
    val sentenceAudio = if (!flipped) "${cardData.id}_s_ru.mp3" else "${cardData.id}_s_fi.mp3"

    // Handle audio playback
    LaunchedEffect(flipped) {
        AudioPlayer.playAudioSequence(context, wordAudio, sentenceAudio) { isLoading ->
            isAudioLoading = isLoading
        }
    }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        finishedListener = {
            if (isSwiped) {
                onSwiped(cardData)
                AudioPlayer.playAudioSequence(
                    context,
                    "${cardData.id + 1}_w_ru.mp3",
                    "${cardData.id + 1}_s_ru.mp3"
                ) { isLoading ->
                    isAudioLoading = isLoading
                }
            }
        },
        label = "offsetXAnimation"
    )

    val flipRotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        label = "flipRotationAnimation"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX < swipeThreshold) {
                            isSwiped = true
                        } else {
                            offsetX = 0f
                        }
                    },
                    onDrag = { _, dragAmount ->
                        if (dragAmount.x < 0) {
                            offsetX += dragAmount.x
                        }
                    }
                )
            }
            .height(300.dp)
            .padding(16.dp)
            .graphicsLayer {
                rotationY = flipRotation
                cameraDistance = 8 * density
            }
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(16.dp))
    ) {
        if (!isAudioLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { flipped = !flipped }
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(if (flipRotation <= 90f) Pink40 else Purple40)
                        .padding(24.dp)
                        .fillMaxSize()
                ) {
                    val textScaleX = if (flipRotation > 90f) -1f else 1f
                    Text(
                        text = if (flipRotation <= 90f) cardData.wordFirstLang else cardData.wordSecondLang,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer { scaleX = textScaleX }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (flipRotation <= 90f) cardData.sentenceFirstLang else cardData.sentenceSecondLang,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer { scaleX = textScaleX }
                    )
                }
            }
        }
    }
}
