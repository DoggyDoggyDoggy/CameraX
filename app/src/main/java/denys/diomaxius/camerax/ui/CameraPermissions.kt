package denys.diomaxius.camerax.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissions() {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    var hasRequested by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (!hasRequested && !cameraPermissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
            hasRequested = true
        }
    }

    when {
        // Permission already exists
        cameraPermissionState.status.isGranted -> {
            CameraPreview()
        }

        // The user has already refused once, you can show rationale
        cameraPermissionState.status.shouldShowRationale -> {
            RationaleContent(
                message = "We do need camera permission to show previews and take photos.",
                buttonText = "Show request",
                onRequest = { cameraPermissionState.launchPermissionRequest() }
            )
        }

        // Other cases - we show that it doesn't work without permission
        else -> {
            RationaleContent(
                message = "The camera is not accessible without permission. You can enable it in the app settings.",
                buttonText = "Open settings",
                onRequest = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun RationaleContent(
    message: String,
    buttonText: String,
    onRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 480.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequest) {
            Text(buttonText)
        }
    }
}