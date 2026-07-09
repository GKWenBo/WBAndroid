在 VSCode 里写和运行 Kotlin 是可行的，但要先说明：Kotlin 的官方支持在 IntelliJ IDEA / Android Studio 里最好，VSCode 更适合写脚本、小程序或临时练手。下面是 Mac 上的完整配置流程。

## 1. 装 JDK

Kotlin 依赖 JVM，先确认有 JDK（17 或以上）：

```bash
java -version
```

没有的话用 Homebrew 装：

```bash
brew install openjdk@17
```

## 2. 装 Kotlin 编译器

推荐用 SDKMAN（版本管理方便），或直接 Homebrew：

```bash
# 方式一：Homebrew
brew install kotlin

# 方式二：SDKMAN
curl -s "https://get.sdkman.io" | bash
sdk install kotlin
```

装完验证：

```bash
kotlinc -version
```

## 3. 装 VSCode 扩展

- **Kotlin**（fwcd 出品，`fwcd.kotlin`）：提供语法高亮、补全、跳转，基于 Kotlin Language Server
- **Code Runner**（`formulahendry.code-runner`）：一键运行

## 4. 写代码并运行

新建 `hello.kt`：

```kotlin
fun main() {
    println("Hello, Kotlin!")
}
```

运行方式任选：

- **命令行**：`kotlinc hello.kt -include-runtime -d hello.jar && java -jar hello.jar`
- **脚本模式**（快，适合小程序）：把文件命名为 `hello.kts`，然后 `kotlinc -script hello.kts`
- **Code Runner**：右上角三角按钮或 `Ctrl+Alt+N`

## 需要注意的点

VSCode 的 Kotlin Language Server 首次加载较慢，对大项目、Gradle 工程支持有限，补全和错误提示不如 IntelliJ 稳。如果只是跑单文件练习完全够用；一旦涉及 Android 或多模块 Gradle 项目，还是回 Android Studio 更省心。

要不要我帮你写个 `.vscode/tasks.json`，把编译+运行做成一个快捷任务？