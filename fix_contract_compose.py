import os

file_path = r"c:\Users\bodys\AndroidStudioProjects\android-diia\opensource\src\main\java\ua\gov\diia\opensource\ui\compose\CreateContractFCompose.kt"

content = r"""package ua.gov.diia.opensource.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import ua.gov.diia.opensource.NavMainDirections
import ua.gov.diia.opensource.ui.compose.ContractsMenuViewModel
import ua.gov.diia.opensource.ui.compose.ContractsFlowViewModel

@AndroidEntryPoint
class CreateContractFCompose : Fragment() {

    private var composeView: ComposeView? = null
    private val contractsMenuViewModel: ContractsMenuViewModel by viewModels()
    private val contractsFlowViewModel: ContractsFlowViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        composeView = ComposeView(requireContext())
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        composeView?.setContent {
            val categoriesState by contractsMenuViewModel.categories.collectAsStateWithLifecycle()
            val templatesState by contractsMenuViewModel.templates.collectAsStateWithLifecycle()
            val isLoadingCategories by contractsMenuViewModel.isLoadingCategories.collectAsStateWithLifecycle()
            val isLoadingTemplates by contractsMenuViewModel.isLoadingTemplates.collectAsStateWithLifecycle()
            val partySchemaState by contractsFlowViewModel.partySchema.collectAsStateWithLifecycle()
            val sessionPartiesState by contractsFlowViewModel.sessionParties.collectAsStateWithLifecycle()
            val contractFieldsState by contractsFlowViewModel.contractFields.collectAsStateWithLifecycle()
            val partyContextState by contractsFlowViewModel.partyContextFields.collectAsStateWithLifecycle()
            val isFlowLoading by contractsFlowViewModel.isLoading.collectAsStateWithLifecycle()
            val previewHtml by contractsFlowViewModel.previewHtml.collectAsStateWithLifecycle()
            val isPreviewLoading by contractsFlowViewModel.isPreviewLoading.collectAsStateWithLifecycle()
            val chatMessages by contractsFlowViewModel.chatMessages.collectAsStateWithLifecycle()
            val isChatSending by contractsFlowViewModel.isChatSending.collectAsStateWithLifecycle()
            val flowError by contractsFlowViewModel.error.collectAsStateWithLifecycle()
            // Визначаємо початковий стан - пропускаємо intro якщо користувач обрав "Більше не показувати"
            val initialStep = if (contractsFlowViewModel.shouldSkipIntro()) {
                ContractCreationStep.SelectCategory
            } else {
                ContractCreationStep.Intro
            }
            var navigationState by remember { mutableStateOf<ContractCreationStep>(initialStep) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { contractsFlowViewModel.loadMySessions() }
            val navigateToContractDetails: (ContractUiModel) -> Unit = { contract ->
                val directions = NavMainDirections.actionHomeFToContracts(
                    sessionId = contract.id,
                    openDetails = true
                )
                navController.navigate(
                    directions,
                    navOptions {
                        popUpTo(ua.gov.diia.opensource.R.id.createContractFCompose) {
                            inclusive = true
                        }
                    }
                )
            }

            AnimatedContent(
                targetState = navigationState,
                transitionSpec = {
                    val forward = targetState.stepIndex > initialState.stepIndex
                    val slideIn = if (forward) slideInHorizontally { width -> width } else slideInHorizontally { width -> -width }
                    val slideOut = if (forward) slideOutHorizontally { width -> -width } else slideOutHorizontally { width -> width }
                    (slideIn + fadeIn()).togetherWith(slideOut + fadeOut())
                },
                label = "CreateContractTransition"
            ) { state ->
                when (state) {
                    is ContractCreationStep.Intro -> {
                        LegalContractsIntroScreen(
                            onBackClick = { findNavController().popBackStack() },
                            onStartClick = { navigationState = ContractCreationStep.SelectCategory },
                            onSkipIntroChange = { skip -> contractsFlowViewModel.setSkipIntro(skip) }
                        )
                    }
                    is ContractCreationStep.SelectCategory -> {
                        LaunchedEffect(Unit) { contractsMenuViewModel.loadCategories() }
                        LegalContractsScreen(
                            contractCategories = categoriesState,
                            onBackClick = { findNavController().popBackStack() },
                            onContractSelected = { categoryId ->
                                scope.launch {
                                    // Сесія створюється пізніше, при збереженні даних
                                    contractsFlowViewModel.selectCategory(categoryId)
                                    navigationState = ContractCreationStep.SelectTemplate(categoryId)
                                }
                            },
                            onAiChatClick = {
                                navigationState = ContractCreationStep.AiChat
                            }
                        )
                    }
                    is ContractCreationStep.SelectTemplate -> {
                        LaunchedEffect(state.categoryId) {
                            contractsMenuViewModel.loadTemplates(state.categoryId)
                        }
                        ContractTemplatesScreen(
                            categoryId = state.categoryId,
                            templates = templatesState[state.categoryId].orEmpty(),
                            onBackClick = { navigationState = ContractCreationStep.SelectCategory },
                            onTemplateSelected = { templateId ->
                                scope.launch {
                                    contractsFlowViewModel.selectTemplate(templateId)
                                    navigationState = ContractCreationStep.SelectMode(templateId)
                                }
                            }
                        )
                    }
                    is ContractCreationStep.SelectMode -> {
                        LaunchedEffect(Unit) {
                            contractsMenuViewModel.loadCategories()
                        }
                        ContractFillingModeScreen(
                            onBackClick = { navigationState = ContractCreationStep.SelectCategory },
                            onModeSelected = { isBothSides ->
                                contractsFlowViewModel.setFillingMode(isBothSides)
                                navigationState = ContractCreationStep.SelectRoleMenu(state.contractType, isBothSides)
                            }
                        )
                    }
                    is ContractCreationStep.SelectRoleMenu -> {
                        // Схема ролей вже завантажена в selectCategory без сесії
                        ContractRoleMenuScreen(
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            clientId = contractsFlowViewModel.clientId,
                            isBothSides = state.isBothSides,
                            onBackClick = { navigationState = ContractCreationStep.SelectMode(state.contractType) },
                            onRoleSelected = { roleId ->
                                navigationState = ContractCreationStep.FillRoleData(
                                    contractType = state.contractType,
                                    isBothSides = state.isBothSides,
                                    selectedRoleId = roleId
                                )
                            }
                        )
                    }
                    is ContractCreationStep.FillRoleData -> {
                        // Схема вже завантажена з категорії, сесія створюється при saveRoleData
                        ContractRoleSelectionScreen(
                            contractType = state.contractType,
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            contractFields = contractFieldsState,
                            partyContext = partyContextState,
                            clientId = contractsFlowViewModel.clientId,
                            onBackClick = { navigationState = ContractCreationStep.SelectRoleMenu(state.contractType, state.isBothSides) },
                            onSaveSuccess = { contract ->
                                navigateToContractDetails(contract)
                            },
                            onRemoteSave = { roleId, personTypeId, fields ->
                                contractsFlowViewModel.saveRoleData(
                                    roleId = roleId,
                                    personTypeId = personTypeId,
                                    fields = fields
                                )
                            },
                            onPartyContextChanged = { roleId, personTypeId ->
                                contractsFlowViewModel.updatePartyContext(roleId, personTypeId)
                            },
                            isBothSides = state.isBothSides,
                            initialSelectedRoleId = state.selectedRoleId,
                            isLoading = isFlowLoading,
                            backendError = flowError,
                            onClearError = { contractsFlowViewModel.clearError() }
                        )
                    }
                    is ContractCreationStep.ContractDetails -> {
                        navigateToContractDetails(state.contract)
                    }
                    is ContractCreationStep.ContractPreview -> {
                        LaunchedEffect(state.contract.id) {
                            contractsFlowViewModel.clearPreview()
                            contractsFlowViewModel.loadContractPreview(state.contract.id)
                        }
                        ContractPreviewScreen(
                            title = state.contract.title,
                            html = previewHtml,
                            isLoading = isPreviewLoading,
                            onBackClick = {
                                contractsFlowViewModel.clearPreview()
                                navigationState = ContractCreationStep.ContractDetails(state.contract)
                            },
                            onRetry = {
                                scope.launch { contractsFlowViewModel.loadContractPreview(state.contract.id) }
                            }
                        )
                    }
                    is ContractCreationStep.AiChat -> {
                        LaunchedEffect(Unit) {
                            contractsFlowViewModel.ensureSession(withLoading = false)
                        }
                        ContractAiChatScreen(
                            messages = chatMessages,
                            isSending = isChatSending,
                            onSendMessage = { text ->
                                scope.launch { contractsFlowViewModel.sendChatMessage(text) }
                            },
                            onBackClick = {
                                navigationState = ContractCreationStep.SelectCategory
                            },
                            onNewChatClick = { contractsFlowViewModel.resetChat() }
                        )
                    }
                    // Інші стани не використовуються в цьому фрагменті
                    else -> { /* ContractPreview - не використовується */ }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}
"""

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print(f"Successfully wrote to {file_path}")
