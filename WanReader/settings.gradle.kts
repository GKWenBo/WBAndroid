// settings.gradle.kts —— 工程的"户口本"：声明有哪些模块、插件和依赖从哪里下载。
// iOS 对照：≈ Xcode 的 project.pbxproj（模块清单）+ SPM 的 Package.resolved 来源配置。
pluginManagement {
    plugins {
        // Kotlin 版本的【单一真源】：只在这里写版本号，模块内引用插件一律不写版本。
        // 坑①：AGP 9.2.1 会自动应用 kotlin.android 插件，
        //       模块 plugins 块里【不要】再声明 id("org.jetbrains.kotlin.android")，
        //       否则报 extension 'kotlin' already registered（demo_02 踩过）。
        id("org.jetbrains.kotlin.android") version "2.3.20"
        // Compose 编译器插件（Kotlin 2.x 起随 Kotlin 同版本发布），必须显式声明。
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
    }
    repositories {
        google {
            content {
                // 只到 Google 仓库找这三类组件，加速解析（国内网络尤其明显）
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
    // 自动下载匹配的 JDK 工具链，避免"本机 JDK 版本不对"一类的坑
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // 企业规范：禁止模块私自声明仓库，依赖来源全部集中在这里管控
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WanReader"
include(":app")
