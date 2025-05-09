package denys.diomaxius.camerax.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

import androidx.lifecycle.compose.LocalLifecycleOwner


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CameraPreviewWithFilters() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Получаем ProcessCameraProvider (для CameraX)
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Переменная состояния для выбранного фильтра (0 – ч/б, 1 – ретро1, 2 – ретро2)
    var selectedFilter by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                // Применяем графический эффект в зависимости от выбранного фильтра (обсуждается ниже)
                .graphicsLayer {
                    renderEffect = when (selectedFilter) {
                        0 -> RenderEffect.createColorFilterEffect(
                            ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
                        ).asComposeRenderEffect() // ✅

                        1 -> RenderEffect.createColorFilterEffect(
                            ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                                0.393f, 0.769f, 0.189f, 0f, 0f,
                                0.349f, 0.686f, 0.168f, 0f, 0f,
                                0.272f, 0.534f, 0.131f, 0f, 0f,
                                0f,     0f,     0f,     1f, 0f
                            )))
                        ).asComposeRenderEffect() // ✅

                        2 -> {
                            val coldMatrix = ColorMatrix().apply { setScale(0.8f, 0.8f, 1.2f, 1f) }
                            val coldFilter = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(coldMatrix))
                            val blur = RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)
                            RenderEffect.createChainEffect(coldFilter, blur).asComposeRenderEffect() // ✅
                        }

                        else -> null
                    }

                },
            factory = { ctx ->
                // Создаем PreviewView и настраиваем параметры
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE // Компатибилити-режим:contentReference[oaicite:3]{index=3}
                    scaleType = PreviewView.ScaleType.FILL_START
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Запускаем привязку CameraX после создания View
                    post {
                        cameraProviderFuture.addListener(Runnable {
                            val cameraProvider = cameraProviderFuture.get()
                            // Создаем use case для превью
                            val preview = Preview.Builder().build()
                            preview.setSurfaceProvider(surfaceProvider)
                            // Выбираем камеру (здесь задняя)
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            // Привязываем к жизненному циклу
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                }
            }
        )

        // Панель кнопок для переключения фильтров
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { selectedFilter = 0 }) {
                Text("Ч/Б")
            }
            Button(onClick = { selectedFilter = 1 }) {
                Text("Ретро 1")
            }
            Button(onClick = { selectedFilter = 2 }) {
                Text("Ретро 2")
            }
        }
    }
}
