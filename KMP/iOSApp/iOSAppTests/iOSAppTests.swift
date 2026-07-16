//
//  iOSAppTests.swift
//  iOSAppTests
//
//  Created by 文波 on 2026/7/15.
//

import Testing
@testable import iOSApp
import Shared

struct iOSAppTests {

    @Test func example() async throws {
        // Write your test here and use APIs like `#expect(...)` to check expected conditions.
        // Swift Testing Documentation
        // https://developer.apple.com/documentation/testing
    }
    
    @Test func test() {
        let seq = CustomFibiKt.generateFibi()
        var iterator = seq.iterator()
          for _ in 0..<10 {
              print(iterator.next() ?? 0)
          }
    }

}
