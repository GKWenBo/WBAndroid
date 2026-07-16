import SwiftUI
import MapKit
import Shared

/// MapKit 地图（iOS 17+ Map API）。点击地图落一个标注，并把坐标回传给 Compose。
struct MapDemoView: View {
    let onCoordinatePicked: (KotlinDouble, KotlinDouble) -> Void

    @State private var position: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 39.9042, longitude: 116.4074),
            span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
        )
    )
    @State private var picked: CLLocationCoordinate2D?

    var body: some View {
        MapReader { proxy in
            Map(position: $position) {
                if let picked {
                    Marker("已选", coordinate: picked)
                }
            }
            .onTapGesture { screenPoint in
                if let coord = proxy.convert(screenPoint, from: .local) {
                    picked = coord
                    onCoordinatePicked(
                        KotlinDouble(double: coord.latitude),
                        KotlinDouble(double: coord.longitude)
                    )
                }
            }
        }
    }
}
