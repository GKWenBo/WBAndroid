import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
//        MainViewControllerKt.MyMainViewController()
        
        /// Compose 嵌入SwiftUI
        MainViewControllerKt.ComposeEntryPointWithUIViewController {
            let swiftUIView = VStack {
                Text("How to use SwiftUI inside Compose Multiplatform")
            }
            return UIHostingController(rootView: swiftUIView)
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
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
