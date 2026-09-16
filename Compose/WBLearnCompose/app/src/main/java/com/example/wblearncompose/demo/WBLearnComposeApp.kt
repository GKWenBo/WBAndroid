package com.example.wblearncompose.demo

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wblearncompose.demo.catalog.CatalogScreen
import com.example.wblearncompose.demo.screens.AnimationDemoScreen
import com.example.wblearncompose.demo.screens.EffectsDemoScreen
import com.example.wblearncompose.demo.screens.LayoutDemoScreen
import com.example.wblearncompose.demo.screens.WidgetsDemoScreen
import com.example.wblearncompose.demo.screens.listdetail.SampleDetailScreen
import com.example.wblearncompose.demo.screens.listdetail.SampleListScreen

/**
 * 应用的导航根：一个 NavHost 登记所有页面，NavController 负责跳转与返回。
 *
 * SwiftUI 对照：NavHost ≈ NavigationStack；每个 composable(route) ≈ 一个
 * navigationDestination；navController.navigate/popBackStack ≈ path 的 push/pop。
 *
 * 约定：各页面不直接持有 NavController，而是接收 onBack / onXxxClick 回调，
 * 由这里统一决定“点了以后去哪儿”。这样页面与导航解耦，更易复用和预览。
 */
@Composable
fun WBLearnComposeApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.CATALOG,
    ) {
        // 首页目录（列表）
        composable(Destinations.CATALOG) {
            CatalogScreen(
                onTopicClick = { route -> navController.navigate(route) },
            )
        }

        // 四大教学分类
        composable(Destinations.WIDGETS) {
            WidgetsDemoScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.LAYOUT) {
            LayoutDemoScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.EFFECTS) {
            EffectsDemoScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.ANIMATION) {
            AnimationDemoScreen(onBack = { navController.popBackStack() })
        }

        // 列表-详情实战：列表页
        composable(Destinations.SAMPLE_LIST) {
            SampleListScreen(
                onBack = { navController.popBackStack() },
                onArticleClick = { id ->
                    navController.navigate(Destinations.sampleDetailRoute(id))
                },
            )
        }

        // 列表-详情实战：详情页，声明 id 为 Int 类型的路由参数
        composable(
            route = Destinations.SAMPLE_DETAIL,
            arguments = listOf(
                navArgument(Destinations.SAMPLE_DETAIL_ARG_ID) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            // 从路由参数里取出 id（≈ 目的地拿到传入的 value）
            val id = backStackEntry.arguments?.getInt(Destinations.SAMPLE_DETAIL_ARG_ID) ?: -1
            SampleDetailScreen(
                articleId = id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
