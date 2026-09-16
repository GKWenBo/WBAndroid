package com.example.wblearncompose.demo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.demo.components.DemoList
import com.example.wblearncompose.demo.components.DemoScaffold
import com.example.wblearncompose.demo.components.DemoSection
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 常用控件详情页。
 *
 * 核心概念：Compose 是「声明式 + 单向数据流」。控件不持有自己的状态，
 * 而是你用 remember { mutableStateOf(...) } 声明状态（≈ SwiftUI @State），
 * 把当前值传给控件显示（value=），控件回调里再把新值写回状态（onValueChange=）。
 * 状态一变，界面自动重组刷新。
 */
@Composable
fun WidgetsDemoScreen(onBack: () -> Unit) {
    DemoScaffold(title = "常用控件", onBack = onBack) { padding ->
        DemoList(contentPadding = padding) {
            item { ButtonsSection() }
            item { TextStylesSection() }
            item { TextFieldSection() }
            item { CheckboxSection() }
            item { SwitchSection() }
            item { SliderSection() }
            item { RadioGroupSection() }
        }
    }
}

@Composable
private fun ButtonsSection() = DemoSection(
    title = "按钮 Button",
    description = "填充/描边/文字三种常见样式，onClick 里写点击逻辑（≈ Button action）。",
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {}) { Text("填充") }
        OutlinedButton(onClick = {}) { Text("描边") }
        TextButton(onClick = {}) { Text("文字") }
    }
}

@Composable
private fun TextStylesSection() = DemoSection(
    title = "文本 Text",
    description = "通过 style 用主题字号，或用 fontWeight / fontStyle / textDecoration 局部调整。",
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("标题样式 titleLarge", style = MaterialTheme.typography.titleLarge)
        Text("加粗", fontWeight = FontWeight.Bold)
        Text("斜体", fontStyle = FontStyle.Italic)
        Text("删除线", textDecoration = TextDecoration.LineThrough)
        Text("主题色文字", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TextFieldSection() = DemoSection(
    title = "输入框 OutlinedTextField",
    description = "remember 声明状态（≈ @State），value 显示、onValueChange 写回，实现受控输入。",
) {
    // 状态提升的最小示例：文字存在这个 State 里
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("请输入内容") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CheckboxSection() = DemoSection(
    title = "复选框 Checkbox",
    description = "布尔状态驱动勾选；把点击范围放到整行更好点。",
) {
    var checked by remember { mutableStateOf(true) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { checked = it })
        Text("我已阅读并同意")
    }
}

@Composable
private fun SwitchSection() = DemoSection(
    title = "开关 Switch",
    description = "≈ SwiftUI Toggle，常用于设置项。",
) {
    var on by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = on, onCheckedChange = { on = it })
        Text(if (on) "已开启" else "已关闭", modifier = Modifier)
    }
}

@Composable
private fun SliderSection() = DemoSection(
    title = "滑块 Slider",
    description = "连续取值 0f..1f；value/onValueChange 同样是受控模式。",
) {
    var value by remember { mutableStateOf(0.3f) }
    Column {
        Slider(value = value, onValueChange = { value = it })
        Text("当前值：${(value * 100).toInt()}%")
    }
}

@Composable
private fun RadioGroupSection() = DemoSection(
    title = "单选组 RadioButton",
    description = "一组选项共享一个“被选中项”状态，selectable 让整行可点。",
) {
    val options = listOf("支付宝", "微信", "银行卡")
    var selected by remember { mutableStateOf(options.first()) }
    Column {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selected),
                        onClick = { selected = option },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = (option == selected),
                    onClick = { selected = option },
                )
                Text(option)
            }
        }
    }
}

@Suppress("unused")
@Composable
private fun IconsPreviewHint() {
    // 图标示例：矢量图标随处可用
    Row {
        Icon(Icons.Filled.Star, contentDescription = null)
        Icon(Icons.Filled.Favorite, contentDescription = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun WidgetsDemoScreenPreview() {
    WBLearnComposeTheme {
        WidgetsDemoScreen(onBack = {})
    }
}
