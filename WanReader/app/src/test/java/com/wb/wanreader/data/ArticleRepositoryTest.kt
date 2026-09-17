package com.wb.wanreader.data

import com.wb.wanreader.data.network.*
import com.wb.wanreader.data.network.dto.ArticlePageDto
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArticleRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ArticleRepository

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        repository = ArticleRepository(NetworkClient.create(server.url("/").toString(), false,
            HttpLoggingInterceptor.Logger { }))
    }

    @After fun tearDown() { server.shutdown() }

    private fun respond(body: String, code: Int = 200) {
        server.enqueue(MockResponse().setResponseCode(code)
            .addHeader("Content-Type", "application/json").setBody(body))
    }

    @Test fun successIgnoresNewFieldsAndMapsNullableAuthor() = runBlocking {
        respond("""{"errorCode":0,"data":{"curPage":1,"over":false,"datas":[{"id":7,"title":"Kotlin","link":"https://example.com","author":null,"shareUser":"分享者","newField":true}]}}""")
        val result = repository.firstPage() as NetResult.Success
        assertEquals("分享者", result.value.articles.single().author)
        assertEquals(7, result.value.articles.single().id)
        val request = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))
        assertEquals("/article/list/0/json", request.path)
        assertEquals("GET", request.method)
        assertEquals("application/json", request.getHeader("Accept"))
    }

    @Test fun businessErrorIsNotHttpSuccess() = runBlocking {
        respond("""{"errorCode":-1001,"errorMsg":"请登录","data":null}""")
        val result = repository.firstPage() as NetResult.Failure
        assertEquals(NetError.Business(-1001, "请登录"), result.error)
    }

    @Test fun positiveBusinessCodeAlsoFails() = runBlocking {
        respond("""{"errorCode":9,"errorMsg":"拒绝","data":null}""")
        assertEquals(NetError.Business(9, "拒绝"), (repository.firstPage() as NetResult.Failure).error)
    }

    @Test fun httpErrorKeepsStatusCode() = runBlocking {
        respond("unavailable", 503)
        assertEquals(NetError.Http(503), (repository.firstPage() as NetResult.Failure).error)
    }

    @Test fun missingRequiredTitleIsDecodeFailure() = runBlocking {
        respond("""{"errorCode":0,"data":{"curPage":1,"over":true,"datas":[{"id":1,"link":"https://example.com"}]}}""")
        assertEquals(NetError.Decode, (repository.firstPage() as NetResult.Failure).error)
    }

    @Test fun absentErrorCodeMustNotDefaultToSuccess() = runBlocking {
        respond("""{"data":{"curPage":1,"over":true,"datas":[]}}""")
        assertEquals(NetError.Decode, (repository.firstPage() as NetResult.Failure).error)
    }

    @Test fun nullSuccessPayloadIsProtocolFailure() = runBlocking {
        respond("""{"errorCode":0,"data":null}""")
        assertEquals(NetError.EmptyData, (repository.firstPage() as NetResult.Failure).error)
    }

    @Test fun emptyArticlesAreValidSuccess() = runBlocking {
        respond("""{"errorCode":0,"data":{"curPage":1,"over":true,"datas":[]}}""")
        val result = repository.firstPage() as NetResult.Success
        assertTrue(result.value.articles.isEmpty())
    }

    @Test fun ioFailureBecomesNetworkError() = runBlocking {
        val repository = ArticleRepository(failingApi(IOException("offline")))
        assertEquals(NetError.Network, (repository.firstPage() as NetResult.Failure).error)
    }

    @Test fun cancellationIsRethrown() = runBlocking {
        val cancellation = CancellationException("leave screen")
        try {
            ArticleRepository(failingApi(cancellation)).firstPage()
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) { assertSame(cancellation, actual) }
    }

    private fun failingApi(failure: Exception) = object : WanApi {
        override suspend fun articles(page: Int): BaseResponse<ArticlePageDto> = throw failure
    }
}
