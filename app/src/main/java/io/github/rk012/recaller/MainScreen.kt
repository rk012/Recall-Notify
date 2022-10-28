package io.github.rk012.recaller

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCameraStart: () -> Unit
) {
    var isConsumerState by remember { mutableStateOf(true) }
    var startCamera by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = isConsumerState,
                    onClick = { isConsumerState = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingBag,
                            contentDescription = "My Items"
                        )
                    },
                    label = {
                        Text("My Items")
                    }
                )

                NavigationBarItem(
                    selected = !isConsumerState,
                    onClick = { isConsumerState = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Sell,
                            contentDescription = "My Products"
                        )
                    },
                    label = {
                        Text("My Products")
                    }
                )
            }
        },
        floatingActionButton = {
            if (isConsumerState) {
                FloatingActionButton(onClick = { startCamera = true }) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Scan item"
                    )
                }
            } else {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Rounded.Create,
                        contentDescription = "Create item"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        MainScreenContent(
            startCamera,
            onCameraStart,
            onCameraFail = { startCamera = false }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MainScreenContent(
    startCamera: Boolean,
    onCameraStart: () -> Unit,
    onCameraFail: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // TODO


    when (cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            if (startCamera) onCameraStart()
        }
        is PermissionStatus.Denied -> {
            if (startCamera) {
                AlertDialog(
                    onDismissRequest = onCameraFail,
                    icon = {
                        Icon(imageVector = Icons.Rounded.CameraAlt, contentDescription = null)
                    },
                    title = {
                        Text("Camera permission required")
                    },
                    text = {
                        Text("Camera permission is needed to scan items.")
                    },
                    confirmButton = {
                        TextButton(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onCameraFail) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}