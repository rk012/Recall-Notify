package io.github.rk012.recaller

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen() {
    // Example screen to make sure permissions are being applied.
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)

    Text(cameraPermission.status.toString())
}