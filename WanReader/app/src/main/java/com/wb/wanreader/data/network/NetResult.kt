package com.wb.wanreader.data.network

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

sealed interface NetResult<out T> {
    data class Success<T>(val value: T) : NetResult<T>
    data class Failure(val error: NetError) : NetResult<Nothing>
}

sealed interface NetError {
    data class Business(val code: Int, val message: String) : NetError
    data class Http(val code: Int) : NetError
    data object Network : NetError
    data object Decode : NetError
    data object EmptyData : NetError
}

// 用于需要非空 data 的查询接口；未来返回空 data 的写接口应有独立契约。
internal suspend fun <T : Any> apiCall(block: suspend () -> BaseResponse<T>): NetResult<T> =
    try {
        val response = block()
        when {
            response.errorCode != 0 -> NetResult.Failure(
                NetError.Business(response.errorCode, response.errorMsg))
            response.data == null -> NetResult.Failure(NetError.EmptyData)
            else -> NetResult.Success(response.data)
        }
    } catch (cancelled: CancellationException) {
        // 取消属于结构化并发的控制流，不能变成“网络错误”。
        throw cancelled
    } catch (http: HttpException) {
        NetResult.Failure(NetError.Http(http.code()))
    } catch (_: SerializationException) {
        NetResult.Failure(NetError.Decode)
    } catch (_: IOException) {
        NetResult.Failure(NetError.Network)
    }
