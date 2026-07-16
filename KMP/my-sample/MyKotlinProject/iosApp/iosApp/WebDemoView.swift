import SwiftUI
import WebKit

/// 用 UIViewRepresentable 把 UIKit 的 WKWebView 桥进 SwiftUI（SwiftUI 原生 WebView 需 iOS 26+）。
struct WebView: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> WKWebView {
        WKWebView()
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        // 仅首次加载，避免 SwiftUI 更新时把页内跳转弹回首页
        if webView.url == nil {
            webView.load(URLRequest(url: url))
        }
    }
}

struct WebDemoView: View {
    let urlString: String

    var body: some View {
        if let url = URL(string: urlString) {
            WebView(url: url)
                .ignoresSafeArea()
        } else {
            Text("URL 无效：\(urlString)")
        }
    }
}
