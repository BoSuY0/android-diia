package ua.gov.diia.opensource.helper

import androidx.compose.runtime.snapshots.SnapshotStateList
import ua.gov.diia.opensource.R
import ua.gov.diia.publicservice.models.PublicServiceCategory
import ua.gov.diia.publicservice.models.PublicServiceChip
import ua.gov.diia.publicservice.models.PublicServiceTab
import ua.gov.diia.publicservice.models.PublicServicesCategories
import ua.gov.diia.publicservice.ui.categories.compose.PublicServicesCategoriesTabMapper
import ua.gov.diia.ui_base.components.atom.status.SquareChipStatusAtmData
import ua.gov.diia.ui_base.components.atom.status.SquareChipType
import ua.gov.diia.ui_base.components.infrastructure.DataActionWrapper
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.state.UIState
import ua.gov.diia.ui_base.components.infrastructure.utils.SidePaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.TopPaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiIcon
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.molecule.card.ServiceCardMlcData
import ua.gov.diia.ui_base.components.molecule.card.ServiceChipCardMlcData
import ua.gov.diia.ui_base.components.molecule.header.chiptabbar.ChipTabMoleculeDataV2
import ua.gov.diia.ui_base.components.molecule.header.chiptabbar.ChipTabsOrgData
import ua.gov.diia.ui_base.components.molecule.input.SearchInputV2Data
import ua.gov.diia.ui_base.components.molecule.tile.ServiceCardTileOrgData
import ua.gov.diia.ui_base.components.organism.carousel.HalvedCardCarouselOrgData
import ua.gov.diia.ui_base.util.toUiModel
import javax.inject.Inject

class PublicServicesCategoriesTabMapperImpl @Inject constructor() :
    PublicServicesCategoriesTabMapper {

    override fun generateServiceCardTileOrgData(
        categories: List<PublicServiceCategory>,
        tabs: List<PublicServiceTab>?,
        selectedTab: String?
    ): ServiceCardTileOrgData {
        return ServiceCardTileOrgData(
            items = SnapshotStateList<UIElementData>().apply {
                if (selectedTab == VETERAN_TAB_CODE) {
                    addAll(
                        categories
                            .filter { category -> category.chips.isNullOrEmpty() }
                            .map { category -> category.toServiceCardMlcData() }
                    )
                    addAll(
                        categories
                            .filter { category -> !category.chips.isNullOrEmpty() }
                            .map { category -> category.toServiceChipCardMlcData(selectedTab) }
                    )
                } else {
                    addAll(
                        categories
                            .map { category -> category.toServiceCardMlcData() }
                    )
                }
            },
            componentId = UiText.StringResource(R.string.home_service_tile_test_tag)
        )
    }

    override fun generateComposeChipTabBarV2(
        tabs: List<PublicServiceTab>?,
        selectedTab: String?
    ): ChipTabsOrgData? {
        return if (tabs?.size == 1) {
            null
        } else ChipTabsOrgData(
            componentId = UiText.StringResource(R.string.home_chip_tabs_services_test_tag),
            tabs = SnapshotStateList<ChipTabMoleculeDataV2>().apply {
                if (tabs != null) {
                    addAll(
                        tabs.map {
                            ChipTabMoleculeDataV2(
                                id = it.code,
                                title = it.name,
                                selectionState = if (it.code == selectedTab) UIState.Selection.Selected else UIState.Selection.Unselected,
                                showCheckIcon = false
                            )
                        }
                    )
                }
            },
            showDivider = true
        )
    }

    override fun generateSearchInputMoleculeV2(
        placeholder: String,
        mode: Int
    ): SearchInputV2Data {
        return SearchInputV2Data(
            placeholder = UiText.DynamicString(placeholder),
            mode = mode,
            componentId = UiText.StringResource(R.string.home_search_services_test_tag)
        )
    }

    override fun generateHalvedCardCarouselOrgData(
        selectedTab: String?,
        additionalElements: List<PublicServicesCategories.AdditionalElement>
    ): HalvedCardCarouselOrgData? {
        if (selectedTab == null) return null
        return additionalElements
            .firstOrNull { additionalElement -> selectedTab in additionalElement.tabCodes.orEmpty() }
            ?.halvedCardCarouselOrg
            ?.toUiModel()
    }

    private fun PublicServiceCategory.toServiceCardMlcData(): ServiceCardMlcData {
        val iconCode = if (this.icon.isNotEmpty()) this.icon else this.code
        return ServiceCardMlcData(
            label = this.name,
            id = this.code,
            icon = UiIcon.DrawableResource(iconCode)
        )
    }

    private fun PublicServiceCategory.toServiceChipCardMlcData(
        currentTabCode: String
    ): ServiceChipCardMlcData {
        val iconCode = if (this.icon.isNotEmpty()) this.icon else this.code
        val publicServiceChip = this.chips?.firstOrNull { chip -> chip.tab == currentTabCode }
        return ServiceChipCardMlcData(
            label = this.name,
            id = this.code,
            icon = UiIcon.DrawableResource(iconCode),
            squareChipStatusAtmData = publicServiceChip?.let { chip ->
                SquareChipStatusAtmData(
                    type = when (chip.type) {
                        PublicServiceChip.Type.BLUE -> SquareChipType.BLUE
                        PublicServiceChip.Type.GRAY -> SquareChipType.GRAY
                    },
                    title = chip.text,
                    paddingTop = TopPaddingMode.NONE,
                    paddingHorizontal = SidePaddingMode.NONE
                )
            }
        )
    }

    companion object {
        private const val VETERAN_TAB_CODE = "veteran"
    }

}
