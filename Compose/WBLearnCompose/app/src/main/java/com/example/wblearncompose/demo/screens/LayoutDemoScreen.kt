package com.example.wblearncompose.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.demo.components.DemoList
import com.example.wblearncompose.demo.components.DemoScaffold
import com.example.wblearncompose.demo.components.DemoSection
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 布局详情页。
 *
 * 三大基础容器（SwiftUI 对照）：
 *  - Column ≈ VStack（纵向排列）
 *  - Row    ≈ HStack（横向排列）
 *  - Box    ≈ ZStack（层叠，用 align 控制子项位置）
 *
 * Modifier 是布局的“链式修饰符”：padding/size/background/weight 都通过它组合，
 * 顺序会影响结果（≈ SwiftUI 的 .modifier 链）。
 */
@Composable
fun LayoutDemoScreen(onBack: () -> Unit) {
    DemoScaffold(title = "布局", onBack = onBack) { padding ->
        DemoList(contentPadding = padding) {
            item { ColumnRowSection() }
            item { WeightSection() }
            item { BoxSection() }
            item { LazyRowSection() }
            item { GridSection() }
        }
    }
}

@Composable
private fun ColumnRowSection() = DemoSection(
    title = "Column / Row + Spacer",
    description = "纵向/横向排列；用 Arrangement 控主轴间距、Alignment 控交叉轴对齐，Spacer 占位撑开。",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorBox(MaterialTheme.colorScheme.primary)
        ColorBox(MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(8.dp)) // 手动占位
        ColorBox(MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun WeightSection() = DemoSection(
    title = "weight 权重",
    description = "按比例瓜分剩余空间：1:2:1，类似弹性布局（Flexbox）里的 flex-grow。",
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        WeightBar(weight = 1f, color = MaterialTheme.colorScheme.primary, label = "1")
        WeightBar(weight = 2f, color = MaterialTheme.colorScheme.secondary, label = "2")
        WeightBar(weight = 1f, color = MaterialTheme.colorScheme.tertiary, label = "1")
    }
}

@Composable
private fun BoxSection() = DemoSection(
    title = "Box 层叠（≈ ZStack）",
    description = "子项互相层叠，用 align 决定各自的位置；这里角标钉在右上角。",
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text("居中内容", modifier = Modifier.align(Alignment.Center))
        Surface(
            color = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(bottomStart = 12.dp),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Text(
                "NEW",
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun LazyRowSection() = DemoSection(
    title = "横向列表 LazyRow",
    description = "横向可滚动、按需渲染（≈ 横向 ScrollView + LazyHStack）。",
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items((1..12).toList()) { index ->
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("#$index") }
            }
        }
    }
}

@Composable
private fun GridSection() = DemoSection(
    title = "网格 LazyHorizontalGrid",
    description = "用 GridCells.Fixed(n) 指定行/列数，网格自动排布（≈ LazyVGrid/LazyHGrid）。",
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items((1..12).toList()) { index ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("$index") }
            }
        }
    }
}

/** 一个纯色小方块，用于演示排列。 */
@Composable
private fun ColorBox(color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color),
    )
}

/** 一根按权重伸缩的色条。注意 weight 是 RowScope 的 Modifier。 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.WeightBar(
    weight: Float,
    color: Color,
    label: String,
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .height(40.dp)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Preview(showBackground = true)
@Composable
private fun LayoutDemoScreenPreview() {
    WBLearnComposeTheme {
        LayoutDemoScreen(onBack = {})
    }
}
