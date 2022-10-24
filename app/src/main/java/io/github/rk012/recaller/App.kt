package io.github.rk012.recaller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    Scaffold(
        bottomBar = {
            NavigationBar() {
                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = {

                    },
                    label = {
                        Text("Item 1")
                    }
                )

                NavigationBarItem(
                    selected = true,
                    onClick = { /*TODO*/ },
                    icon = {

                    },
                    label = {
                        Text("Item 2")
                    }
                )
            }
        },
        floatingActionButton = {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "" // TODO
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) {

    }
}