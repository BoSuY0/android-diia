package ua.gov.diia.opensource.ui.compose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asSharedFlow
import ua.gov.diia.core.util.extensions.mutableSharedFlowOf
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.components.DiiaResourceIcon
import ua.gov.diia.ui_base.components.infrastructure.DataActionWrapper
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiIcon
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.molecule.header.NavigationPanelMlcData
import ua.gov.diia.ui_base.components.molecule.header.TitleGroupMlcData
import ua.gov.diia.ui_base.components.molecule.list.ListItemMlcData
import ua.gov.diia.ui_base.components.organism.header.TopGroupOrgData
import ua.gov.diia.ui_base.components.organism.list.ListItemGroupOrgData
import ua.gov.diia.ui_base.components.atom.space.SpacerAtmData
import ua.gov.diia.ui_base.components.atom.space.SpacerAtmType
import ua.gov.diia.ui_base.components.infrastructure.utils.SidePaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.TopPaddingMode
import ua.gov.diia.ui_base.navigation.BaseNavigation
import javax.inject.Inject

@HiltViewModel
class SignHistoryVM @Inject constructor() : ViewModel() {

    val topBarData: SnapshotStateList<UIElementData> = mutableStateListOf()
    val bodyData: SnapshotStateList<UIElementData> = mutableStateListOf()

    private val _navigation = mutableSharedFlowOf<BaseNavigation>()
    val navigation = _navigation.asSharedFlow()

    init {
        configureTopBar()
        configureBody()
    }

    fun onUIAction(event: UIAction) {
        if (event.actionKey == UIActionKeysCompose.TOOLBAR_NAVIGATION_BACK) {
            _navigation.tryEmit(BaseNavigation.Back)
        }
    }

    private fun configureTopBar() {
        val toolbar = TopGroupOrgData(
            navigationPanelMlcData = NavigationPanelMlcData(
                title = UiText.StringResource(R.string.sign_history_title),
                isContextMenuExist = false
            ),
            titleGroupMlcData = TitleGroupMlcData(
                heroText = UiText.StringResource(R.string.sign_history_title),
                label = UiText.StringResource(R.string.sign_history_subtitle),
                componentId = UiText.DynamicString("sign_history_title")
            ),
        )
        topBarData.add(toolbar)
    }

    private fun configureBody() {
        // Відступ між заголовком і першою картою
        bodyData.add(SpacerAtmData(type = SpacerAtmType.MEDIUM))

        val overview = ListItemGroupOrgData(
            itemsList = listOf(
                ListItemMlcData(
                    id = "sign_history_hint",
                    label = UiText.StringResource(R.string.sign_history_hint),
                    description = UiText.StringResource(R.string.sign_history_description),
                    iconLeft = UiIcon.DrawableResource(DiiaResourceIcon.DOC_INFO.code),
                    componentId = UiText.DynamicString("sign_history_hint")
                )
            ),
            componentId = UiText.DynamicString("sign_history_overview"),
            paddingTop = TopPaddingMode.LARGE,
            paddingHorizontal = SidePaddingMode.MEDIUM
        )
        bodyData.add(overview)
        bodyData.add(SpacerAtmData(type = SpacerAtmType.MEDIUM))

        val historyItems = listOf(
            SignedItem(
                id = "sign_history_contract",
                title = UiText.StringResource(R.string.sign_history_item_contract_label),
                description = UiText.StringResource(R.string.sign_history_signed_at, "12.01.2024"),
                icon = UiIcon.DrawableResource(DiiaResourceIcon.STACK.code)
            ),
            SignedItem(
                id = "sign_history_employment",
                title = UiText.StringResource(R.string.sign_history_item_employment_label),
                description = UiText.StringResource(R.string.sign_history_signed_at, "08.12.2023"),
                icon = UiIcon.DrawableResource(DiiaResourceIcon.BAG.code)
            ),
            SignedItem(
                id = "sign_history_application",
                title = UiText.StringResource(R.string.sign_history_item_statement_label),
                description = UiText.StringResource(R.string.sign_history_signed_at, "02.11.2023"),
                icon = UiIcon.DrawableResource(DiiaResourceIcon.QR.code)
            )
        )

        val historyGroup = ListItemGroupOrgData(
            itemsList = historyItems.map { item ->
                ListItemMlcData(
                    id = item.id,
                    label = item.title,
                    description = item.description,
                    iconLeft = item.icon,
                    action = DataActionWrapper(type = item.id),
                    componentId = UiText.DynamicString(item.id)
                )
            },
            componentId = UiText.DynamicString("sign_history_list"),
            paddingTop = TopPaddingMode.MEDIUM,
            paddingHorizontal = SidePaddingMode.MEDIUM
        )
        bodyData.add(historyGroup)
    }

    private data class SignedItem(
        val id: String,
        val title: UiText,
        val description: UiText,
        val icon: UiIcon.DrawableResource
    )
}
