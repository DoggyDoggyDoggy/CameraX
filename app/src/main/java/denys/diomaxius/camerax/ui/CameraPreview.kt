package denys.diomaxius.camerax.ui

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Log
import android.content.ContentValues
import android.provider.MediaStore

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier
) {
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraView(imageCapture = imageCapture)
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White, shape = CircleShape)
                .clip(CircleShape),
            onClick = {
                captureImage(
                    context = context,
                    imageCapture = imageCapture,
                    cameraExecutor = cameraExecutor
                )
            }
        ) {}
    }
}

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture
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
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(context))

            previewView
        }
    )
}

private fun captureImage(context: Context, imageCapture: ImageCapture, cameraExecutor: ExecutorService) {
    // 1. Form the file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(Date())
    val displayName = "IMG_$timeStamp.jpg"

    // 2. Preparing metadata for MediaStore
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    // 3. Collect saving options directly into the gallery
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // 5. Remove IS_PENDING so the photo becomes visible
                val savedUri = outputFileResults.savedUri ?: return
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(savedUri, contentValues, null, null)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("saveImage", "$exception")
            }
        }
    )
}