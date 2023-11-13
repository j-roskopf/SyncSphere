package com.joetr.sync.sphere.design.toolbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.joetr.sync.sphere.design.theme.AppTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DefaultToolbar(
    title: String,
    onBack: (() -> Unit)? = null,
) {
    AppTheme {
        Surface {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = {
                                onBack()
                            },
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                null,
                            )
                        }
                    }
                },
            )
        }
    }
}

internal fun Navigator.backOrNull(): (() -> Unit)? {
    if (this.canPop) {
        return {
            this.pop()
        }
    } else {
        return null
    }
}
