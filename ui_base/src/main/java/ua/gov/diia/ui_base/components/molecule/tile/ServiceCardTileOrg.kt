package ua.gov.diia.ui_base.components.molecule.tile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dagger.hilt.EntryPoints
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.utils.TopPaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.infrastructure.utils.toDp
import ua.gov.diia.ui_base.components.molecule.card.ServiceCardMlc
import ua.gov.diia.ui_base.components.molecule.card.ServiceCardMlcData
import ua.gov.diia.ui_base.components.molecule.card.ServiceChipCardMlc
import ua.gov.diia.ui_base.components.molecule.card.ServiceChipCardMlcData
import ua.gov.diia.ui_base.components.theme.WhiteAlpha40
import ua.gov.diia.ui_base.di.ServiceCardResourceEntryPointModule

private lateinit var serviceCardResourceEntryPoint: ServiceCardResourceEntryPointModule.ServiceCardResourceEntryPoint

@Composable
fun requireServiceCardResourceEntryPoint(): ServiceCardResourceEntryPointModule.ServiceCardResourceEntryPoint {
    if (!::serviceCardResourceEntryPoint.isInitialized) {
        serviceCardResourceEntryPoint =
            EntryPoints.get(
                LocalContext.current.applicationContext,
                ServiceCardResourceEntryPointModule.ServiceCardResourceEntryPoint::class.java,
            )
    }
    return serviceCardResourceEntryPoint
}

fun LazyListScope.loadItems(
    serviceCardTileOrgData: ServiceCardTileOrgData,
    onUIAction: (UIAction) -> Unit
) {
    val serviceCardMlcDataList =
        serviceCardTileOrgData.items.filterIsInstance<ServiceCardMlcData>()
    val serviceChipCardMlcDataList =
        serviceCardTileOrgData.items.filterIsInstance<ServiceChipCardMlcData>()

    createRow(
        items = serviceCardMlcDataList,
        startTopPadding = serviceCardTileOrgData.paddingMode.toDp(16.dp),
        onUIAction = onUIAction
    )

    if (serviceChipCardMlcDataList.isEmpty()) return

    if (serviceCardMlcDataList.isNotEmpty()) {
        item {
            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        horizontal = 24.dp,
                        vertical = 16.dp
                    ),
                thickness = 1.dp,
                color = WhiteAlpha40
            )
        }
    }

    createRow(
        items = serviceChipCardMlcDataList,
        startTopPadding = 0.dp,
        onUIAction = onUIAction
    )
}

data class ServiceCardTileOrgData(
    val items: SnapshotStateList<UIElementData>,
    val componentId: UiText? = null,
    val paddingMode: TopPaddingMode = TopPaddingMode.LARGE,
) : UIElementData

private fun LazyListScope.createRow(
    items: List<UIElementData>,
    onUIAction: (UIAction) -> Unit,
    startTopPadding: Dp
) {
    val rowsCount = (items.size + 1) / 2
    if (rowsCount > 0) {
        repeat(rowsCount) { rowIndex ->
            item {
                Row(
                    modifier = Modifier
                        .padding(
                            start = 24.dp,
                            top = if (rowIndex == 0) startTopPadding else 8.dp,
                            end = 24.dp
                        )
                ) {
                    val firstIndex = rowIndex * 2
                    val secondIndex = firstIndex + 1
                    val hasSecond = secondIndex < items.size

                    if (hasSecond) {
                        createCardMlc(
                            modifier = Modifier
                                .weight(1f),
                            item = items[firstIndex],
                            onUIAction = onUIAction
                        )

                        Spacer(
                            modifier = Modifier
                                .width(8.dp)
                        )

                        createCardMlc(
                            modifier = Modifier
                                .weight(1f),
                            item = items[secondIndex],
                            onUIAction = onUIAction
                        )
                    } else {
                        createCardMlc(
                            modifier = Modifier
                                .fillMaxWidth(),
                            item = items[firstIndex],
                            onUIAction = onUIAction
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
private fun createCardMlc(
    modifier: Modifier,
    item: UIElementData,
    onUIAction: (UIAction) -> Unit
) {
    (item as? ServiceCardMlcData)?.let { serviceCardMlcData ->
        ServiceCardMlc(
            modifier = modifier,
            data = serviceCardMlcData,
            onUIAction = onUIAction,
            serviceCardResourceHelper = requireServiceCardResourceEntryPoint().serviceCardResourceHelper
        )
    }
    (item as? ServiceChipCardMlcData)?.let { serviceChipCardMlcData ->
        ServiceChipCardMlc(
            modifier = modifier,
            data = serviceChipCardMlcData,
            onUIAction = onUIAction,
            serviceCardResourceHelper = requireServiceCardResourceEntryPoint().serviceCardResourceHelper
        )
    }
}