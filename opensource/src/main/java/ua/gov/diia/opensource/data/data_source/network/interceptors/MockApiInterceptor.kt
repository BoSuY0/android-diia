package ua.gov.diia.opensource.data.data_source.network.interceptors

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import ua.gov.diia.core.models.common_compose.general.DiiaResponse
import ua.gov.diia.opensource.BuildConfig
import ua.gov.diia.opensource.mock.MockBackendData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockApiInterceptor @Inject constructor(
    @ApplicationContext @Suppress("UNUSED_PARAMETER") private val context: Context
) : Interceptor {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val path = url.encodedPath
        val host = url.host
        val contractsHost = BuildConfig.CONTRACTS_BASE_URL.toHttpUrlOrNull()?.host

        val isContractsRequest = BuildConfig.CONTRACTS_BACKEND_ENABLED && when {
            contractsHost != null && contractsHost.equals(host, ignoreCase = true) -> true
            path.startsWith("/categories") -> true
            path.startsWith("/sessions") -> true
            path.startsWith("/chat") -> true
            path.startsWith("/user-documents") -> true
            else -> false
        }

        if (!BuildConfig.MOCK_MODE || isContractsRequest) {
            return chain.proceed(request)
        }

        val method = request.method.uppercase()

        val body: String? = when {
            // ----- Settings / app config -----
            method == "GET" && path == "/api/v1/settings" -> {
                encode(MockBackendData.appSettingsInfo())
            }

            // ----- Notifications (fire-and-forget) -----
            method == "POST" && path == "/api/v1/notification/app-version" -> {
                "{}"
            }

            method == "POST" && path == "/api/v1/notification/user-push-token" -> {
                "{}"
            }

            method == "POST" && path == "/api/v1/analytics/app-status" -> {
                "{}"
            }

            // ----- Auth / login -----
            method == "GET" && path == "/api/v1/auth/photoid/fld" -> {
                val isLowRam = url.queryParameter("isLowRamDevice")?.toBoolean() ?: false
                encode(MockBackendData.faceRecoConfig(isLowRam))
            }

            method == "POST" && path.startsWith("/api/v1/auth/test/") && path.endsWith("/token") -> {
                val requestId = path.removePrefix("/api/v1/auth/test/").removeSuffix("/token")
                // For simplicity userInfo is ignored here.
                encode(MockBackendData.testToken(requestId, emptyMap()))
            }

            method == "POST" && path == "/api/v2/auth/token/refresh" -> {
                encode(MockBackendData.refreshToken(null))
            }

            method == "GET" && path == "/api/v3/auth/token" -> {
                val processId = url.queryParameter("processId") ?: "mock-process-1"
                encode(MockBackendData.loginToken(processId))
            }

            method == "GET" && path == "/api/v1/auth/otp/screen" -> {
                val processId = url.queryParameter("processId") ?: "mock-process-1"
                val nfcAvailable = url.queryParameter("nfcAvailable")?.toBoolean() ?: false
                encode(MockBackendData.authenticationOtpScreen(processId, nfcAvailable))
            }

            method == "GET" && path.startsWith("/api/v1/auth/acquirer/branch/offer/") && path.endsWith("/token") -> {
                val uuid = path
                    .removePrefix("/api/v1/auth/acquirer/branch/offer/")
                    .removeSuffix("/token")
                val mobileUid = request.header("mobile_uid") ?: "mock-mobile-uid"
                encode(MockBackendData.serviceAccountToken(uuid, mobileUid))
            }

            method == "POST" && path == "/api/v2/auth/token/logout" -> {
                "{}"
            }

            method == "POST" && path == "/api/v1/auth/acquirer/branch/offer/token/logout" -> {
                "{}"
            }

            // ----- Verification -----
            method == "GET" && path == "/api/v3/auth/authorization/methods" -> {
                encode(
                    MockBackendData.verificationMethodsAuthorization(
                        processId = url.queryParameter("processId"),
                        selectedMethod = url.queryParameter("selectedMethod")
                    )
                )
            }

            method == "GET" && path.startsWith("/api/v3/auth/") && path.endsWith("/auth-url") -> {
                val segments = path.removePrefix("/api/v3/auth/").removeSuffix("/auth-url")
                val methodCode = segments
                val processId = url.queryParameter("processId") ?: "mock-process-1"
                val bankCode = url.queryParameter("bankId")
                encode(MockBackendData.verificationUrl(methodCode, processId, bankCode))
            }

            method == "GET" && path.startsWith("/api/v1/auth/") && path.contains("/verify") -> {
                val withoutPrefix = path.removePrefix("/api/v1/auth/")
                val methodAndRest = withoutPrefix.split('/', limit = 2)
                val methodCode = methodAndRest.getOrNull(0) ?: "unknown"
                val rest = methodAndRest.getOrNull(1) ?: ""
                val requestId = rest.removeSuffix("/verify")
                val processId = url.queryParameter("processId") ?: "mock-process-1"
                val bankCode = url.queryParameter("bankId")
                encode(
                    MockBackendData.completeVerificationStep(
                        methodCode = methodCode,
                        requestId = requestId,
                        processId = processId,
                        bankCode = bankCode
                    )
                )
            }

            // ----- Documents -----
            method == "GET" && path == "/api/v6/documents" -> {
                encode(MockBackendData.docs())
            }

            method == "GET" && path == "/api/v1/documents/manual" -> {
                encode(MockBackendData.docsManual())
            }

            method == "GET" && path == "/api/v1/documents/itn" -> {
                encode(MockBackendData.itn())
            }

            method == "GET" && path.startsWith("/api/v1/documents/") && path.endsWith("/share") -> {
                val parts = path.removePrefix("/api/v1/documents/").split('/')
                val docName = parts.getOrNull(0)
                val documentId = parts.getOrNull(1)
                encode(MockBackendData.shareUrl(docName, documentId))
            }

            method == "GET" && path.startsWith("/api/v2/documents/") && path.endsWith("/share") -> {
                val parts = path.removePrefix("/api/v2/documents/").split('/')
                val documentType = parts.getOrNull(0)
                val documentId = parts.getOrNull(1)
                val localization = url.queryParameter("localization") ?: "UA"
                encode(
                    MockBackendData.verificationCodesOrg(
                        documentType = documentType,
                        documentId = documentId,
                        localization = localization
                    )
                )
            }

            // ----- Feed -----
            method == "GET" && path == "/api/v1/feed" -> {
                encode(MockBackendData.feedScreen())
            }

            method == "GET" && path == "/api/v1/feed/news" && url.queryParameter("id") == null -> {
                encode(MockBackendData.newsList())
            }

            method == "GET" && path == "/api/v1/feed/news" && url.queryParameter("id") != null -> {
                encode(MockBackendData.newsDetails())
            }

            method == "GET" && path == "/api/v1/feed/news/screen" -> {
                encode(MockBackendData.feedScreen())
            }

            method == "GET" && path == "/api/v1/public-service/enemy-track/link" -> {
                encode(MockBackendData.enemyShareLink())
            }

            // ----- Public services -----
            method == "GET" && path == "/api/v3/public-service/catalog" -> {
                encode(MockBackendData.publicServices())
            }

            method == "GET" && path == "/api/v1/public-service/promo" -> {
                encode(MockBackendData.checkPromo())
            }

            method == "POST" && path == "/api/v1/user/subscription/public-service" -> {
                encode(MockBackendData.subscribeToBeta(url.queryParameter("segmentId")?.toIntOrNull()))
            }

            method == "GET" && path.startsWith("/api/v1/public-service/") && path.endsWith("/portal") -> {
                val serviceCode = path.removePrefix("/api/v1/public-service/").removeSuffix("/portal")
                encode(MockBackendData.publicServicePortalUrl(serviceCode))
            }

            // ----- BankID (banks list) -----
            method == "GET" && path == "/api/v1/auth/banks" -> {
                encode(MockBackendData.authBanks())
            }

            else -> null
        }

        val responseBody = body ?: encodeEmpty()
        val statusCode = if (body != null) 200 else 501
        val message = if (body != null) "OK (MOCK)" else "No mock for $path"

        return buildResponse(request, statusCode, message, responseBody)
    }

    private fun encodeEmpty(): String = encode(
        DiiaResponse(
            topGroup = null,
            body = emptyList(),
            centeredBody = null,
            bottomGroup = null,
            processCode = null,
            template = null,
            ratingForm = null,
            nextStep = null,
            code = null
        )
    )

    private fun buildResponse(
        request: Request,
        code: Int,
        message: String,
        body: String
    ): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private inline fun <reified T> encode(value: T): String {
        return moshi.adapter(T::class.java).toJson(value)
    }
}
