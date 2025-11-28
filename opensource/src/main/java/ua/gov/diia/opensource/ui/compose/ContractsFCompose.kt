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
import androidx.compose.ui.platform.LocalContext
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
import android.widget.Toast
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
            val templatesState by contractsMenuViewModel.templates.collectAsStateWithLifecycle()
            val isLoadingTemplates by contractsMenuViewModel.isLoadingTemplates.collectAsStateWithLifecycle()
            val contractsState by contractsFlowViewModel.contracts.collectAsStateWithLifecycle()
            val partySchemaState by contractsFlowViewModel.partySchema.collectAsStateWithLifecycle()
            val sessionPartiesState by contractsFlowViewModel.sessionParties.collectAsStateWithLifecycle()
            val contractFieldsState by contractsFlowViewModel.contractFields.collectAsStateWithLifecycle()
            val partyContextState by contractsFlowViewModel.partyContextFields.collectAsStateWithLifecycle()
            val currentUserRoleState by contractsFlowViewModel.currentUserRole.collectAsStateWithLifecycle()
            val mainRoleState by contractsFlowViewModel.mainRole.collectAsStateWithLifecycle()
            val isFlowLoading by contractsFlowViewModel.isLoading.collectAsStateWithLifecycle()
            val previewHtml by contractsFlowViewModel.previewHtml.collectAsStateWithLifecycle()
            val isPreviewLoading by contractsFlowViewModel.isPreviewLoading.collectAsStateWithLifecycle()
            val chatMessages by contractsFlowViewModel.chatMessages.collectAsStateWithLifecycle()
            val isChatSending by contractsFlowViewModel.isChatSending.collectAsStateWithLifecycle()
            val flowError by contractsFlowViewModel.error.collectAsStateWithLifecycle()
            val deepLinkSessionId = args.sessionId
            val startInDetails = args.openDetails
            val startInCreationMenu = args.openCreationMenu
            var navigationState by remember {
                mutableStateOf<ContractsNavigationState>(
                    if (startInCreationMenu) ContractsNavigationState.LegalContracts else ContractsNavigationState.List
                )
            }
            var editingContract by remember { mutableStateOf<ContractUiModel?>(null) }
            val scope = rememberCoroutineScope()
            val appContext = LocalContext.current

            LaunchedEffect(flowError) {
                flowError?.let { message ->
                    Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
                    contractsFlowViewModel.clearError()
                }
            }
            LaunchedEffect(Unit) { contractsFlowViewModel.loadMySessions() }
            LaunchedEffect(deepLinkSessionId, startInDetails) {
                if (!deepLinkSessionId.isNullOrBlank()) {
                    val joinedContract = contractsFlowViewModel.joinSessionFromDeepLink(deepLinkSessionId)
                    if (joinedContract != null) {
                        editingContract = joinedContract
                        val userRole = contractsFlowViewModel.currentUserRole.value
                        navigationState = when {
                            startInDetails -> ContractsNavigationState.Details(joinedContract)
                            // Якщо користувач ще не обрав роль - показуємо вибір ролі
                            userRole.isNullOrBlank() -> ContractsNavigationState.CreateRoleMenu(
                                contractType = joinedContract.id,
                                templateId = contractsFlowViewModel.selectedTemplateId.orEmpty(),
                                isBothSides = contractsFlowViewModel.fillingMode.value == FillingMode.FULL
                            )
                            // Якщо роль вже обрана - показуємо форму заповнення
                            else -> ContractsNavigationState.CreateRole(
                                contractType = joinedContract.id,
                                templateId = contractsFlowViewModel.selectedTemplateId.orEmpty(),
                                isBothSides = contractsFlowViewModel.fillingMode.value == FillingMode.FULL,
                                selectedRoleId = userRole,
                                fromEdit = true
                            )
                        }
                    } else {
                        // Deep-link не вдалося обробити - показуємо повідомлення
                        // Помилка вже встановлена в joinSessionFromDeepLink і покажеться через Toast
                        android.util.Log.e("ContractsFCompose", "Deep-link join failed for sessionId=$deepLinkSessionId")
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
                                is ContractsNavigationState.LegalContracts -> {
                                    // Якщо флоу запустився одразу з вибору категорії - повертаємось до Сервісів
                                    if (startInCreationMenu) {
                                        findNavController().popBackStack()
                                    } else {
                                        navigationState = ContractsNavigationState.List
                                    }
                                }
                                is ContractsNavigationState.SelectTemplate -> navigationState = ContractsNavigationState.LegalContracts
                                is ContractsNavigationState.CreateMode -> {
                                    val current = navigationState as ContractsNavigationState.CreateMode
                                    navigationState = ContractsNavigationState.SelectTemplate(current.contractType)
                                }
                                is ContractsNavigationState.CreateRole -> {
                                    val current = navigationState as ContractsNavigationState.CreateRole
                                    navigationState = if (current.fromEdit && editingContract != null) {
                                        ContractsNavigationState.Details(editingContract!!)
                                    } else {
                                        ContractsNavigationState.CreateMode(current.contractType, current.templateId)
                                    }
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
                    // Визначаємо порядок станів для правильного напрямку анімації
                    fun navIndex(state: ContractsNavigationState): Int = when (state) {
                        is ContractsNavigationState.List -> 0
                        is ContractsNavigationState.Details -> 1
                        is ContractsNavigationState.LegalContracts -> 2
                        is ContractsNavigationState.SelectTemplate -> 3
                        is ContractsNavigationState.CreateMode -> 4
                        is ContractsNavigationState.CreateRoleMenu -> 5
                        is ContractsNavigationState.CreateRole -> 6
                        is ContractsNavigationState.AiChat -> 7
                        is ContractsNavigationState.Preview -> 8
                    }
                    val forward = navIndex(targetState) > navIndex(initialState)
                    if (forward) {
                        // Вперед - новий екран з'їжджає справа
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        // Назад - старий екран з'їжджає вправо
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
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
                        var shareLink by remember(state.contract.id) { mutableStateOf<String?>(null) }
                        LaunchedEffect(state.contract.id) {
                            val refreshed = contractsFlowViewModel.loadContractDetails(state.contract.id)
                            if (refreshed != null) {
                                contractWithHistory = refreshed
                            } else {
                                val historyFallback = contractsFlowViewModel.fetchContractHistory(state.contract.id).orEmpty()
                                val nonEmptyHistory = historyFallback.takeIf { it.isNotEmpty() }
                                    ?: listOf(
                                        HistoryUiModel(
                                            date = state.contract.lastUpdated,
                                            description = state.contract.subtitle
                                        )
                                    )
                                contractWithHistory = state.contract.copy(history = nonEmptyHistory)
                            }
                        }
                        shareLink?.let { link ->
                            ShareContractLinkDialog(
                                link = link,
                                onDismiss = { shareLink = null }
                            )
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
                                        templateId = contractsFlowViewModel.selectedTemplateId.orEmpty(),
                                        isBothSides = false,
                                        selectedRoleId = currentUserRoleState ?: "",  // Empty string for editing to let user choose
                                        fromEdit = true
                                    )
                                }
                            },
                            onShareClick = {
                                shareLink = contractsFlowViewModel.getContractShareLink(
                                    requireContext(),
                                    contractWithHistory.id
                                )
                            }
                        )
                    }
                    is ContractsNavigationState.LegalContracts -> {
                        LaunchedEffect(Unit) { contractsMenuViewModel.loadCategories() }
                        LegalContractsScreen(
                            contractCategories = categoriesState,
                            onBackClick = {
                                // Якщо флоу запустився одразу з вибору категорії - повертаємось до Сервісів
                                if (startInCreationMenu) {
                                    findNavController().popBackStack()
                                } else {
                                    navigationState = ContractsNavigationState.List
                                }
                            },
                            onContractSelected = { contractType ->
                                scope.launch {
                                    // Сесія створюється пізніше, при збереженні даних
                                    contractsFlowViewModel.selectCategory(contractType)
                                    navigationState = ContractsNavigationState.SelectTemplate(contractType)
                                }
                            },
                            onAiChatClick = {
                                navigationState = ContractsNavigationState.AiChat
                            }
                        )
                    }
                    is ContractsNavigationState.SelectTemplate -> {
                        LaunchedEffect(state.categoryId) {
                            contractsMenuViewModel.loadTemplates(state.categoryId)
                        }
                        ContractTemplatesScreen(
                            categoryId = state.categoryId,
                            templates = templatesState[state.categoryId].orEmpty(),
                            onBackClick = { navigationState = ContractsNavigationState.LegalContracts },
                            onTemplateSelected = { templateId ->
                                scope.launch {
                                    contractsFlowViewModel.selectTemplate(templateId)
                                    navigationState = ContractsNavigationState.CreateMode(state.categoryId, templateId)
                                }
                            },
                            isLoading = isLoadingTemplates
                        )
                    }
                    is ContractsNavigationState.CreateMode -> {
                        ContractFillingModeScreen(
                            onBackClick = { navigationState = ContractsNavigationState.SelectTemplate(state.contractType) },
                            onModeSelected = { isBothSides ->
                                contractsFlowViewModel.setFillingMode(isBothSides)
                                navigationState = ContractsNavigationState.CreateRoleMenu(state.contractType, state.templateId, isBothSides)
                            }
                        )
                    }
                    is ContractsNavigationState.CreateRoleMenu -> {
                        // Схема ролей вже завантажена в selectCategory без сесії
                        ContractRoleMenuScreen(
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            clientId = contractsFlowViewModel.clientId,
                            isBothSides = state.isBothSides,
                            onBackClick = { navigationState = ContractsNavigationState.CreateMode(state.contractType, state.templateId) },
                            onRoleSelected = { roleId ->
                                navigationState = ContractsNavigationState.CreateRole(state.contractType, state.templateId, state.isBothSides, roleId)
                            }
                        )
                    }
                    is ContractsNavigationState.CreateRole -> {
                        // Для редагування існуючого договору - оновлюємо схему
                        // Для нового - схема вже завантажена з категорії, сесія створюється при saveRoleData
                        if (state.fromEdit) {
                            LaunchedEffect(state.contractType) {
                                contractsFlowViewModel.refreshSessionSchema(dataMode = "values")
                            }
                        }
                        // В partial mode умови договору доступні ТІЛЬКИ для main_role
                        // (щоб орендар не бачив поля, які повинен заповнити орендодавець)
                        val filteredContractFields = if (!state.isBothSides && state.selectedRoleId != mainRoleState) {
                            emptyList()
                        } else {
                            contractFieldsState
                        }
                        ContractRoleSelectionScreen(
                            contractType = state.contractType,
                            roles = if (sessionPartiesState.isNotEmpty()) sessionPartiesState else partySchemaState?.roles.orEmpty(),
                            personTypes = partySchemaState?.personTypes.orEmpty(),
                            contractFields = filteredContractFields,
                            partyContext = partyContextState,
                            clientId = contractsFlowViewModel.clientId,
                            onBackClick = {
                                navigationState = if (state.fromEdit && editingContract != null) {
                                    ContractsNavigationState.Details(editingContract!!)
                                } else {
                                    ContractsNavigationState.CreateRoleMenu(state.contractType, state.templateId, state.isBothSides)
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
                            initialSelectedRoleId = state.selectedRoleId,
                            isLoading = isFlowLoading,
                            backendError = flowError,
                            onClearError = { contractsFlowViewModel.clearError() }
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
                            onBackClick = { navigationState = ContractsNavigationState.LegalContracts },
                            onNewChatClick = {
                                contractsFlowViewModel.resetChat()
                            },
                            onActionClick = { action ->
                                // Обробляємо action від LLM
                                when (action.type) {
                                    "navigate_filling_mode" -> {
                                        // Переходимо до вибору режиму заповнення
                                        val categoryId = action.payload["category_id"] ?: contractsFlowViewModel.selectedCategoryId.orEmpty()
                                        val templateId = action.payload["template_id"] ?: contractsFlowViewModel.selectedTemplateId.orEmpty()
                                        scope.launch {
                                            // Використовуємо syncSessionContext щоб зберегти поточну сесію з чату
                                            if (categoryId.isNotBlank()) {
                                                contractsFlowViewModel.syncSessionContext(categoryId, templateId.takeIf { it.isNotBlank() })
                                            }
                                            navigationState = ContractsNavigationState.CreateMode(categoryId, templateId)
                                        }
                                    }
                                    "confirm_category" -> {
                                        // Користувач підтвердив категорію - показуємо шаблони
                                        val categoryId = action.payload["category_id"].orEmpty()
                                        if (categoryId.isNotBlank()) {
                                            scope.launch {
                                                contractsFlowViewModel.selectCategory(categoryId)
                                                navigationState = ContractsNavigationState.SelectTemplate(categoryId)
                                            }
                                        }
                                    }
                                    "select_template" -> {
                                        // Переходимо до конкретного шаблону
                                        val categoryId = action.payload["category_id"].orEmpty()
                                        val templateId = action.payload["template_id"].orEmpty()
                                        if (templateId.isNotBlank()) {
                                            scope.launch {
                                                if (categoryId.isNotBlank()) {
                                                    contractsFlowViewModel.selectCategory(categoryId)
                                                }
                                                contractsFlowViewModel.selectTemplate(templateId)
                                                navigationState = ContractsNavigationState.CreateMode(categoryId, templateId)
                                            }
                                        }
                                    }
                                    else -> {
                                        // Невідомий тип action - ігноруємо
                                        android.util.Log.w("ContractsFCompose", "Unknown action type: ${action.type}")
                                    }
                                }
                            }
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
                                contractsFlowViewModel.loadContractPreview(state.contract.id)
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

/**
 * Стани навігації для ContractsFCompose.
 * Включає як стани перегляду договорів (List, Details, Preview),
 * так і стани флоу створення (LegalContracts, CreateMode, CreateRoleMenu, CreateRole, AiChat).
 * 
 * Для standalone флоу створення див. ContractCreationStep.
 */
sealed class ContractsNavigationState {
    /** Список всіх договорів */
    object List : ContractsNavigationState()
    
    /** Деталі конкретного договору */
    data class Details(val contract: ContractUiModel) : ContractsNavigationState()
    
    /** Прев'ю HTML договору */
    data class Preview(val contract: ContractUiModel) : ContractsNavigationState()
    
    // --- Стани флоу створення (аналогічні ContractCreationStep) ---
    
    /** Вибір категорії договору */
    object LegalContracts : ContractsNavigationState()
    
    /** Вибір шаблону договору */
    data class SelectTemplate(val categoryId: String) : ContractsNavigationState()
    
    /** Вибір режиму заповнення */
    data class CreateMode(val contractType: String, val templateId: String) : ContractsNavigationState()
    
    /** Меню вибору ролі */
    data class CreateRoleMenu(val contractType: String, val templateId: String, val isBothSides: Boolean) : ContractsNavigationState()
    
    /** Форма заповнення даних для ролі */
    data class CreateRole(
        val contractType: String,
        val templateId: String,
        val isBothSides: Boolean,
        val selectedRoleId: String? = null,
        val fromEdit: Boolean = false
    ) : ContractsNavigationState()
    
    /** AI чат для кастомного договору */
    object AiChat : ContractsNavigationState()
}
