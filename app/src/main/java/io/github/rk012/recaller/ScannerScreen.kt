package io.github.rk012.recaller

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import java.util.concurrent.Executors

@Composable
fun ScannerScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onUseCase = {
                previewUseCase = it
            }
        )

        LaunchedEffect(previewUseCase) {
            val cameraProvider = context.getCameraProvider()
            val cameraExecutor = Executors.newSingleThreadExecutor()

            val qrAnalyzer = QrCodeAnalyzer {
                // TODO
                Log.i("ScannerScreen", "Qr Code found: ${it.rawValue}")
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, qrAnalyzer)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase, imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraCapture", "Failed to bind camera use cases", e)
            }
        }
    }

    // TODO
}