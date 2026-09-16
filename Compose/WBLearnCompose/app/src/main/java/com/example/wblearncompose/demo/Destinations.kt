package com.example.wblearncompose.demo

/**
 * 路由表：集中管理所有页面的路由字符串。
 *
 * SwiftUI 对照：Compose 用「字符串路由」描述导航目的地，类似你在
 * NavigationStack 里用 value/path 驱动 destination。把路由集中成常量，
 * 避免在各处手写字符串时打错字。
 */
object Destinations {
    /** 首页目录（列表） */
    const val CATALOG = "catalog"

    /** 四大教学分类详情页 */
    const val WIDGETS = "widgets"
    const val LAYOUT = "layout"
    const val EFFECTS = "effects"
    const val ANIMATION = "animation"

    /** 「列表-详情」实战：列表页 */
    const val SAMPLE_LIST = "sampleList"

    /**
     * 「列表-详情」实战：详情页，带一个 id 参数。
     * 真机跳转时用 sampleDetailRoute(id) 生成实际路径，例如 "sampleDetail/2"。
     */
    const val SAMPLE_DETAIL = "sampleDetail/{id}"
    const val SAMPLE_DETAIL_ARG_ID = "id"

    fun sampleDetailRoute(id: Int) = "sampleDetail/$id"
}
