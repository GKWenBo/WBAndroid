package com.wb.wanreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wb.wanreader.ui.MainScreen
import com.wb.wanreader.ui.theme.WanReaderTheme
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint：让这个 Activity 能接住 Hilt 容器里的依赖
// （hiltViewModel() 能工作的前提）。忘加它是 Hilt 最高频报错，见 S1 文档坑②。
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WanReaderTheme {
                MainScreen()
            }
        }
    }
}
