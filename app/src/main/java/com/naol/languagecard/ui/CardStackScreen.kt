package com.naol.languagecard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.naol.languagecard.ui.theme.DarkSlateBlue
import com.naol.languagecard.ui.theme.DeepTeal
import com.naol.languagecard.ui.theme.robotoFontFamily
import com.naol.languagecard.utils.AudioPlayer
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min
import kotlin.math.roundToInt


@Composable
fun HomeScreen(
    modifier: Modifier, viewModel: CardViewModel = koinViewModel()
) {
    val cards by viewModel.cards.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    var isLoadingFirstTime by remember { mutableStateOf(true) }
    val context = LocalContext.current
    if (isLoadingFirstTime && cards.isNotEmpty()) {
        val firstCard = cards[0]
        LaunchedEffect(Unit) {
            AudioPlayer.playAudioSequence(
                context,
                "${firstCard.id}_w_ru.mp3",
                "${firstCard.id}_s_ru.mp3"
            )
            isLoadingFirstTime = false
        }
    }

    Box(modifier = modifier) {
        CardStack(
            cards = cards,
            currentIndex = currentIndex,
            onSwiped = { viewModel.swipeCard() }
        )
    }
}

@Composable
fun CardStack(
    cards: List<CardData>,
    currentIndex: Int,
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVisibleCards = 3
    val remainingCards = cards.size - currentIndex
    Box(modifier = modifier.fillMaxSize()) {
        if (remainingCards <= 0) {
            Text(
                text = "No more cards!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {

            val visibleCards = cards.subList(
                currentIndex,
                min(currentIndex + maxVisibleCards, cards.size)
            )
            visibleCards.forEachIndexed { index, card ->
                key(card.id) {
                    val cardOffset = (index * 16).dp
                    val cardScale = 1f - (index * 0.08f)

                    SwipeableCard(
                        cardData = card,
                        onSwiped = onSwiped,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = cardOffset)
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                            }
                            .zIndex((maxVisibleCards - index).toFloat())
                    )
                }
            }
        }
    }
}


@Composable
fun SwipeableCard(
    cardData: CardData,
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = -200f
    var isSwiped by remember { mutableStateOf(false) }
    var flipped by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val targetOffset = if (isSwiped) -2000f else 0f
    val animatedOffsetX by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 300),
        finishedListener = {
            if (isSwiped) {
                onSwiped()
                offsetX = 0f
                isSwiped = false
                AudioPlayer.playAudioSequence(
                    context,
                    "${cardData.id + 1}_w_ru.mp3",
                    "${cardData.id + 1}_s_ru.mp3"
                )
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
                        isSwiped = offsetX < swipeThreshold
                        offsetX = if (isSwiped) targetOffset else 0f
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
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
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    flipped = !flipped
                    val wordAudio =
                        if (!flipped) "${cardData.id}_w_ru.mp3" else "${cardData.id}_w_fi.mp3"
                    val sentenceAudio =
                        if (!flipped) "${cardData.id}_s_ru.mp3" else "${cardData.id}_s_fi.mp3"
                    AudioPlayer.playAudioSequence(context, wordAudio, sentenceAudio)
                }
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(if (flipRotation <= 90f) DeepTeal else DarkSlateBlue)
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                val textScaleX = if (flipRotation > 90f) -1f else 1f
                Text(
                    text = if (flipRotation <= 90f) cardData.wordFirstLang else cardData.wordSecondLang,
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = robotoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { scaleX = textScaleX }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (flipRotation <= 90f) cardData.sentenceFirstLang else cardData.sentenceSecondLang,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = robotoFontFamily,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { scaleX = textScaleX }
                )
            }
        }
    }
}
