import SwiftUI
import PhotosUI
import Shared

/// Data → KotlinByteArray 的桥接（逐字节拷贝，演示用；大图可换更快的方式）。
extension Data {
    func toKotlinByteArray() -> KotlinByteArray {
        let array = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            array.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return array
    }
}

/// PhotosPicker 选图（iOS 16+，进程外选择，无需相册权限描述）。
/// 选完把图片转 PNG 字节，通过回调回传给 Compose 显示。
struct ImagePickerDemoView: View {
    let onImagePicked: (KotlinByteArray) -> Void

    @State private var selection: PhotosPickerItem?
    @State private var previewImage: UIImage?

    var body: some View {
        VStack(spacing: 16) {
            PhotosPicker("选择照片", selection: $selection, matching: .images)
                .buttonStyle(.borderedProminent)

            if let previewImage {
                Image(uiImage: previewImage)
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 200)
            } else {
                Text("尚未选择，选完会同时回传给 Compose 显示")
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .onChange(of: selection) { _, newItem in
            Task {
                guard let data = try? await newItem?.loadTransferable(type: Data.self),
                      let uiImage = UIImage(data: data),
                      let png = uiImage.pngData() else { return }
                previewImage = uiImage
                onImagePicked(png.toKotlinByteArray())
            }
        }
    }
}
