package ua.gov.diia.ui_base.components.molecule.message

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.gov.diia.core.models.common.message.AttentionIconMessageMlc
import ua.gov.diia.ui_base.R
import ua.gov.diia.ui_base.components.DiiaResourceIcon
import ua.gov.diia.ui_base.components.atom.icon.SmallIconAtm
import ua.gov.diia.ui_base.components.atom.icon.SmallIconAtmData
import ua.gov.diia.ui_base.components.atom.icon.toUiModel
import ua.gov.diia.ui_base.components.atom.text.textwithparameter.TextParameter
import ua.gov.diia.ui_base.components.atom.text.textwithparameter.TextWithParametersAtom
import ua.gov.diia.ui_base.components.atom.text.textwithparameter.TextWithParametersData
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.components.infrastructure.utils.SidePaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.TopPaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.toDynamicStringOrNull
import ua.gov.diia.ui_base.components.infrastructure.utils.toDp
import ua.gov.diia.ui_base.components.infrastructure.utils.toSidePaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.toTopPaddingMode
import ua.gov.diia.ui_base.components.molecule.list.table.items.tableblock.TableBlockItem
import ua.gov.diia.ui_base.components.noRippleClickable
import ua.gov.diia.ui_base.components.organism.list.pagination.SimplePagination
import ua.gov.diia.ui_base.components.theme.AzureRadiance16
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.InfoYellow
import ua.gov.diia.ui_base.components.theme.gradientBluePosition01
import ua.gov.diia.ui_base.components.theme.gradientBluePosition02

@Composable
fun AttentionIconMessageMlc(
    modifier: Modifier = Modifier,
    data: AttentionIconMessageMlcData,
    onUIAction: (UIAction) -> Unit
) {
    val isExpanded = remember { mutableStateOf(data.isExpanded) }
    val isVisible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible.value = true
    }

    val shape = RoundedCornerShape(16.dp)
    val baseModifier = modifier
        .padding(
            start = data.paddingHorizontal.toDp(defaultPadding = 24.dp),
            top = data.paddingTop.toDp(defaultPadding = 16.dp),
            end = data.paddingHorizontal.toDp(defaultPadding = 24.dp)
        )
        .fillMaxWidth()

    val boxModifier = when (data.backgroundMode) {
        BackgroundMode.INFO -> {
            baseModifier.background(
                color = InfoYellow,
                shape = shape
            )
        }

        BackgroundMode.NOTE -> {
            baseModifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        gradientBluePosition01,
                        gradientBluePosition02
                    )
                ),
                shape = shape
            )
        }
    }

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> fullHeight / 4 }),
        exit = fadeOut()
    ) {
        Box(
            modifier = boxModifier
                .testTag(data.componentId?.asString() ?: "")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.wrapContentWidth()) {
                    data.icon?.let {
                        SmallIconAtm(
                            modifier = Modifier
                                .semantics {
                                    if (it.accessibilityDescription == null) {
                                        hideFromAccessibility()
                                    }
                                },
                            data = it,
                            onUIAction = onUIAction
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    data.text?.let {
                        TextWithParametersAtom(
                            modifier = Modifier,
                            data = TextWithParametersData(
                                actionKey = UIActionKeysCompose.TEXT_WITH_PARAMETERS,
                                text = data.text,
                                parameters = data.parameters
                            ),
                            style = DiiaTextStyle.t2TextDescription,
                            onUIAction = onUIAction,
                            maxLines = if (isExpanded.value == false) 3 else Int.MAX_VALUE
                        )
                    }

                    if (data.hasExpandedData == true) {
                        Row(modifier = Modifier.padding(top = 16.dp)) {
                            val text = if (isExpanded.value == true) {
                                data.collapsedText
                            } else {
                                data.expandedText
                            }
                            Text(
                                modifier = Modifier.noRippleClickable {
                                    isExpanded.value = !(isExpanded.value ?: false)
                                },
                                text = text?.asString() ?: "",
                                style = DiiaTextStyle.t3TextBody,
                            )

                            Spacer(modifier = Modifier.size(4.dp))
                            Icon(
                                modifier = Modifier
                                    .padding(start = 4.dp, top = 4.dp)
                                    .size(8.dp, 8.dp),
                                painter = painterResource(
                                    id = if (isExpanded.value == true) {
                                        R.drawable.ic_arrow_show_less
                                    } else {
                                        R.drawable.ic_arrow_show_more
                                    }
                                ),
                                contentDescription = stringResource(R.string.details),
                                tint = Black
                            )
                        }
                    }
                }
            }
        }
    }
}


