package com.example.wblearncompose.demo.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.demo.components.DemoList
import com.example.wblearncompose.demo.components.DemoScaffold
import com.example.wblearncompose.demo.components.DemoSection
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 动画详情页。
 *
 * Compose 动画心法：你只声明「目标状态」，动画 API 负责把当前值平滑过渡到目标值。
 * 状态一变，界面自动做补间——这和 SwiftUI 的 withAnimation { state = ... } 思路一致。
 */
@Composable
fun AnimationDemoScreen(onBack: () -> Unit) {
    DemoScaffold(title = "动画", onBack = onBack) { padding ->
        DemoList(contentPadding = padding) {
            item { AnimateAsStateSection() }
            item { AnimatedVisibilitySection() }
            item { CrossfadeSection() }
            item { InfiniteTransitionSection() }
        }
    }
}

@Composable
private fun AnimateAsStateSection() = DemoSection(
    title = "animate*AsState：值补间",
    description = "点击切换目标状态，颜色与尺寸自动平滑过渡，无需手写动画帧。",
) {
    var expanded by remember { mutableStateOf(false) }
    // 目标值随 expanded 改变，animate*AsState 负责补间
    val color by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondary,
        label = "color",
    )
    val size by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (expanded) 120.dp else 64.dp,
        animationSpec = tween(durationMillis = 400),
        label = "size",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
        )
        Button(onClick = { expanded = !expanded }) {
            Text(if (expanded) "收起" else "展开")
        }
    }
}

@Composable
private fun AnimatedVisibilitySection() = DemoSection(
    title = "AnimatedVisibility：进出场",
    description = "包裹的内容出现/消失时自动带淡入淡出 + 展开收起动画。",
) {
    var visible by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "隐藏" else "显示")
        }
        AnimatedVisibility(visible = visible) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("我会带动画进出场") }
            }
        }
    }
}

@Composable
private fun CrossfadeSection() = DemoSection(
    title = "Crossfade：内容交叉淡入",
    description = "在两个界面/状态之间切换时交叉淡化，常用于 Tab 或加载态切换。",
) {
    var page by remember { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { page = (page + 1) % 3 }) { Text("切换页面") }
        Crossfade(targetState = page, label = "crossfade") { current ->
            Surface(
                color = when (current) {
                    0 -> MaterialTheme.colorScheme.primaryContainer
                    1 -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("第 ${current + 1} 页") }
            }
        }
    }
}

@Composable
private fun InfiniteTransitionSection() = DemoSection(
    title = "无限动画 rememberInfiniteTransition",
    description = "循环往复的动画（呼吸、闪烁、加载指示），无需手动重启。",
) {
    val transition = rememberInfiniteTransition(label = "infinite")
    // 透明度在 0.3~1 之间来回，形成呼吸效果
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    // 尺寸也做无限缩放（InfiniteTransition 只提供 animateFloat/animateColor，
    // 所以这里补间一个 Float 再转成 Dp）
    val boxSize by transition.animateFloat(
        initialValue = 48f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "boxSize",
    )
    Box(
        modifier = Modifier
            .size(boxSize.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE53935)),
    )
}

@Preview(showBackground = true)
@Composable
private fun AnimationDemoScreenPreview() {
    WBLearnComposeTheme {
        AnimationDemoScreen(onBack = {})
    }
}
