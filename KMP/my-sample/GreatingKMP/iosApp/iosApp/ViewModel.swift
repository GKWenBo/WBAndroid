//
//  ViewModel.swift
//  iosApp
//
//  Created by 文波 on 2026/7/16.
//
import Combine
import SharedLogic

@MainActor
class ViewModel: ObservableObject {
    @Published var greetings: Array<String> = []
    
    func startOberving() async {
        for await phrase in Greeting().greet() {
            self.greetings.append(phrase)
        }
    }
}
