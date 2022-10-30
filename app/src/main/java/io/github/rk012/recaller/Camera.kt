package io.github.rk012.recaller

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// https://medium.com/@dpisoni/building-a-simple-photo-app-with-jetpack-compose-camerax-and-coroutines-part-2-camera-preview-cf1d795129f6

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, executor)
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onUseCase: (UseCase) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }.let(onUseCase)

            previewView
        }
    )
}

class QrCodeAnalyzer(
    private val onCodeFound: (Barcode) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastTimeStamp = 0L

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val currentTimeStamp = System.currentTimeMillis()

        if (currentTimeStamp - lastTimeStamp >= 1) {
            image.image?.let { inputImage ->
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()

                val scanner = BarcodeScanning.getClient(options)
                val processImage = InputImage.fromMediaImage(inputImage, image.imageInfo.rotationDegrees)

                scanner.process(processImage)
                    .addOnSuccessListener { qrCodes ->
                        if (qrCodes.isNotEmpty()) {
                            onCodeFound(qrCodes[0])
                        } else {
                            Log.d("QrCodeAnalyzer", "No QR Code scanned")
                        }
                    }
                    .addOnFailureListener {
                        Log.w("QrCodeAnalyzer", "QrCodeAnalyzer: Something went wrong $it")
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }

            lastTimeStamp = currentTimeStamp
        } else {
            image.close()
        }
    }
}