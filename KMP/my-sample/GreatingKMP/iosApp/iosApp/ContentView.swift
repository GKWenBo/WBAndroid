import SwiftUI
import SharedLogic

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
    
    var body: some View {
        ListView(phrases: viewModel.greetings)
            .task {
                await self.viewModel.startOberving()
            }
    }
}


struct ListView: View {
    let phrases: Array<String>

    var body: some View {
        List(phrases, id: \.self) {
            Text($0)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(viewModel: ViewModel())
    }
}
