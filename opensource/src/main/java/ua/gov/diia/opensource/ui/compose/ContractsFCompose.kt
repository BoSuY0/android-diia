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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ua.gov.diia.ui_base.components.infrastructure.collectAsEffect
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.navigation.BaseNavigation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ua.gov.diia.opensource.ui.compose.ContractsMenuViewModel
import ua.gov.diia.opensource.ui.compose.ContractsFlowViewModel

@AndroidEntryPoint
class ContractsFCompose : Fragment() {

    private var composeView: ComposeView? = null
    private val args: ContractsFComposeArgs by navArgs()
    private val viewModel: ContractsVM by viewModels()
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
        setDarkSystemBars()
        composeView?.setContent {
            val categoriesState by contractsMenuViewModel.categories.collectAsStateWithLifecycle()
            val contractsState by contractsFlowViewModel.contracts.collectAsStateWithLifecycle()
            val partySchemaState by contractsFlowViewModel.partySchema.collectAsStateWithLifecycle()
            val sessionPartiesState by contractsFlowViewModel.sessionParties.collectAsStateWithLifecycle()
            val contractFieldsState by contractsFlowViewModel.contractFields.collectAsStateWithLifecycle()
            val partyContextState by contractsFlowViewModel.partyContextFields.collectAsStateWithLifecycle()
            val mainRoleState by contractsFlowViewModel.mainRole.collectAsStateWithLifecycle()
            val currentUserRoleState by contractsFlowViewModel.currentUserRole.collectAsStateWithLifecycle()
            val isFlowLoading by contractsFlowViewModel.isLoading.collectAsStateWithLifecycle()
            val previewHtml by contractsFlowViewModel.previewHtml.collectAsStateWithLifecycle()
            val isPreviewLoading by contractsFlowViewModel.isPreviewLoading.collectAsStateWithLifecycle()
            val chatMessages by contractsFlowViewModel.chatMessages.collectAsStateWithLifecycle()
            val isChatSending by contractsFlowViewModel.isChatSending.collectAsStateWithLifecycle()
            val deepLinkSessionId = args.sessionId
            var navigationState by remember { mutableStateOf<ContractsNavigationState>(ContractsNavigationState.List) }
            var editingContract by remember { mutableStateOf<ContractUiModel?>(null) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { contractsFlowViewModel.loadMySessions() }
            LaunchedEffect(deepLinkSessionId) {
                if (!deepLinkSessionId.isNullOrBlank()) {
                    val joinedContract = contractsFlowViewModel.joinSessionFromDeepLink(deepLinkSessionId)
                    joinedContract?.let {
                        editingContract = it
                        navigationState = ContractsNavigationState.CreateRole(
                            contractType = it.id,
                            isBothSides = contractsFlowViewModel.fillingMode.value == "full",
                            selectedRoleId = contractsFlowViewModel.currentUserRole.value.orEmpty(),
                            fromEdit = true
                        )
                    }
                }
            }

            viewModel.navigation.collectAsEffect { navigation ->
                when (navigation) {
                    is BaseNavigation.Back -> {
                        if (navigationState !is ContractsNavigationState.List) {
                            // Handle internal back navigation
                            when (navigationState) {
                                is ContractsNavigationState.Details -> navigationState = ContractsNavigationState.List
                                is ContractsNavigationState.LegalContracts -> navigationState = ContractsNavigationState.List
                                is ContractsNavigationState.CreateMode -> navigationState = ContractsNavigationState.LegalContracts
                                is ContractsNavigationState.CreateRole -> {
                                    val current = navigationState as ContractsNavigationState.CreateRole
                                    navigationState = if (current.fromEdit && editingContract != null) {
                                        ContractsNavigationState.Details(editingContract!!)
                                    } else {
                                        ContractsNavigationState.CreateMode(current.contractType)
                                    }
                                }
                                is ContractsNavigationState.CreateInput -> {
                                    val current = navigationState as ContractsNavigationState.CreateInput
                                    navigationState = ContractsNavigationState.CreateRole(
                                        current.contractType,
                                        current.isBothSides,
                                        fromEdit = editingContract != null
                                    )
                                }
                                else -> navigationState = ContractsNavigationState.List
                            }
                        } else {
                            findNavController().popBackStack()
                        }
                    }
                    else -> Unit
                }
            }

            AnimatedContent(
                targetState = navigationState,
                transitionSpec = {
                    if (targetState is ContractsNavigationState.List && initialState !is ContractsNavigationState.List) {
                        // Back to list
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    } else if (targetState !is ContractsNavigationState.List && initialState is ContractsNavigationState.List) {
                        // From list to details or create
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        // Between flow steps (forward or backward) - simplified for now to always slide forward-like
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    }
                },
                label = "NavigationTransition"
            ) { state ->
                when (state) {
                    is ContractsNavigationState.List -> {
                        ContractsScreen(
                            contracts = contractsState,
                            onBackClick = { viewModel.onUIAction(UIAction(UIActionKeysCompose.TOOLBAR_NAVIGATION_BACK)) },
                            onContractClick = { contract -> navigationState = ContractsNavigationState.Details(contract) },
                            onCreateContractClick = {
                                navigationState = ContractsNavigationState.LegalContracts
                            }
                        )
                    }
                    is ContractsNavigationState.Details -> {
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
                            onBackClick = { navigationState = ContractsNavigationState.List },
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
                            onViewClick = {
                                navigationState = ContractsNavigationState.Preview(contractWithHistory)
                            },
                            onEditClick = {
                                editingContract = contractWithHistory
                                scope.launch {
                                    contractsFlowViewModel.startEditingSession(contractWithHistory.id)
                                    navigationState = ContractsNavigationState.CreateRole(
                                        contractType = contractWithHistory.id,
                                        isBothSides = false,
                                        selectedRoleId = currentUserRoleState ?: "",  // Empty string for editing to let user choose
                                        fromEdit = true
                                    )
                                }
                            },
                            onShareClick = {
                                scope.launch {
                                    contractsFlowViewModel.shareContractLink(requireContext(), contractWithHistory.id)
                                }
                            }
                        )
                    }
                    is ContractsNavigationState.LegalContracts -> {
                        LaunchedEffect(Unit) { contractsMenuViewModel.loadCategories() }
                        LegalContractsScreen(
                            contractCategories = categoriesState,
                            onBackClick = { navigationState = ContractsNavigationState.List },
                            onContractSelected = { contractType ->
                                scope.launch {
                                    contractsFlowViewModel.ensureSession()
                                    contractsFlowViewModel.selectCategory(contractType)
                                    navigationState = ContractsNavigationState.CreateMode(contractType)
                                }
                            },
                            onAiChatClick = {
                                navigationState = ContractsNavigationState.AiChat
                            }
                        )
                    }
                    is ContractsNavigationState.CreateMode -> {
                        ContractFillingModeScreen(
                            onBackClick = { navigationState = ContractsNavigationState.LegalContracts },
                            onModeSelected = { isBothSides -> 
                                navigationState = ContractsNavigationState.CreateRoleMenu(state.contractType, isBothSides)
                                scope.launch {
                                    contractsFlowViewModel.setFillingMode(isBothSides)
                                }
                            }
                        )
                    }
                    is ContractsNavigationState.CreateRoleMenu -> {
                        LaunchedEffect(state.contractType) {
                            contractsFlowViewModel.refreshSessionSchema()
                        }
                        ContractRoleMenuScreen(
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            clientId = contractsFlowViewModel.clientId,
                            isBothSides = state.isBothSides,
                            mainRoleId = mainRoleState,
                            onBackClick = { navigationState = ContractsNavigationState.CreateMode(state.contractType) },
                            onRoleSelected = { roleId ->
                                navigationState = ContractsNavigationState.CreateRole(state.contractType, state.isBothSides, roleId)
                            }
                        )
                    }
                    is ContractsNavigationState.CreateRole -> {
                        LaunchedEffect(state.contractType, state.fromEdit) {
                            val mode = if (state.fromEdit) "values" else "status"
                            contractsFlowViewModel.refreshSessionSchema(dataMode = mode)
                        }
                        ContractRoleSelectionScreen(
                            contractType = state.contractType,
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            contractFields = contractFieldsState,
                            partyContext = partyContextState,
                            clientId = contractsFlowViewModel.clientId,
                            onBackClick = {
                                navigationState = if (state.fromEdit && editingContract != null) {
                                    ContractsNavigationState.Details(editingContract!!)
                                } else {
                                    ContractsNavigationState.CreateMode(state.contractType)
                                }
                            },
                            onSaveSuccess = { contract ->
                                editingContract = contract
                                navigationState = ContractsNavigationState.Details(contract)
                            },
                            onRemoteSave = { roleId, personTypeId, fields ->
                                contractsFlowViewModel.saveRoleData(roleId, personTypeId, fields)
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
                    is ContractsNavigationState.CreateInput -> {
                        ContractDataInputScreen(
                            role = state.role,
                            onBackClick = {
                                navigationState = ContractsNavigationState.CreateRole(
                                    state.contractType,
                                    state.isBothSides,
                                    fromEdit = editingContract != null
                                )
                            },
                            onNextClick = { 
                                // Finish flow, go back to list for now
                                navigationState = ContractsNavigationState.List 
                            }
                        )
                    }
                    is ContractsNavigationState.AiChat -> {
                        LaunchedEffect(Unit) {
                            contractsFlowViewModel.ensureSession(withLoading = false)
                        }
                        ContractAiChatScreen(
                            messages = chatMessages,
                            isSending = isChatSending,
                            onSendMessage = { text ->
                                scope.launch { contractsFlowViewModel.sendChatMessage(text) }
                            },
                            onBackClick = { navigationState = ContractsNavigationState.LegalContracts }
                        )
                    }
                    is ContractsNavigationState.Preview -> {
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
                                navigationState = ContractsNavigationState.Details(state.contract)
                            },
                            onRetry = {
                                scope.launch { contractsFlowViewModel.loadContractPreview(state.contract.id) }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setDarkSystemBars()
    }

    private fun setDarkSystemBars() {
        val window = activity?.window ?: return
        val darkColor = ContextCompat.getColor(requireContext(), android.R.color.black)
        window.statusBarColor = darkColor
        window.navigationBarColor = darkColor
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}

sealed class ContractsNavigationState {
    object List : ContractsNavigationState()
    data class Details(val contract: ContractUiModel) : ContractsNavigationState()
    object LegalContracts : ContractsNavigationState()
    data class CreateMode(val contractType: String) : ContractsNavigationState()
    data class CreateRoleMenu(val contractType: String, val isBothSides: Boolean) : ContractsNavigationState()
    data class CreateRole(
        val contractType: String,
        val isBothSides: Boolean,
        val selectedRoleId: String? = null,
        val fromEdit: Boolean = false
    ) : ContractsNavigationState()
    data class CreateInput(val contractType: String, val isBothSides: Boolean, val role: String) : ContractsNavigationState()
    object AiChat : ContractsNavigationState()
    data class Preview(val contract: ContractUiModel) : ContractsNavigationState()
}
