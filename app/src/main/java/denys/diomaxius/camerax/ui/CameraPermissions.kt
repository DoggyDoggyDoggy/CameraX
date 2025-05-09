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
        // Разрешение уже есть
        cameraPermissionState.status.isGranted -> {
            CameraPreviewWithFilters()
        }

        // Пользователь уже отказал один раз, можно показать rationale
        cameraPermissionState.status.shouldShowRationale -> {
            RationaleContent(
                message = "Нам действительно нужно разрешение на камеру, чтобы показывать превью и делать фото.",
                buttonText = "Показать запрос",
                onRequest = { cameraPermissionState.launchPermissionRequest() }
            )
        }

        // Остальные случаи — показываем что без разрешения не работает
        else -> {
            RationaleContent(
                message = "Камера недоступна без разрешения. Вы можете включить его в настройках приложения.",
                buttonText = "Открыть настройки",
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