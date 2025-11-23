package ua.gov.diia.opensource.ui.compose

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.R as UiBaseR
import ua.gov.diia.ui_base.components.theme.Alabaster
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.BlackAlpha54
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.White

@Composable
fun ContractPreviewScreen(
    title: String,
    html: String?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                tonalElevation = 0.dp,
                color = White
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                            contentDescription = null,
                            tint = Black
                        )
                    }
                    Text(
                        text = title,
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                        color = Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Alabaster),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = Black)
                }

                html != null -> {
                    val context = LocalContext.current
                    AndroidView(
                        factory = {
                            WebView(context).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                settings.defaultTextEncodingName = "UTF-8"
                                settings.javaScriptEnabled = false
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                null,
                                html,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.contracts_preview_error),
                            style = DiiaTextStyle.t1BigText,
                            color = BlackAlpha54
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Black,
                                contentColor = White
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.contracts_preview_retry),
                                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}
