package ua.gov.diia.opensource.ui.compose

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ua.gov.diia.ui_base.components.DiiaResourceIcon
import ua.gov.diia.ui_base.components.infrastructure.UIElementData
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.components.infrastructure.navigation.NavigationPath
import ua.gov.diia.ui_base.components.infrastructure.state.UIState
import ua.gov.diia.ui_base.components.infrastructure.utils.SidePaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.TopPaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.molecule.input.InputTextMlcV2Data
import ua.gov.diia.ui_base.components.molecule.list.table.items.tableblock.TableMainHeadingMlcData
import ua.gov.diia.ui_base.components.molecule.message.AttentionIconMessageMlcData
import ua.gov.diia.ui_base.components.molecule.message.BackgroundMode
import ua.gov.diia.ui_base.components.organism.bottom.BottomGroupOrgData
import ua.gov.diia.ui_base.components.organism.container.InputBlockOrgData
import ua.gov.diia.ui_base.components.atom.button.BtnPlainAtmData
import ua.gov.diia.ui_base.components.atom.button.BtnPrimaryDefaultAtmData
import ua.gov.diia.ui_base.components.atom.icon.SmallIconAtmData
import ua.gov.diia.ui_base.navigation.BaseNavigation
import ua.gov.diia.ui_base.util.navigation.generateComposeNavigationPanel

class LeaseContractFormComposeVM : ViewModel() {

    private val _toolbarData = mutableStateListOf<UIElementData>()
    val toolbarData: SnapshotStateList<UIElementData> = _toolbarData

    private val _bodyData = mutableStateListOf<UIElementData>()
    val bodyData: SnapshotStateList<UIElementData> = _bodyData

    private val _bottomData = mutableStateListOf<UIElementData>()
    val bottomData: SnapshotStateList<UIElementData> = _bottomData

    private val _navigation = MutableSharedFlow<NavigationPath>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigation = _navigation.asSharedFlow()

    companion object {
        private const val VALIDATION_BANNER_ID = "leaseContractValidationBanner"
    }

    init {
        buildScreen()
    }

    private fun buildScreen() {
        _toolbarData.clear()
        _bodyData.clear()
        _bottomData.clear()

        _toolbarData.add(
            generateComposeNavigationPanel(
                title = "Договір оренди житла"
            )
        )

        val partiesItems = SnapshotStateList<UIElementData>().apply {
            add(
                createRequiredTextField(
                    id = "lessorFullName",
                    label = "ПІБ орендодавця",
                    placeholder = "Вкажіть повне ім'я орендодавця"
                )
            )
            add(
                createRequiredTextField(
                    id = "lesseeFullName",
                    label = "ПІБ орендаря",
                    placeholder = "Вкажіть повне ім'я орендаря"
                )
            )
        }

        val objectItems = SnapshotStateList<UIElementData>().apply {
            add(
                createRequiredTextField(
                    id = "propertyAddress",
                    label = "Адреса об'єкта оренди",
                    placeholder = "Населений пункт, вулиця, будинок, квартира"
                )
            )
            add(
                createRequiredTextField(
                    id = "contractTerm",
                    label = "Строк дії договору",
                    placeholder = "Наприклад: з 01.01.2024 по 31.12.2024"
                )
            )
            add(
                createRequiredTextField(
                    id = "rentAmount",
                    label = "Розмір орендної плати",
                    placeholder = "Наприклад: 10 000 грн на місяць"
                )
            )
        }

        _bodyData.add(
            InputBlockOrgData(
                id = "leaseContractParties",
                paddingTop = TopPaddingMode.MEDIUM,
                paddingHorizontal = SidePaddingMode.MEDIUM,
                tableMainHeadingMlc = TableMainHeadingMlcData(
                    title = UiText.DynamicString("Сторони договору"),
                    description = UiText.DynamicString("Вкажіть дані орендодавця та орендаря.")
                ),
                items = partiesItems
            )
        )

        _bodyData.add(
            InputBlockOrgData(
                id = "leaseContractObject",
                paddingTop = TopPaddingMode.MEDIUM,
                paddingHorizontal = SidePaddingMode.MEDIUM,
                tableMainHeadingMlc = TableMainHeadingMlcData(
                    title = UiText.DynamicString("Об'єкт та умови оренди"),
                    description = UiText.DynamicString("Опишіть житло, строк дії договору та розмір орендної плати.")
                ),
                items = objectItems
            )
        )

        _bottomData.add(
            BottomGroupOrgData(
                primaryButton = BtnPrimaryDefaultAtmData(
                    id = "leaseContractOrder",
                    title = UiText.DynamicString("Замовити договір")
                ),
                secondaryButton = BtnPlainAtmData(
                    id = "leaseContractPreview",
                    title = UiText.DynamicString("Попередній перегляд")
                )
            )
        )
    }

