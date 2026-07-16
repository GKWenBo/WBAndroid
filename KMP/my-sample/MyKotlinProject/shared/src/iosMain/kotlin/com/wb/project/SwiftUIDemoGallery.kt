package com.wb.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.Image as SkiaImage
import platform.UIKit.UIViewController

/**
 * 顶层画廊：selectedKind 为 null 显示列表，否则显示全屏 SwiftUI 详情。
 * 这是「不用注释代码、直接交互浏览」的核心：点列表项进详情，点返回回列表。
 */
@Composable
fun SwiftUIDemoGallery(factory: NativeViewFactory) {
    var selectedKind by remember { mutableStateOf<DemoKind?>(null) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            val current = selectedKind
            if (current == null) {
                DemoList(onSelect = { selectedKind = it })
            } else {
                DemoDetail(kind = current, factory = factory, onBack = { selectedKind = null })
            }
        }
    }
}

@Composable
private fun DemoList(onSelect: (DemoKind) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("Compose ↔ SwiftUI 示例", modifier = Modifier.padding(16.dp), fontSize = 22.sp)
        LazyColumn(Modifier.fillMaxSize()) {
            items(demoItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onSelect(item.kind) },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.title, fontSize = 18.sp)
                        Text(item.subtitle, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun DemoDetail(kind: DemoKind, factory: NativeViewFactory, onBack: () -> Unit) {
    var coordinate by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var pickedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val title = remember(kind) { demoItems.firstOrNull { it.kind == kind }?.title ?: kind.name }

    // 关键：用 remember(kind) 缓存工厂 lambda，避免回传导致 DemoDetail 重组时
    // 重新创建原生视图（否则地图会因每次点击重建而丢状态）。回传用的 setter 是稳定的。
    val nativeFactory: () -> UIViewController = remember(kind) {
        when (kind) {
            DemoKind.MAP -> {
                { factory.createMapView { lat, lng -> coordinate = lat to lng } }
            }
            DemoKind.WEB -> {
                { factory.createWebView("https://www.jetbrains.com/lp/compose-multiplatform/") }
            }
            DemoKind.IMAGE_PICKER -> {
                { factory.createImagePicker { bytes -> pickedImage = decodeImage(bytes) } }
            }
            DemoKind.COMPOSE_IN_SWIFTUI -> {
                { factory.createComposeInSwiftUIView() }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) { Text("← 返回") }
            Spacer(Modifier.width(12.dp))
            Text(title, fontSize = 18.sp)
        }

        coordinate?.let { (lat, lng) ->
            Text("回传坐标：lat=$lat, lng=$lng", Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }
        pickedImage?.let { image ->
            Image(image, contentDescription = "回传图片", modifier = Modifier.fillMaxWidth().height(200.dp))
        }

        UIKitViewController(factory = nativeFactory, modifier = Modifier.fillMaxSize())
    }
}

/** PNG/JPEG 字节 → Compose ImageBitmap（iOS 上走 Skia/skiko）。 */
private fun decodeImage(bytes: ByteArray): ImageBitmap =
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
