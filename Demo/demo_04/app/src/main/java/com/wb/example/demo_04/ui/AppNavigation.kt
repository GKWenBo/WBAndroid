package com.wb.example.demo_04.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wb.example.demo_04.ui.characters.CharacterDetailScreen
import com.wb.example.demo_04.ui.characters.CharactersScreen

// Compose Navigation = iOS 的 NavigationStack / Coordinator。
// 路由字符串 + 参数（"character/{id}"）对应 Swift 的 NavigationLink(value:)。
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "characters") {
        composable("characters") {
            CharactersScreen(
                onCharacterClick = { id -> navController.navigate("character/$id") }
            )
        }
        composable(
            route = "character/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            CharacterDetailScreen(characterId = id)
        }
    }
}
