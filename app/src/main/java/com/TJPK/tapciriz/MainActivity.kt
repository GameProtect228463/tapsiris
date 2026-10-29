package com.TJPK.tapciriz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.TJPK.tapciriz.ui.ScheduleScreen
import com.TJPK.tapciriz.ui.theme.TapcirizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapcirizTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScheduleScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScheduleScreenPreview() {
    TapcirizTheme {
        ScheduleScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScheduleScreenDarkPreview() {
    TapcirizTheme(darkTheme = true) {
        ScheduleScreen()
    }
}
