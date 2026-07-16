import SwiftUI
import Shared

/// Kotlin NativeViewFactory 的 Swift 实现。
/// 每个方法把一个 SwiftUI 视图包进 UIHostingController 返回给 Compose。
/// 本步先用占位视图，后续任务逐个替换成真实的 Map / Web / 图片选择 / 反向嵌套。
class IOSNativeViewFactory: NativeViewFactory {
    func createMapView(onCoordinatePicked: @escaping (KotlinDouble, KotlinDouble) -> Void) -> UIViewController {
        UIHostingController(rootView: MapDemoView(onCoordinatePicked: onCoordinatePicked))
    }

    func createWebView(urlString: String) -> UIViewController {
        UIHostingController(rootView: WebDemoView(urlString: urlString))
    }

    func createImagePicker(onImagePicked: @escaping (KotlinByteArray) -> Void) -> UIViewController {
        UIHostingController(rootView: ImagePickerDemoView(onImagePicked: onImagePicked))
    }

    func createComposeInSwiftUIView() -> UIViewController {
        UIHostingController(rootView: ComposeInSwiftUIDemoView())
    }
}
