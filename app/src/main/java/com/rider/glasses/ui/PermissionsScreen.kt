package com.rider.glasses.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.content.pm.PackageManager

private val RUNTIME_PERMISSIONS = buildList {
    add(Manifest.permission.BLUETOOTH_SCAN)
    add(Manifest.permission.BLUETOOTH_CONNECT)
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.RECORD_AUDIO)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

private val PERMISSION_LABELS = mapOf(
    Manifest.permission.BLUETOOTH_SCAN to "Bluetooth Scan",
    Manifest.permission.BLUETOOTH_CONNECT to "Bluetooth Connect",
    Manifest.permission.ACCESS_FINE_LOCATION to "Location",
    Manifest.permission.RECORD_AUDIO to "Microphone",
    Manifest.permission.POST_NOTIFICATIONS to "Notifications"
)

@Composable
fun PermissionsScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun isListenerEnabled() =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    var grantedMap by remember {
        mutableStateOf(RUNTIME_PERMISSIONS.associateWith { isGranted(it) })
    }
    var listenerEnabled by remember { mutableStateOf(isListenerEnabled()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                grantedMap = RUNTIME_PERMISSIONS.associateWith { isGranted(it) }
                listenerEnabled = isListenerEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allRuntimeGranted = grantedMap.values.all { it }

    if (allRuntimeGranted && listenerEnabled) {
        onAllGranted()
        return
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        grantedMap = grantedMap + results
    }

    val anyPermanentlyDenied = RUNTIME_PERMISSIONS.any { permission ->
        !isGranted(permission) && !shouldShowRationale(context, permission)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Glasses needs a few permissions", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        RUNTIME_PERMISSIONS.forEach { permission ->
            val label = PERMISSION_LABELS[permission] ?: permission
            val granted = grantedMap[permission] == true
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (granted) "Granted" else "Required",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notification Access", style = MaterialTheme.typography.bodyMedium)
            Text(
                if (listenerEnabled) "Enabled" else "Required",
                style = MaterialTheme.typography.labelSmall,
                color = if (listenerEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        if (!allRuntimeGranted) {
            Button(
                onClick = {
                    launcher.launch(RUNTIME_PERMISSIONS.filter { !isGranted(it) }.toTypedArray())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }

        if (anyPermanentlyDenied) {
            Spacer(Modifier.height(8.dp))
            Text(
                "One or more permissions were permanently denied.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open App Settings")
            }
        }

        if (!listenerEnabled) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Notification Access")
            }
        }
    }
}

private fun shouldShowRationale(context: android.content.Context, permission: String): Boolean {
    return (context as? androidx.activity.ComponentActivity)
        ?.shouldShowRequestPermissionRationale(permission) ?: true
}
