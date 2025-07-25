package com.module.notelycompose.audio.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.audio.ui.formatTimeToHHMMSS
import com.module.notelycompose.audio.ui.player.model.AudioPlayerUiState
import com.module.notelycompose.audio.ui.uicomponents.Thumb
import com.module.notelycompose.audio.ui.uicomponents.Track
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.style.LayoutGuide
import com.module.notelycompose.resources.vectors.IcPause
import com.module.notelycompose.resources.vectors.Images
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.player_ui_initial_time
import com.module.notelycompose.resources.player_ui_pause
import com.module.notelycompose.resources.player_ui_play
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlatformAudioPlayerUi(
    filePath: String,
    uiState: AudioPlayerUiState,
    onLoadAudio: (filePath: String) -> Unit,
    onClear: () -> Unit,
    onSeekTo: (position: Int) -> Unit,
    onTogglePlayPause: () -> Unit
) {
    // Load audio when the composable is first created
    LaunchedEffect(filePath) {
        onLoadAudio(filePath)
    }

    Box(
        modifier = Modifier
            .width(800.dp)
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LocalCustomColors.current.playerBoxBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                IconButton(
                    onClick = { onTogglePlayPause() },
                    modifier = Modifier.size(28.dp),
                    enabled = uiState.isLoaded
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Images.Icons.IcPause else Icons.Filled.PlayArrow,
                        contentDescription = if (uiState.isPlaying) {
                            stringResource(Res.string.player_ui_pause)
                        } else {
                            stringResource(Res.string.player_ui_play)
                        },
                        modifier = Modifier.size(28.dp),
                        tint = if (uiState.isLoaded) Color.DarkGray else Color.LightGray
                    )
                }
            }

            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = uiState.currentPosition.formatTimeToHHMMSS(),
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AudioSlider(
                    modifier = Modifier.fillMaxWidth(),
                    filePath = filePath,
                    uiState = uiState,
                    onLoadAudio = { filePath -> onLoadAudio(filePath) },
                    onClear = { onClear() },
                    onSeekTo = { position -> onSeekTo(position) },
                    onTogglePlayPause = { onTogglePlayPause() }
                )
            }

            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = if (uiState.duration > 0) {
                        uiState.duration.formatTimeToHHMMSS()
                    } else {
                        stringResource(Res.string.player_ui_initial_time)
                    },
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSlider(
    modifier: Modifier = Modifier,
    filePath: String,
    uiState: AudioPlayerUiState,
    onLoadAudio: (filePath: String) -> Unit,
    onClear: () -> Unit,
    onSeekTo: (position: Int) -> Unit,
    onTogglePlayPause: () -> Unit
) {
    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = sliderPosition?.toFloat() ?: uiState.currentPosition.toFloat(),
        onValueChange = {
            sliderPosition = it
        },
        modifier = modifier,
        thumb = {
            Thumb(
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(
                    thumbColor = LocalCustomColors.current.activeThumbTrackColor,
                ),
                thumbSize = DpSize(width = 16.dp, height = 16.dp)
            )
        },
        track = { state ->
            Track(
                sliderState = state,
                colors = SliderDefaults.colors(
                    activeTrackColor = LocalCustomColors.current.activeThumbTrackColor,
                    inactiveTrackColor = Color.LightGray
                )
            )
        },
        onValueChangeFinished = {
            sliderPosition?.let {
                onSeekTo(it.toInt())
                sliderPosition = null
            }
        },
        valueRange = 0f..uiState.duration.toFloat().coerceAtLeast(1f),
        enabled = uiState.isLoaded,
        interactionSource = interactionSource
    )
}

