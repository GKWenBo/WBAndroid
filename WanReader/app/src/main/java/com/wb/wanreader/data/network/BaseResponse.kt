package com.wb.wanreader.data.network

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    // 不默认成 0：缺少协议字段必须暴露，而不是被误判为成功。
    val errorCode: Int,
    val errorMsg: String = "",
    val data: T? = null
)
