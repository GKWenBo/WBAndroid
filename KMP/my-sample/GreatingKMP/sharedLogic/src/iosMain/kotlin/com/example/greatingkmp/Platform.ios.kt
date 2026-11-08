package com.example.greatingkmp

import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual val num: Int = 2

actual fun platformTag(): String = NSProcessInfo.processInfo.operatingSystemVersionString
