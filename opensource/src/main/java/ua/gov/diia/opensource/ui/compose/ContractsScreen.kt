package ua.gov.diia.opensource.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.components.theme.Alabaster
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.BlackAlpha10
import ua.gov.diia.ui_base.components.theme.BlackAlpha20
import ua.gov.diia.ui_base.components.theme.BlackAlpha54
import ua.gov.diia.ui_base.components.theme.BlackAlpha80
import ua.gov.diia.ui_base.components.theme.DarkGreen
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.WarningYellow
import ua.gov.diia.ui_base.components.theme.White
import ua.gov.diia.ui_base.components.theme.WhiteAlpha20
import ua.gov.diia.ui_base.components.theme.WhiteAlpha70
import ua.gov.diia.ui_base.R as UiBaseR

@Composable
fun ContractsScreen(
    contracts: List<ContractUiModel> = emptyList(),
    onBackClick: () -> Unit,
    onContractClick: (ContractUiModel) -> Unit,
    onCreateContractClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ContractsFilter.ALL) }
    val activeCount = contracts.count { it.status == ContractStatus.ACTIVE }
    val pendingCount = contracts.count { it.status == ContractStatus.PENDING_SIGNATURE }
    val draftCount = contracts.count { it.status == ContractStatus.DRAFT }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { ContractsCreateBar(onCreateContractClick) }
    ) { innerPadding ->
        val bottomPadding = innerPadding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Alabaster)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ContractsHeader(
                    onBackClick = onBackClick,
                    activeCount = activeCount,
                    pendingCount = pendingCount,
                    draftCount = draftCount
                )

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = White,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        FiltersRow(
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )

                        AnimatedContent(
                            targetState = selectedFilter,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 70))
                                    .togetherWith(fadeOut(animationSpec = tween(durationMillis = 180)))
                                    .using(SizeTransform(clip = false))
                            },
                            label = "ContractsFilterContent"
                        ) { filter ->
                            val filteredContracts = contracts.filter { it.matches(filter) }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = 8.dp,
                                    bottom = bottomPadding + 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredContracts, key = { it.id }) { contract ->
                                    ContractCard(
                                        contract = contract,
                                        onClick = { onContractClick(contract) }
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

@Composable
private fun ContractsHeader(
    onBackClick: () -> Unit,
    activeCount: Int,
    pendingCount: Int,
    draftCount: Int
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.contracts_title),
                    style = DiiaTextStyle.heroText,
                    color = Black
                )
                Text(
                    text = stringResource(id = R.string.contracts_subtitle),
                    style = DiiaTextStyle.t1BigText,
                    color = BlackAlpha54
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBadge(label = stringResource(id = R.string.contract_status_active), value = activeCount)
                StatBadge(label = stringResource(id = R.string.contract_status_pending), value = pendingCount)
                StatBadge(label = stringResource(id = R.string.contract_status_draft), value = draftCount)
            }
        }
    }
}

@Composable
private fun RowScope.StatBadge(label: String, value: Int) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .border(1.dp, BlackAlpha10, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = value.toString(),
            style = DiiaTextStyle.h2MediumHeading.copy(fontWeight = FontWeight.Bold),
            color = Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = DiiaTextStyle.t3TextBody,
            color = BlackAlpha54
        )
    }
}

private enum class ContractsFilter { ALL, ACTIVE, PENDING, DRAFT }

@Composable
private fun FiltersRow(
    selectedFilter: ContractsFilter,
    onFilterSelected: (ContractsFilter) -> Unit
) {
    val filters = listOf(
        ContractsFilter.ALL,
        ContractsFilter.ACTIVE,
        ContractsFilter.PENDING,
        ContractsFilter.DRAFT
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
                ContractsFilter.ALL -> stringResource(id = R.string.contracts_filter_all)
                ContractsFilter.ACTIVE -> stringResource(id = R.string.contracts_filter_active)
                ContractsFilter.PENDING -> stringResource(id = R.string.contracts_filter_pending)
                ContractsFilter.DRAFT -> stringResource(id = R.string.contracts_filter_draft)
            }
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = label,
                        style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    labelColor = BlackAlpha80,
                    selectedContainerColor = Black,
                    selectedLabelColor = White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BlackAlpha20,
                    borderWidth = 1.dp,
                    selectedBorderColor = Black,
                    selectedBorderWidth = 0.dp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContractCard(
    contract: ContractUiModel,
    onClick: () -> Unit
) {
    val statusStyle = contract.status.asBadgeStyle()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(18.dp), clip = true)
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = White,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlackAlpha10),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = contract.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = contract.title,
                        style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.SemiBold),
                        color = Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contract.subtitle,
                        style = DiiaTextStyle.t3TextBody,
                        color = BlackAlpha54,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    painter = painterResource(id = UiBaseR.drawable.ic_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
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
private fun StatusBadge(style: ContractStatusStyle) {
    Row(
        modifier = Modifier
            .background(style.containerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(style.contentColor)
        )
        Text(
            text = style.label,
            style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.SemiBold),
            color = style.contentColor
        )
    }
}

@Composable
private fun MetaBadge(
    text: String,
    iconRes: Int? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    val bgColor = backgroundColor ?: BlackAlpha10
    val contentColor = textColor ?: BlackAlpha80
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
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
private fun ContractsCreateBar(
    onCreateContractClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Black)
                .clickable(onClick = onCreateContractClick)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.contracts_action_create),
                style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                color = White
            )
        }
    }
}

data class ContractUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: ContractStatus,
    val lastUpdated: String,
    val iconRes: Int,
    val isFilled: Boolean,
    val isSigned: Boolean,
    val history: List<HistoryUiModel>
)

enum class ContractStatus {
    ACTIVE,
    PENDING_SIGNATURE,
    DRAFT
}

data class HistoryUiModel(
    val date: String,
    val description: String
)

data class ContractStatusStyle(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val darkContainerColor: Color,
    val darkContentColor: Color
)

@Composable
fun ContractStatus.asBadgeStyle(): ContractStatusStyle {
    val label = when (this) {
        ContractStatus.ACTIVE -> stringResource(id = R.string.contract_status_active)
        ContractStatus.PENDING_SIGNATURE -> stringResource(id = R.string.contract_status_pending)
        ContractStatus.DRAFT -> stringResource(id = R.string.contract_status_draft)
    }
    return when (this) {
        ContractStatus.ACTIVE -> ContractStatusStyle(
            label = label,
            containerColor = DarkGreen.copy(alpha = 0.12f),
            contentColor = DarkGreen,
            darkContainerColor = DarkGreen.copy(alpha = 0.24f),
            darkContentColor = White
        )
        ContractStatus.PENDING_SIGNATURE -> ContractStatusStyle(
            label = label,
            containerColor = WarningYellow.copy(alpha = 0.3f),
            contentColor = Black,
            darkContainerColor = WarningYellow.copy(alpha = 0.4f),
            darkContentColor = Black
        )
        ContractStatus.DRAFT -> ContractStatusStyle(
            label = label,
            containerColor = BlackAlpha10,
            contentColor = BlackAlpha80,
            darkContainerColor = WhiteAlpha20,
            darkContentColor = White
        )
    }
}

private fun ContractUiModel.matches(filter: ContractsFilter): Boolean =
    when (filter) {
        ContractsFilter.ALL -> true
        ContractsFilter.ACTIVE -> status == ContractStatus.ACTIVE
        ContractsFilter.PENDING -> status == ContractStatus.PENDING_SIGNATURE
        ContractsFilter.DRAFT -> status == ContractStatus.DRAFT
    }
