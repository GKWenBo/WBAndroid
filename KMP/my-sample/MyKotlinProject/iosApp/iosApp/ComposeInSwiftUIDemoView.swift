import SwiftUI
import Shared

/// 把 Kotlin 侧的 Compose 视图（ComposeChildViewController）桥进 SwiftUI。
struct ComposeChildRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ComposeInteropKt.ComposeChildViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// 反向示例：最外层是 SwiftUI，中间嵌一个 Compose 子视图。
/// 配合外层的 Compose 画廊，形成 Compose → SwiftUI → Compose 的完整双向演示。
struct ComposeInSwiftUIDemoView: View {
    var body: some View {
        VStack(spacing: 0) {
            Text("这是 SwiftUI 外层")
                .font(.headline)
                .padding()

            ComposeChildRepresentable()
        }
    }
}
