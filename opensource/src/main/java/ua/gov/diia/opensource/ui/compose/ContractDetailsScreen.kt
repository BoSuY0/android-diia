package ua.gov.diia.opensource.ui.compose

import android.widget.Toast

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.components.theme.Alabaster
import ua.gov.diia.ui_base.components.theme.AzureRadiance
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.BlackAlpha10
import ua.gov.diia.ui_base.components.theme.BlackAlpha54
import ua.gov.diia.ui_base.components.theme.BlackAlpha80
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.White
import ua.gov.diia.ui_base.components.theme.WhiteAlpha20
import ua.gov.diia.ui_base.components.theme.WhiteAlpha70
import ua.gov.diia.ui_base.R as UiBaseR

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContractDetailsScreen(
    contract: ContractUiModel,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSignClick: () -> Unit,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val statusStyle = contract.status.asBadgeStyle()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            ContractDetailsActions(
                isSigned = contract.isSigned,
                isFilled = contract.isFilled,
                onViewClick = onViewClick,
                onDownloadClick = onDownloadClick,
                onSignClick = onSignClick,
                onEditClick = onEditClick,
                onShareClick = onShareClick
            )
        }
    ) { innerPadding ->
        val bottomPadding = innerPadding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Alabaster)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DetailsHeader(
                    contract = contract,
                    statusStyle = statusStyle,
                    onBackClick = onBackClick
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = White,
                    tonalElevation = 0.dp
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = bottomPadding + 16.dp
                    )
                ) {
                    if (contract.history.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.contracts_details_history_title),
                                style = DiiaTextStyle.h3SmallHeading,
                                color = Black
                            )
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 0.dp,
                                shadowElevation = 6.dp,
                                color = White
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    contract.history.forEachIndexed { index, history ->
                                        HistoryItem(
                                            history = history,
                                            isLast = index == contract.history.lastIndex
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun DetailsHeader(
    contract: ContractUiModel,
    statusStyle: ContractStatusStyle,
    onBackClick: () -> Unit
) {
    val headerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = headerShape,
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5F7FA), Color(0xFFFFFFFF))
                    )
                )
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(BlackAlpha10, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.contracts_title),
                    style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.SemiBold),
                    color = Black
                )
            }

            Text(
                text = contract.title,
                style = DiiaTextStyle.h1Heading.copy(fontWeight = FontWeight.Bold),
                color = Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = contract.subtitle,
                style = DiiaTextStyle.t1BigText,
                color = BlackAlpha54,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusBadge(style = statusStyle, dark = false)
                MetaBadge(
                    text = stringResource(id = R.string.contracts_updated_at, contract.lastUpdated),
                    iconRes = UiBaseR.drawable.ic_time_black_square,
                    dark = false
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ContractHeroCard(
    contract: ContractUiModel,
    statusStyle: ContractStatusStyle
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        color = White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Alabaster),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = contract.iconRes),
                        contentDescription = null,
                        tint = Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = contract.title,
                        style = DiiaTextStyle.h2MediumHeading,
                        color = Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contract.subtitle,
                        style = DiiaTextStyle.t3TextBody,
                        color = BlackAlpha54,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(style = statusStyle)
                MetaBadge(
                    text = stringResource(id = R.string.contracts_updated_at, contract.lastUpdated),
                    iconRes = UiBaseR.drawable.ic_time_black_square
                )
                when {
                    contract.isSigned -> MetaBadge(
                        text = stringResource(id = R.string.contracts_meta_signed),
                        backgroundColor = Color(0xFFFFF3CD), // Світло-жовтий фон
                        textColor = Color(0xFF856404) // Темно-жовтий текст
                    )
                    !contract.isFilled -> MetaBadge(text = stringResource(id = R.string.contracts_meta_draft))
                }
            }
        }
    }
}

@Composable
fun ShareContractLinkDialog(
    link: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val copyLinkAndClose: () -> Unit = {
        clipboardManager.setText(AnnotatedString(link))
        Toast.makeText(
            context,
            context.getString(R.string.contracts_share_copied),
            Toast.LENGTH_SHORT
        ).show()
        // Закриваємо діалог одразу — Toast показується асинхронно
        onDismiss()
    }
    
    val copyLinkOnly: () -> Unit = {
        clipboardManager.setText(AnnotatedString(link))
        Toast.makeText(
            context,
            context.getString(R.string.contracts_share_copied),
            Toast.LENGTH_SHORT
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = White,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, BlackAlpha10),
            modifier = Modifier.widthIn(max = 400.dp) // Обмеження ширини для планшетів
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.contracts_share_title),
                    style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.SemiBold),
                    color = Black
                )
                Text(
                    text = stringResource(id = R.string.contracts_share_description),
                    style = DiiaTextStyle.t3TextBody,
                    color = BlackAlpha80
                )
                OutlinedTextField(
                    value = link,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.contracts_share_link_label)) },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = copyLinkOnly) {
                            Icon(
                                painter = painterResource(id = UiBaseR.drawable.ic_copy),
                                contentDescription = stringResource(id = UiBaseR.string.copy_to_clipboard),
                                tint = Black
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Black,
                        unfocusedBorderColor = BlackAlpha10,
                        focusedLabelColor = Black,
                        unfocusedLabelColor = BlackAlpha54,
                        disabledBorderColor = BlackAlpha10
                    )
                )
                Button(
                    onClick = copyLinkAndClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Black,
                        contentColor = White
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = UiBaseR.string.copy_to_clipboard),
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Black),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Black),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = UiBaseR.string.close),
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(style: ContractStatusStyle) {
    StatusBadge(style = style, dark = false)
}

@Composable
private fun StatusBadge(style: ContractStatusStyle, dark: Boolean) {
    Row(
        modifier = Modifier
            .background(
                if (dark) style.darkContainerColor else style.containerColor,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (dark) style.darkContentColor else style.contentColor)
        )
        Text(
            text = style.label,
            style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.SemiBold),
            color = if (dark) style.darkContentColor else style.contentColor
        )
    }
}

@Composable
private fun MetaBadge(
    text: String,
    iconRes: Int? = null,
    dark: Boolean = false,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    val background = backgroundColor ?: if (dark) WhiteAlpha20 else BlackAlpha10
    val contentColor = textColor ?: if (dark) White else BlackAlpha80

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        iconRes?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
        }
        Text(
            text = text,
            style = DiiaTextStyle.t3TextBody,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryItem(
    history: HistoryUiModel,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(AzureRadiance)
            )
            if (!isLast) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(AzureRadiance.copy(alpha = 0.25f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .padding(bottom = if (isLast) 0.dp else 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = history.description,
                style = DiiaTextStyle.t1BigText,
                color = Black
            )
            Text(
                text = history.date,
                style = DiiaTextStyle.t2TextDescription,
                color = BlackAlpha54
            )
        }
    }
}

@Composable
private fun ContractDetailsActions(
    isSigned: Boolean,
    isFilled: Boolean,
    onViewClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSignClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onViewClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black,
                    contentColor = White
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.contracts_details_primary_action),
                    style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            when {
                isSigned -> {
                    OutlinedButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Black),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Black
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.contracts_details_download),
                            style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                isFilled -> {
                    OutlinedButton(
                        onClick = onSignClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Black),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Black
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.contracts_details_sign),
                            style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            if (!isSigned) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Black
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.contracts_details_edit),
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Black
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.contracts_details_share),
                    style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
