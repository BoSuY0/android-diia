package ua.gov.diia.auth_bankid.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ua.gov.diia.auth_bankid.ui.auth.BankAuthConst.PROGRESS_ACTIVE
import ua.gov.diia.auth_bankid.ui.auth.BankAuthConst.PROGRESS_INACTIVE
import ua.gov.diia.core.models.ConsumableItem
import ua.gov.diia.core.util.delegation.WithBuildConfig
import ua.gov.diia.core.util.extensions.fragment.hideKeyboard
import ua.gov.diia.core.util.extensions.fragment.setNavigationResult
import ua.gov.diia.ui_base.components.infrastructure.collectAsEffect
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.navigation.BaseNavigation
import ua.gov.diia.verification.model.VerificationFlowResult
import javax.inject.Inject

@AndroidEntryPoint
internal class BankAuthF : Fragment() {

    private var composeView: ComposeView? = null
    private val viewModel: BankAuthVM by viewModels()
    private val args: BankAuthFArgs by navArgs()

    @Inject
    lateinit var withBuildConfig: WithBuildConfig

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // In mock debug build we skip real BankID web flow
        // and immediately complete verification with mock data.
        if (isMockMode()) {
            val result = VerificationFlowResult.CompleteVerificationStep(
                requestId = "mock-request-id",
                bankCode = args.requestData.bankCode
            )
            completeAuth(result)
            return null
        }

        composeView = ComposeView(requireContext())
        return composeView
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView?.setContent {
            with(viewModel) {
                navigation.collectAsEffect { navigation ->
                    when (navigation) {
                        is BankAuthVM.Navigation.CompleteAuth -> {
                            completeAuth(navigation.data)
                        }

                        is BaseNavigation.Back -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }

            val data = viewModel.uiData.value

            BankAuthScreen(
                data = data,
                configureWebView = { webView ->
                    webView.webViewClient = bankIdWebClient()
                    with(webView.settings) {
                        allowFileAccess = false
                        allowFileAccessFromFileURLs = false
                        allowUniversalAccessFromFileURLs = false
                        allowContentAccess = false
                        domStorageEnabled = true
                        javaScriptEnabled = true
                    }
                },
                onUIAction = {
                    viewModel.onUIAction(it)
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }

    private fun bankIdWebClient() = object : WebViewClient() {

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
            if (view != null && url != null) {
                handleUrlOverride(view, url)
            } else {
                false
            }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString()
            return if (view != null && url != null) {
                handleUrlOverride(view, url)
            } else {
                false
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            viewModel.onUIAction(UIAction(PROGRESS_ACTIVE))
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            viewModel.onUIAction(UIAction(PROGRESS_INACTIVE))
        }
    }

    private fun handleUrlOverride(view: WebView, url: String): Boolean {
        when {
            url.startsWith(withBuildConfig.getBankIdCallbackUrl()) -> {
                viewModel.parseAuthCode(url)
                return true
            }

            url.startsWith(HTTP) || url.startsWith(HTTPS) -> {
                view.loadUrl(url, getCustomHeaders())
                return true
            }

            url.startsWith(APP) || url.startsWith(MARKET) -> {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                val canHandle = intent.resolveActivity(requireContext().packageManager) != null

                if (url.startsWith(APP) && canHandle) {
                    startActivity(intent)
                    return true
                }

                if (url.startsWith(MARKET) && canHandle) {
                    startActivity(intent)
                    return true
                }

                return true
            }

            url.startsWith(INTENT) -> {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                val canHandle = intent.resolveActivity(requireContext().packageManager) != null

                if (canHandle) {
                    startActivity(intent)
                } else {
                    val fallbackUrl = intent.getStringExtra(BROWSER_FALLBACK_URL_KEY)
                    if (!fallbackUrl.isNullOrEmpty()) {
                        view.loadUrl(fallbackUrl, getCustomHeaders())
                    }
                }

                return true
            }

            else -> {
                // Handled next links/uris -> bankIdCallbackUrl, http, https, app, market, intent
                return true
            }
        }
    }

    private fun getCustomHeaders(): MutableMap<String, String> {
        val headers: MutableMap<String, String> = HashMap()
        headers[X_DII_HEADER] = "Android:${withBuildConfig.getVersionName()}"
        return headers
    }

    private fun completeAuth(result: VerificationFlowResult) {
        setNavigationResult(
            arbitraryDestination = args.resultDestination,
            key = args.resultKey,
            data = ConsumableItem(result)
        )
        findNavController().popBackStack(args.resultDestination, false)
    }

    private fun isMockMode(): Boolean {
        val buildType = withBuildConfig.getBuildType()
        val versionName = withBuildConfig.getVersionName()
        return buildType.contains("mock", ignoreCase = true) ||
            buildType.contains("hackathon", ignoreCase = true) ||
            buildType.contains("debug", ignoreCase = true) ||
            versionName.contains("mock", ignoreCase = true) ||
            versionName.contains("hackathon", ignoreCase = true)
    }

    private companion object {
        const val X_DII_HEADER = "x-diia-version"
        const val HTTP = "http"
        const val HTTPS = "https"
        const val APP = "app"
        const val MARKET = "market"
        const val INTENT = "intent"
        const val BROWSER_FALLBACK_URL_KEY = "browser_fallback_url"
    }

}
