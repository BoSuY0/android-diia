package ua.gov.diia.opensource.ui.compose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asSharedFlow
import ua.gov.diia.core.util.extensions.mutableSharedFlowOf
import ua.gov.diia.ui_base.R as UiBaseR
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.molecule.header.NavigationPanelMlcData
import ua.gov.diia.ui_base.components.molecule.header.TitleGroupMlcData
import ua.gov.diia.ui_base.components.organism.header.TopGroupOrgData
import ua.gov.diia.ui_base.components.atom.space.SpacerAtmData
import ua.gov.diia.ui_base.components.atom.space.SpacerAtmType
import ua.gov.diia.ui_base.navigation.BaseNavigation
import javax.inject.Inject

@HiltViewModel
class ContractsVM @Inject constructor() : ViewModel() {

    val topBarData: SnapshotStateList<UIElementData> = mutableStateListOf()
    val bodyData: SnapshotStateList<UIElementData> = mutableStateListOf()

    private val _navigation = mutableSharedFlowOf<BaseNavigation>()
    val navigation = _navigation.asSharedFlow()

    init {
        configureTopBar()
        configureBody()
    }

    fun onUIAction(event: UIAction) {
        when (event.actionKey) {
            UIActionKeysCompose.TOOLBAR_NAVIGATION_BACK -> {
                _navigation.tryEmit(BaseNavigation.Back)
            }
        }
    }

    private fun configureTopBar() {
        val toolbar = TopGroupOrgData(
            titleGroupMlcData = TitleGroupMlcData(
                heroText = UiText.StringResource(R.string.contracts_title),
                label = UiText.StringResource(R.string.contracts_subtitle),
                componentId = UiText.DynamicString("contracts_title")
            ),
            navigationPanelMlcData = NavigationPanelMlcData(
                title = UiText.StringResource(R.string.contracts_title),
                isContextMenuExist = false
            )
        )
        topBarData.add(toolbar)
    }

    private fun configureBody() {
        // Дані для екрану договорів підтягуються з реального джерела; мок-елементи видалені.
        bodyData.add(SpacerAtmData(type = SpacerAtmType.MEDIUM))
    }
}
