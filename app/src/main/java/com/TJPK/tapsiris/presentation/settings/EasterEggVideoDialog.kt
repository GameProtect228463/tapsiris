package com.TJPK.tapsiris.presentation.settings

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.TJPK.tapsiris.R

@Composable
fun EasterEggVideoDialog(
    onDismiss: () -> Unit,
    rawResId: Int = R.raw.musor_drop_green_screen
) {
    AlertDialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.Black
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                            // 1. Сохраняем текущие настройки звука
                            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val wasSpeakerOn = audioManager.isSpeakerphoneOn

                            // 2. Выкручиваем громкость на максимум и переключаем на динамик
                            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                            audioManager.isSpeakerphoneOn = true

                            // Функция для восстановления исходных параметров
                            fun restoreAudioSettings() {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                                audioManager.isSpeakerphoneOn = wasSpeakerOn
                            }

                            VideoView(context).apply {
                                val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
                                setVideoURI(uri)

                                setOnPreparedListener { mediaPlayer ->
                                    mediaPlayer.isLooping = false
                                    start()
                                }

                                setOnCompletionListener {
                                    restoreAudioSettings()
                                    onDismiss()
                                }
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(24.dp)
    )
}