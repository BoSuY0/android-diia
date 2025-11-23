package ua.gov.diia.ui_base.components.infrastructure.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ua.gov.diia.core.util.state.Loader
import ua.gov.diia.core.util.state.getLegacyProgress
import ua.gov.diia.ui_base.components.atom.button.BtnPlainAtm
import ua.gov.diia.ui_base.components.atom.button.BtnPlainAtmData
import ua.gov.diia.ui_base.components.atom.button.BtnPrimaryDefaultAtm
import ua.gov.diia.ui_base.components.atom.button.BtnPrimaryDefaultAtmData
import ua.gov.diia.ui_base.components.atom.text.TickerAtm
import ua.gov.diia.ui_base.components.atom.text.TickerAtomData
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.molecule.button.BtnLoadIconPlainGroupMlc
import ua.gov.diia.ui_base.components.molecule.button.BtnLoadIconPlainGroupMlcData
import ua.gov.diia.ui_base.components.molecule.button.BtnSlideMlc
import ua.gov.diia.ui_base.components.molecule.button.BtnSlideMlcData
import ua.gov.diia.ui_base.components.molecule.checkbox.CheckboxBtnOrg
import ua.gov.diia.ui_base.components.molecule.checkbox.CheckboxBtnOrgData
import ua.gov.diia.ui_base.components.molecule.checkbox.CheckboxBtnWhiteOrg
import ua.gov.diia.ui_base.components.molecule.checkbox.CheckboxBtnWhiteOrgData
import ua.gov.diia.ui_base.components.organism.bottom.BottomGroupOrg
import ua.gov.diia.ui_base.components.organism.bottom.BottomGroupOrgData
import ua.gov.diia.ui_base.components.organism.bottom.BtnIconRoundedGroupOrg
import ua.gov.diia.ui_base.components.organism.bottom.BtnIconRoundedGroupOrgData
import ua.gov.diia.ui_base.components.organism.list.ListItemGroupOrg
import ua.gov.diia.ui_base.components.organism.list.ListItemGroupOrgData
import ua.gov.diia.ui_base.mappers.loader.mapToLoader

@Composable
fun BottomBarRootContainer(
    modifier: Modifier = Modifier,
    bottomViews: SnapshotStateList<UIElementData>,
    loader: Loader = Loader.createComponent(isLoading = false),
    onUIAction: (UIAction) -> Unit
) {
    val lazyListScrollState = rememberLazyListState()
    var isContainBottomGroupOrg = false
    if (bottomViews.size != 0) {
        Column(modifier = modifier.fillMaxWidth()) {
            bottomViews.forEachIndexed { index, element ->
                when (element) {
                    is BottomGroupOrgData -> {
                        isContainBottomGroupOrg = true
                        BottomGroupOrg(
                            data = element,
                            progressIndicator = loader.getLegacyProgress(),
                            onUIAction = onUIAction
                        )
                    }

                    is CheckboxBtnOrgData -> {
                        CheckboxBtnOrg(
                            data = element,
                            progressIndicator = loader.getLegacyProgress(),
                            onUIAction = onUIAction
                        )
                    }

                    is ListItemGroupOrgData -> {
                        ListItemGroupOrg(
                            data = element,
                            onUIAction = onUIAction,
                            progressIndicator = loader.getLegacyProgress()
                        )
                    }

                    is BtnPrimaryDefaultAtmData -> {
                        BtnPrimaryDefaultAtm(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            data = element,
                            onUIAction = onUIAction,
                            loader = mapToLoader(loader.getLegacyProgress())
                        )
                    }

                    is BtnPlainAtmData -> {
                        BtnPlainAtm(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            data = element,
                            onUIAction = onUIAction
                        )
                    }

                    is BtnLoadIconPlainGroupMlcData -> {
                        BtnLoadIconPlainGroupMlc(
                            data = element,
                            onUIAction = onUIAction,
                            progressIndicator = loader.getLegacyProgress()
                        )
                        AddBottomPadding(true)
                    }

                    is CheckboxBtnWhiteOrgData -> {
                        CheckboxBtnWhiteOrg(
                            data = element,
                            onUIAction = onUIAction,
                            progressIndicator = loader.getLegacyProgress()
                        )
                    }

                    is TickerAtomData -> {
                        TickerAtm(
                            data = element,
                            onUIAction = onUIAction,
                        )
                    }
                    is BtnSlideMlcData -> {
                        BtnSlideMlc(
                            data = element,
                            onUIAction = onUIAction,
                        )
                    }

                    is BtnIconRoundedGroupOrgData -> {
                        isContainBottomGroupOrg = true
                        BtnIconRoundedGroupOrg(
                            modifier = Modifier.fillMaxWidth(),
                            data = element,
                            onUIAction = onUIAction
                        )
                    }
                }
            }
            if (!isContainBottomGroupOrg) {
                AddBottomPadding(true)
            }
        }
    }
}

@Composable
private fun AddBottomPadding(displayBottomSpacer: Boolean) {
    if (displayBottomSpacer) {
        Box(modifier = Modifier) {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
fun BottomBarRootContainerPreview_Case1() {

}