data class AttentionIconMessageMlcData(
    override val id: String = "",
    val componentId: UiText? = null,
    val icon: SmallIconAtmData?,
    val text: UiText?,
    val parameters: List<TextParameter>? = null,
    val backgroundMode: BackgroundMode,
    val hasExpandedData: Boolean? = null,
    val isExpanded: Boolean? = null,
    val expandedText: UiText? = null,
    val collapsedText: UiText? = null,
    val paddingTop: TopPaddingMode? = null,
    val paddingHorizontal: SidePaddingMode? = null
) : UIElementData, TableBlockItem, SimplePagination

enum class BackgroundMode {
    INFO, NOTE;
}

fun AttentionIconMessageMlc.toUiModel(): AttentionIconMessageMlcData {
    return AttentionIconMessageMlcData(
        componentId = this.componentId.toDynamicStringOrNull(),
        id = this.componentId.orEmpty(),
        icon = this.smallIconAtm?.toUiModel(),
        text = this.text?.let {
            UiText.DynamicString(it)
        },
        backgroundMode = when (this.backgroundMode) {
            AttentionIconMessageMlc.BackgroundMode.info -> BackgroundMode.INFO
            AttentionIconMessageMlc.BackgroundMode.note -> BackgroundMode.NOTE
        },
        paddingTop = paddingMode?.top.toTopPaddingMode(),
        paddingHorizontal = paddingMode?.side.toSidePaddingMode(),
        parameters = if (this.parameters != null) {
            mutableListOf<TextParameter>().apply {
                parameters?.forEach {
                    add(
                        TextParameter(
                            data = TextParameter.Data(
                                name = it.data?.name.toDynamicStringOrNull(),
                                resource = it.data?.resource.toDynamicStringOrNull(),
                                alt = it.data?.alt.toDynamicStringOrNull()
                            ),
                            type = it.type
                        )
                    )
                }
            }
        } else emptyList(),
        hasExpandedData = this.expanded != null,
        isExpanded = this.expanded?.isExpanded ?: false,
        expandedText = this.expanded?.expandedText?.let { UiText.DynamicString(it) },
        collapsedText = this.expanded?.collapsedText?.let { UiText.DynamicString(it) },
    )
}

fun generateAttentionIconMessageMlcMockData(): AttentionIconMessageMlcData {
    return AttentionIconMessageMlcData(
        icon = SmallIconAtmData(code = DiiaResourceIcon.ELLIPSE_INFO.code),
        text = UiText.DynamicString("Щоб надіслати заяву, потрібно вказати всі дані."),
        backgroundMode = BackgroundMode.INFO
    )
}

fun generateAttentionIconMessageMlcExpandedMockData(): AttentionIconMessageMlcData {
    return AttentionIconMessageMlcData(
        icon = SmallIconAtmData(code = DiiaResourceIcon.ELLIPSE_INFO.code),
        text = UiText.DynamicString("Try not to write more than 10 lines of message in this component. Even if we have enough space, users have difficulty perceiving large amounts of information."),
        backgroundMode = BackgroundMode.NOTE,
        isExpanded = false,
        hasExpandedData = true,
        expandedText = UiText.DynamicString("Show more"),
        collapsedText = UiText.DynamicString("Show less"),
    )
}

fun generateAttentionIconMessageMlcMockDataCollapsedMockData(): AttentionIconMessageMlcData {
    return AttentionIconMessageMlcData(
        icon = SmallIconAtmData(code = DiiaResourceIcon.ELLIPSE_INFO.code),
        text = UiText.DynamicString("Try not to write more than 10 lines of message in this component. Even if we have enough space, users have difficulty perceiving large amounts of information."),
        backgroundMode = BackgroundMode.NOTE,
        isExpanded = true,
        hasExpandedData = true,
        expandedText = UiText.DynamicString("Show more"),
        collapsedText = UiText.DynamicString("Show less"),
    )
}

fun generateAttentionIconMessageMlcMockDataBgNote(): AttentionIconMessageMlcData {
    return AttentionIconMessageMlcData(
        icon = SmallIconAtmData(code = DiiaResourceIcon.ELLIPSE_INFO.code),
        text = UiText.DynamicString("Щоб надіслати заяву, потрібно вказати всі дані."),
        backgroundMode = BackgroundMode.NOTE
    )
}

@Composable
@Preview
fun AttentionIconMessageMlcPreview() {
    AttentionIconMessageMlc(
        data = generateAttentionIconMessageMlcMockData()
    ) {}
}

@Composable
@Preview
fun AttentionIconMessageMlcPreview_NOTE() {
    AttentionIconMessageMlc(data = generateAttentionIconMessageMlcMockDataBgNote()) {}
}

@Composable
@Preview
fun AttentionIconMessageMlcPreview_expanded() {
    AttentionIconMessageMlc(data = generateAttentionIconMessageMlcExpandedMockData()) {}
}

@Composable
@Preview
fun AttentionIconMessageMlcPreview_collapsed() {
    AttentionIconMessageMlc(data = generateAttentionIconMessageMlcMockDataCollapsedMockData()) {}
}
