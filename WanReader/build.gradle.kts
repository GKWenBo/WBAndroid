// 根 build.gradle.kts —— 只做一件事：把插件"登记"到类路径但不应用（apply false），
// 由各模块自行选用。iOS 对照：≈ Podfile 顶部的平台/源声明，不含具体 target 配置。
plugins {
    alias(libs.plugins.android.application) apply false
    // S1 新增：KSP（注解处理）与 Hilt（DI），版本在此仲裁，模块自行应用
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
