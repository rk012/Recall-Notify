package io.github.rk012.recaller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import io.github.rk012.recaller.ui.theme.RecallerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecallerTheme {
                var screenState by remember { mutableStateOf(ScreenState.MAIN) }

                App(
                    screenState = screenState,
                    setScreenState = { screenState = it }
                )
            }
        }
    }
}

enum class ScreenState {
    MAIN,
    SCAN,
    INPUT
}

@Composable
fun App(
    screenState: ScreenState,
    setScreenState: (ScreenState) -> Unit
) {
    when (screenState) {
        ScreenState.MAIN -> MainScreen(
            onCameraStart = {
                setScreenState(ScreenState.SCAN)
            }
        )

        ScreenState.SCAN -> {
            ScannerScreen()
        }
        ScreenState.INPUT -> {}
    }
}