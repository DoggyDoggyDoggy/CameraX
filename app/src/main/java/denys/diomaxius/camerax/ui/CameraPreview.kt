package denys.diomaxius.camerax.ui

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FIT_CENTER
            }

            val previewUseCase = Preview.Builder().build()
            previewUseCase.setSurfaceProvider(previewView.surfaceProvider)

            val listenableFuture = ProcessCameraProvider.getInstance(context)
            listenableFuture.addListener({
                val cameraProvider = listenableFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase)
            }, ContextCompat.getMainExecutor(context))

            previewView
        }
    )
}