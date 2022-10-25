package io.github.rk012.recaller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var isConsumerState by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            NavigationBar() {
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
                FloatingActionButton(onClick = { /*TODO*/ }) {
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
        // TODO
    }
}