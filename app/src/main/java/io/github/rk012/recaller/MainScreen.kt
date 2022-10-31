package io.github.rk012.recaller

import android.Manifest
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glxn.qrgen.android.QRCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCameraStart: () -> Unit,
    cameraData: String?
) {
    var isConsumerState by remember { mutableStateOf(true) }

    var startCamera by remember { mutableStateOf(false) }
    var showInputForm by remember { mutableStateOf(false) }

    var qrCodeData: String? by remember { mutableStateOf(null) }

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
    ) { padding ->
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
                padding = padding
            )
        } else {
            SellerContent(
                showInputForm = showInputForm,
                resetInputForm = { showInputForm = false },
                qrCodeData = qrCodeData,
                setQrCodeData = { qrCodeData = it },
                padding = padding
            )
        }
    }
}

class AppViewModel : ViewModel() {
    val userProducts = mutableStateListOf<Product>()
    val sellerProducts = mutableStateListOf<SellerProduct>()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerContent(
    showInputForm: Boolean,
    resetInputForm: () -> Unit,
    qrCodeData: String?,
    setQrCodeData: (String?) -> Unit,
    padding: PaddingValues,
    viewModel: AppViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    
    LazyColumn {
        items(viewModel.sellerProducts) { sellerProduct ->
            ProductCard(product = sellerProduct.product, padding = padding) {
                scope.launch {
                    issueRecall(sellerProduct.token)
                    viewModel.sellerProducts.remove(sellerProduct)
                }
            }
        }
    }

    if (showInputForm) Dialog(onDismissRequest = resetInputForm) {
        var name by remember { mutableStateOf("") }
        var seller by remember { mutableStateOf("") }
        var enableButton by remember { mutableStateOf(true) }

        Column {
            TextField(value = name, onValueChange = { name = it }, placeholder = { Text(text = "Name") })
            TextField(value = seller, onValueChange = { seller = it }, placeholder = { Text(text = "Seller") })
            Button(onClick = {
                scope.launch {
                    enableButton = false
                    val productInfo = getRecallId()
                    val product = SellerProduct(
                        product = Product(
                            name = name,
                            seller = seller,
                            id = productInfo.id.toLong()
                        ),
                        token = productInfo.token
                    )

                    viewModel.sellerProducts.add(product)
                    setQrCodeData(Json.encodeToString(product.product))
                    resetInputForm()
                }
            }, enabled = enableButton) {
                Text(text = "Submit")
            }
        }
    }

    qrCodeData?.let {
        Dialog(
            onDismissRequest = {
                setQrCodeData(null)
            }
        ) {
            Image(
                bitmap = QRCode.from(qrCodeData).bitmap().asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize(0.9f)
            )
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