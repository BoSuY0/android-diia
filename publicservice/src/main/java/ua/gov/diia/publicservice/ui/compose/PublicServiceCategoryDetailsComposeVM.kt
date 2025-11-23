package ua.gov.diia.publicservice.ui.compose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ua.gov.diia.ui_base.navigation.BaseNavigation
import ua.gov.diia.core.util.delegation.WithErrorHandlingOnFlow
import ua.gov.diia.core.util.delegation.WithRetryLastAction
import ua.gov.diia.core.util.extensions.lifecycle.asLiveData
import ua.gov.diia.publicservice.R
import ua.gov.diia.ui_base.util.navigation.generateComposeNavigationPanel
import ua.gov.diia.publicservice.helper.PublicServiceHelper
import ua.gov.diia.publicservice.models.PublicService
import ua.gov.diia.publicservice.models.PublicServiceCategory
import ua.gov.diia.ui_base.components.DiiaResourceIcon
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.addIfNotNull
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.components.infrastructure.navigation.NavigationPath
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiIcon
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.molecule.button.BtnIconRoundedMlcData
import ua.gov.diia.ui_base.components.molecule.input.SearchInputV2Data
import ua.gov.diia.ui_base.components.organism.bottom.BtnIconRoundedGroupOrgData
import ua.gov.diia.ui_base.components.organism.list.ListItemGroupOrgData
import javax.inject.Inject

@HiltViewModel
class PublicServiceCategoryDetailsComposeVM @Inject constructor(
    private val helper: PublicServiceHelper,
    private val errorHandling: WithErrorHandlingOnFlow,
    private val retryLastAction: WithRetryLastAction,
    private val composeMapper: PublicServiceCategoryDetailsComposeMapper
) : ViewModel(), WithErrorHandlingOnFlow by errorHandling,
    WithRetryLastAction by retryLastAction,
    PublicServiceCategoryDetailsComposeMapper by composeMapper,
    PublicServiceHelper by helper {

    private val _toolBarData = mutableStateListOf<UIElementData>()
    val toolBarData: SnapshotStateList<UIElementData> = _toolBarData

    private val _bodyData = mutableStateListOf<UIElementData>()
    val bodyData: SnapshotStateList<UIElementData> = _bodyData

    private val _bottomData = mutableStateListOf<UIElementData>()
    val bottomData: SnapshotStateList<UIElementData> = _bottomData

    private val _navigation = MutableSharedFlow<NavigationPath>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigation = _navigation.asSharedFlow()

    private val _category = MutableLiveData<PublicServiceCategory>()
    val category = _category.asLiveData()
    private var currentQuery: String? = null

    fun doInit(category: PublicServiceCategory) {
        clearContent()
        _category.value = category
        _toolBarData.addIfNotNull(
            generateComposeNavigationPanel(
                title = category.name,
                componentId = UiText.StringResource(R.string.home_navigation_panel_subcategories_test_tag)
            )
        )
        _bodyData.addIfNotNull(
            generateSearchInputMoleculeV2(
                placeholder = "Пошук",
                mode = 0
            )
        )
        applySearch(null)

        _bottomData.addIfNotNull(
            BtnIconRoundedGroupOrgData(
                items = listOf(
                    BtnIconRoundedMlcData(
                        id = "legalContractsSupport",
                        icon = UiIcon.DrawableResource(DiiaResourceIcon.MESSAGE_CIRCLE.code)
                    )
                )
            )
        )
    }

    fun onUIAction(event: UIAction) {
        when (event.actionKey) {
            UIActionKeysCompose.TOOLBAR_NAVIGATION_BACK -> {
                _navigation.tryEmit(BaseNavigation.Back)
            }

            UIActionKeysCompose.SEARCH_INPUT -> {
                currentQuery = event.data
                applySearch(currentQuery)
            }

            UIActionKeysCompose.LIST_ITEM_MLC,
            UIActionKeysCompose.LIST_ITEM_GROUP_ORG -> {
                event.action?.type?.let {
                    val service = event.data?.let { findPublicService(it) }
                    if (service != null) {
                        _navigation.tryEmit(
                            PublicServicesCategoriesDetailsNavigation.NavigateToService(
                                service
                            )
                        )
                    }
                }
            }
        }
    }

    private fun findPublicService(code: String): PublicService? {
        val snapshot = _category.value ?: return null
        return snapshot.publicServices.find {
            it.code == code
        }
    }

    private fun clearContent() {
        _toolBarData.clear()
        _bodyData.clear()
        _bottomData.clear()
    }

    private fun applySearch(query: String?) {
        val snapshot = _category.value ?: return
        val services = snapshot.publicServices
        val filtered = if (query.isNullOrBlank()) {
            services
        } else {
            services.filter { service ->
                service.search.contains(query, ignoreCase = true)
            }
        }
        val listData = filtered.toComposeListItemGroupOrg()
        val searchIndex = _bodyData.indexOfFirst { it is SearchInputV2Data }
        val listIndex = _bodyData.indexOfFirst { it is ListItemGroupOrgData }

        if (searchIndex != -1) {
            _bodyData[searchIndex] =
                (_bodyData[searchIndex] as SearchInputV2Data).onChange(query)
        }

        if (listIndex == -1) {
            _bodyData.addIfNotNull(listData)
        } else {
            _bodyData[listIndex] = listData
        }
    }
}

sealed class PublicServicesCategoriesDetailsNavigation : NavigationPath {

    data class NavigateToService(val service: PublicService) :
        PublicServicesCategoriesDetailsNavigation()
}
