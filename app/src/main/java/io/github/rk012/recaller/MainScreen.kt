package io.github.rk012.recaller

import android.Manifest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCameraStart: () -> Unit,
    cameraData: String?
) {
    var isConsumerState by remember { mutableStateOf(true) }
    var startCamera by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        if (isConsumerState) {
            ConsumerContent(
                startCamera,
                onCameraStart,
                onCameraFail = { showSnackbar ->
                    if (showSnackbar) scope.launch {
                        if (snackbarHostState.currentSnackbarData == null) snackbarHostState.showSnackbar(
                            message = "Camera permission required for this feature",
                            withDismissAction = true
                        )
                    }
                    startCamera = false
                },
                cameraData = cameraData,
                padding = it
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ConsumerContent(
    startCamera: Boolean,
    onCameraStart: () -> Unit,
    onCameraFail: (Boolean) -> Unit,
    cameraData: String?,
    padding: PaddingValues
) {
    var showCameraDialog by remember { mutableStateOf(true) }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA) {
        showCameraDialog = it
    }

    cameraData?.let {
        Text(text = it, modifier = Modifier.padding(padding))
    }

    // TODO


    when (cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            if (startCamera) onCameraStart()
        }
        is PermissionStatus.Denied -> {
            if (showCameraDialog && startCamera) {
                AlertDialog(
                    onDismissRequest = { onCameraFail(false) },
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
                        TextButton(onClick = { onCameraFail(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            } else if (startCamera) {
                onCameraFail(true)
            }
        }
    }
}

@Composable
private fun SellerContent() {

}