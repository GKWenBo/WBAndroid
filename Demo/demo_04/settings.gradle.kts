pluginManagement {
    plugins {
        // Kotlin 版本在此统一声明，供下方插件引用，避免 “unknown version” 冲突。
        // 注意：本机 AGP 9.2.1 会自动应用 kotlin.android，模块里【不要】单独声明它；
        // 其余 Kotlin 插件（compose / serialization）与 KSP 必须显式声明并带版本。
        id("org.jetbrains.kotlin.android") version "2.3.20"
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
        id("com.google.devtools.ksp") version "2.3.10"
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "demo_04"
include(":app")
