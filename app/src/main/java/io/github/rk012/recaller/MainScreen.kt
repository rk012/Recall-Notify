package io.github.rk012.recaller

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCameraStart: () -> Unit,
    cameraData: String?
) {
    var isConsumerState by remember { mutableStateOf(true) }

    var startCamera by remember { mutableStateOf(false) }
    var showInputForm by remember { mutableStateOf(false) }

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
                FloatingActionButton(onClick = { showInputForm = true }) {
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
        } else {
            SellerContent(padding = it)
        }
    }
}

object ProductSaver : Saver<List<Product>, String> {
    override fun restore(value: String): List<Product>? = Json.decodeFromString(value)
    override fun SaverScope.save(value: List<Product>): String = Json.encodeToString(value)
}

object SellerProductSaver : Saver<List<SellerProduct>, String> {
    override fun restore(value: String): List<SellerProduct>? = Json.decodeFromString(value)
    override fun SaverScope.save(value: List<SellerProduct>): String = Json.encodeToString(value)
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
private fun SellerContent(
    padding: PaddingValues
) {
    var sellerProducts = rememberSaveable(saver = SellerProductSaver) {
        emptyList()
    }

    val scope = rememberCoroutineScope()
    
    LazyColumn {
        items(sellerProducts) { sellerProduct ->
            ProductCard(product = sellerProduct.product, padding = padding) {
                scope.launch {
                    issueRecall(sellerProduct.token)
                    sellerProducts = sellerProducts.dropWhile { it == sellerProduct }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, padding: PaddingValues, onRecallIssue: () -> Unit) = Card(
    modifier = Modifier.padding(padding)
) {
    Row {
        Column(modifier = Modifier) {
            Text(product.name)
            Text(product.seller)
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onRecallIssue) {
            Text("Issue recall")
        }
    }
}