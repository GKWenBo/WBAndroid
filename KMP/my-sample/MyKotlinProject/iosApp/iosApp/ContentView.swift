import UIKit
import SwiftUI
import Shared

/// 用 Compose 承载整个示例画廊；把 Swift 的 NativeViewFactory 实现传进去。
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(factory: IOSNativeViewFactory())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

#Preview {
    ContentView()
}
