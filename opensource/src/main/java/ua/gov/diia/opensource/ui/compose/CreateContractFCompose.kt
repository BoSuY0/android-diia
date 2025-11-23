package ua.gov.diia.opensource.ui.compose

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
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
        composeView?.setContent {
            val categoriesState by contractsMenuViewModel.categories.collectAsStateWithLifecycle()
            val templatesState by contractsMenuViewModel.templates.collectAsStateWithLifecycle()
            val partySchemaState by contractsFlowViewModel.partySchema.collectAsStateWithLifecycle()
            val sessionPartiesState by contractsFlowViewModel.sessionParties.collectAsStateWithLifecycle()
            val contractFieldsState by contractsFlowViewModel.contractFields.collectAsStateWithLifecycle()
            val partyContextState by contractsFlowViewModel.partyContextFields.collectAsStateWithLifecycle()
            val mainRoleState by contractsFlowViewModel.mainRole.collectAsStateWithLifecycle()
            val isFlowLoading by contractsFlowViewModel.isLoading.collectAsStateWithLifecycle()
            val chatMessages by contractsFlowViewModel.chatMessages.collectAsStateWithLifecycle()
            val isChatSending by contractsFlowViewModel.isChatSending.collectAsStateWithLifecycle()
            var navigationState by remember { mutableStateOf<CreateContractNavigationState>(CreateContractNavigationState.Intro) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { contractsFlowViewModel.loadMySessions() }

            AnimatedContent(
                targetState = navigationState,
                transitionSpec = {
                    fun navIndex(state: CreateContractNavigationState): Int = when (state) {
                        is CreateContractNavigationState.Intro -> 0
                        is CreateContractNavigationState.SelectCategory -> 1
                        is CreateContractNavigationState.ContractTemplates -> 2
                        is CreateContractNavigationState.SelectMode -> 3
                        is CreateContractNavigationState.SelectRoleMenu -> 4
                        is CreateContractNavigationState.SelectRole -> 5
                        is CreateContractNavigationState.InputData -> 6
                        is CreateContractNavigationState.ChatAi -> 7
                        is CreateContractNavigationState.Details -> 8
                    }
                    val forward = navIndex(targetState) > navIndex(initialState)
                    val slideIn = if (forward) slideInHorizontally { width -> width } else slideInHorizontally { width -> -width }
                    val slideOut = if (forward) slideOutHorizontally { width -> -width } else slideOutHorizontally { width -> width }
                    (slideIn + fadeIn()).togetherWith(slideOut + fadeOut())
                },
                label = "CreateContractTransition"
            ) { state ->
                when (state) {
                    is CreateContractNavigationState.Intro -> {
                        LegalContractsIntroScreen(
                            onBackClick = { findNavController().popBackStack() },
                            onStartClick = { navigationState = CreateContractNavigationState.SelectCategory }
                        )
                    }
                    is CreateContractNavigationState.SelectCategory -> {
                        LaunchedEffect(Unit) { contractsMenuViewModel.loadCategories() }
                        LegalContractsScreen(
                            contractCategories = categoriesState,
                            onBackClick = { findNavController().popBackStack() },
                            onContractSelected = { categoryId ->
                                scope.launch {
                                    contractsFlowViewModel.ensureSession()
                                    contractsFlowViewModel.selectCategory(categoryId)
                                    navigationState = CreateContractNavigationState.ContractTemplates(categoryId)
                                }
                            },
                            onAiChatClick = {
                                navigationState = CreateContractNavigationState.ChatAi
                            }
                        )
                    }
                    is CreateContractNavigationState.ContractTemplates -> {
                        LaunchedEffect(state.categoryId) {
                            contractsMenuViewModel.loadTemplates(state.categoryId)
                        }
                        ContractTemplatesScreen(
                            categoryId = state.categoryId,
                            templates = templatesState[state.categoryId].orEmpty(),
                            onBackClick = { navigationState = CreateContractNavigationState.SelectCategory },
                            onTemplateSelected = { templateId ->
                                scope.launch {
                                    contractsFlowViewModel.selectTemplate(templateId)
                                    navigationState = CreateContractNavigationState.SelectMode(templateId)
                                }
                            }
                        )
                    }
                    is CreateContractNavigationState.SelectMode -> {
                        LaunchedEffect(Unit) {
                            contractsMenuViewModel.loadCategories()
                        }
                        ContractFillingModeScreen(
                            onBackClick = { navigationState = CreateContractNavigationState.SelectCategory },
                            onModeSelected = { isBothSides ->
                                navigationState = CreateContractNavigationState.SelectRoleMenu(state.contractType, isBothSides)
                                scope.launch {
                                    contractsFlowViewModel.setFillingMode(isBothSides)
                                }
                            }
                        )
                    }
                    is CreateContractNavigationState.SelectRoleMenu -> {
                        LaunchedEffect(state.contractType) {
                            contractsFlowViewModel.refreshSessionSchema()
                        }
                        ContractRoleMenuScreen(
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            clientId = contractsFlowViewModel.clientId,
                            isBothSides = state.isBothSides,
                            mainRoleId = mainRoleState,
                            onBackClick = { navigationState = CreateContractNavigationState.SelectMode(state.contractType) },
                            onRoleSelected = { roleId ->
                                navigationState = CreateContractNavigationState.SelectRole(
                                    contractType = state.contractType,
                                    isBothSides = state.isBothSides,
                                    selectedRoleId = roleId
                                )
                            }
                        )
                    }
                    is CreateContractNavigationState.SelectRole -> {
                        LaunchedEffect(state.contractType) {
                            contractsFlowViewModel.refreshSessionSchema()
                        }
                        ContractRoleSelectionScreen(
                            contractType = state.contractType,
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            contractFields = contractFieldsState,
                            partyContext = partyContextState,
                            clientId = contractsFlowViewModel.clientId,
                            onBackClick = { navigationState = CreateContractNavigationState.SelectMode(state.contractType) },
                            onSaveSuccess = { contract ->
                                navigationState = CreateContractNavigationState.Details(contract)
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
                            mainRoleId = mainRoleState,
                            initialSelectedRoleId = state.selectedRoleId,
                            isLoading = isFlowLoading
                        )
                    }
                    is CreateContractNavigationState.InputData -> {
                        ContractDataInputScreen(
                            role = state.role,
                            onBackClick = { navigationState = CreateContractNavigationState.SelectRole(state.contractType, state.isBothSides) },
                            onNextClick = {
                                // Finish flow, go back
                                findNavController().popBackStack()
                            }
                        )
                    }
                    is CreateContractNavigationState.Details -> {
                        var contractWithHistory by remember(state.contract.id) { mutableStateOf(state.contract) }
                        LaunchedEffect(state.contract.id) {
                            val history = contractsFlowViewModel.fetchContractHistory(state.contract.id).orEmpty()
                            val nonEmptyHistory = history.takeIf { it.isNotEmpty() }
                                ?: listOf(
                                    HistoryUiModel(
                                        date = state.contract.lastUpdated,
                                        description = state.contract.subtitle
                                    )
                                )
                            contractWithHistory = state.contract.copy(history = nonEmptyHistory)
                        }
                        ContractDetailsScreen(
                            contract = contractWithHistory,
                            onBackClick = { navigationState = CreateContractNavigationState.SelectCategory },
                            onDownloadClick = {
                                scope.launch {
                                    contractsFlowViewModel.downloadContract(requireContext(), contractWithHistory.id)
                                }
                            },
                            onSignClick = {
                                scope.launch {
                                    contractsFlowViewModel.signContract(
                                        sessionId = contractWithHistory.id,
                                        currentContract = contractWithHistory
                                    )?.let { updated ->
                                        contractWithHistory = updated
                                    }
                                }
                            },
                            onViewClick = { findNavController().popBackStack() },
                            onEditClick = { findNavController().popBackStack() },
                            onShareClick = {
                                scope.launch {
                                    contractsFlowViewModel.shareContractLink(requireContext(), contractWithHistory.id)
                                }
                            }
                        )
                    }
                    is CreateContractNavigationState.ChatAi -> {
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
                                navigationState = CreateContractNavigationState.SelectCategory
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}

sealed class CreateContractNavigationState {
    object Intro : CreateContractNavigationState()
    object SelectCategory : CreateContractNavigationState()
    data class ContractTemplates(val categoryId: String) : CreateContractNavigationState()
    data class SelectMode(val contractType: String) : CreateContractNavigationState()
    data class SelectRoleMenu(val contractType: String, val isBothSides: Boolean) : CreateContractNavigationState()
    data class SelectRole(val contractType: String, val isBothSides: Boolean, val selectedRoleId: String? = null) : CreateContractNavigationState()
    data class InputData(val contractType: String, val isBothSides: Boolean, val role: String) : CreateContractNavigationState()
    object ChatAi : CreateContractNavigationState()
    data class Details(val contract: ContractUiModel) : CreateContractNavigationState()
}
