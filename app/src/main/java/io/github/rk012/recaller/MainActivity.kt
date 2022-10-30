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
                var screenData by remember { mutableStateOf(ScreenData(ScreenState.MAIN, null)) }

                App(
                    screenData = screenData,
                    setScreenData = { screenData = it }
                )
            }
        }
    }
}

data class ScreenData(
    val screenState: ScreenState,
    val lastCameraScan: String?
)

enum class ScreenState {
    MAIN,
    SCAN,
    INPUT
}

@Composable
fun App(
    screenData: ScreenData,
    setScreenData: (ScreenData) -> Unit
) {
    when (screenData.screenState) {
        ScreenState.MAIN -> MainScreen(
            onCameraStart = {
                setScreenData(
                    ScreenData(screenState = ScreenState.SCAN, lastCameraScan = null)
                )
            },
            cameraData = screenData.lastCameraScan
        )

        ScreenState.SCAN -> ScannerScreen(
            onCodeScanned = {
                setScreenData(
                    ScreenData(screenState = ScreenState.MAIN, lastCameraScan = it)
                )
            }
        )

        ScreenState.INPUT -> {}
    }
}