    private fun createRequiredTextField(
        id: String,
        label: String,
        placeholder: String
    ): InputTextMlcV2Data {
        return InputTextMlcV2Data(
            id = id,
            label = label,
            inputValue = "",
            placeholder = placeholder,
            validationData = listOf(
                InputTextMlcV2Data.ValidationTextItem(
                    regex = ".+",
                    flags = emptyList(),
                    errorMessage = "Поле є обов'язковим"
                )
            ),
            validation = UIState.Validation.NeverBeenPerformed,
            isEnabled = true,
            mandatory = true,
            paddingTop = TopPaddingMode.NONE,
            paddingHorizontal = SidePaddingMode.NONE
        )
    }

    fun onUIAction(event: UIAction) {
        when (event.actionKey) {
            UIActionKeysCompose.TOOLBAR_NAVIGATION_BACK -> {
                _navigation.tryEmit(BaseNavigation.Back)
            }

            UIActionKeysCompose.TEXT_INPUT_V2 -> {
                handleTextChanged(
                    id = event.optionalId,
                    newValue = event.data
                )
            }

            UIActionKeysCompose.CLEAR_TEXT_INPUT_V2 -> {
                handleClearInput(id = event.optionalId)
            }

            UIActionKeysCompose.BUTTON_REGULAR,
            UIActionKeysCompose.BTN_PLAIN_ATM -> {
                when (event.data) {
                    "leaseContractOrder" -> {
                        if (!isFormValid()) {
                            showValidationBanner()
                        } else {
                            hideValidationBanner()
                            // TODO: implement order flow when form is valid
                        }
                    }

                    "leaseContractPreview" -> {
                        // TODO: implement preview flow
                    }
                }
            }
        }
    }

    private fun handleTextChanged(id: String?, newValue: String?) {
        if (id == null) return
        _bodyData.forEachIndexed { index, element ->
            if (element is InputBlockOrgData) {
                _bodyData[index] = element.onInputChanged(id, newValue)
            }
        }
        if (isFormValid()) {
            hideValidationBanner()
        }
    }

    private fun handleClearInput(id: String?) {
        if (id == null) return
        _bodyData.forEachIndexed { index, element ->
            if (element is InputBlockOrgData) {
                _bodyData[index] = element.onClearInput(id)
            }
        }
    }

    private fun isFormValid(): Boolean {
        val forms = _bodyData.filterIsInstance<InputBlockOrgData>()
        if (forms.isEmpty()) return false
        return forms.all { it.isFormFilledAndValid() }
    }

    private fun createValidationBanner(): AttentionIconMessageMlcData {
        return AttentionIconMessageMlcData(
            id = VALIDATION_BANNER_ID,
            icon = SmallIconAtmData(
                code = DiiaResourceIcon.ELLIPSE_INFO.code
            ),
            text = UiText.DynamicString("Заповніть усі необхідні поля договору."),
            backgroundMode = BackgroundMode.NOTE,
            paddingTop = TopPaddingMode.MEDIUM,
            paddingHorizontal = SidePaddingMode.MEDIUM
        )
    }

    private fun showValidationBanner() {
        val alreadyShown = _bodyData.any {
            it is AttentionIconMessageMlcData && it.id == VALIDATION_BANNER_ID
        }
        if (alreadyShown) return

        val banner = createValidationBanner()
        val lastFormIndex = _bodyData.indexOfLast { it is InputBlockOrgData }
        if (lastFormIndex != -1) {
            _bodyData.add(lastFormIndex + 1, banner)
        } else {
            _bodyData.add(banner)
        }
    }

    private fun hideValidationBanner() {
        _bodyData.removeAll {
            it is AttentionIconMessageMlcData && it.id == VALIDATION_BANNER_ID
        }
    }
}
