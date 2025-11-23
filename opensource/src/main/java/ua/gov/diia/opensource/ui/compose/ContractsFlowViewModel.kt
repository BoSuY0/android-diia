package ua.gov.diia.opensource.ui.compose

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import ua.gov.diia.diia_storage.DiiaStorage
import ua.gov.diia.opensource.data.contracts.storage.ClientIdStorage
import ua.gov.diia.opensource.data.contracts.repo.ContractPartyField
import ua.gov.diia.opensource.data.contracts.repo.ContractPartyRole
import ua.gov.diia.opensource.data.contracts.repo.ContractPartySchema
import ua.gov.diia.opensource.BuildConfig
import ua.gov.diia.opensource.R
import ua.gov.diia.opensource.data.contracts.api.SyncPartyDto
import ua.gov.diia.opensource.data.contracts.api.SyncRequestDto
import ua.gov.diia.opensource.data.contracts.repo.ContractsRepository
import ua.gov.diia.opensource.ui.compose.ContractStatus
import ua.gov.diia.opensource.ui.compose.ContractUiModel
import ua.gov.diia.opensource.ui.compose.HistoryUiModel
import javax.inject.Inject

data class PartyContextFields(
    val roleId: String,
    val personTypeId: String,
    val fields: List<ContractPartyField>
)

@HiltViewModel
class ContractsFlowViewModel @Inject constructor(
    private val repository: ContractsRepository,
    clientIdStorage: ClientIdStorage,
    diiaStorage: DiiaStorage
) : ViewModel() {

    private fun roleForId(roleId: String): ContractPartyRole? =
        _sessionParties.value.firstOrNull { it.id == roleId }

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _partySchema = MutableStateFlow<ContractPartySchema?>(null)
    val partySchema: StateFlow<ContractPartySchema?> = _partySchema.asStateFlow()

    private val _sessionParties = MutableStateFlow<List<ContractPartyRole>>(emptyList())
    val sessionParties: StateFlow<List<ContractPartyRole>> = _sessionParties.asStateFlow()

    private val _contractFields = MutableStateFlow<List<ContractPartyField>>(emptyList())
    val contractFields: StateFlow<List<ContractPartyField>> = _contractFields.asStateFlow()

    private val _mainRole = MutableStateFlow<String?>(null)
    val mainRole: StateFlow<String?> = _mainRole.asStateFlow()
    
    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _fillingMode = MutableStateFlow<String?>("partial")
    val fillingMode: StateFlow<String?> = _fillingMode.asStateFlow()

    private val _partyContextFields = MutableStateFlow<PartyContextFields?>(null)
    val partyContextFields: StateFlow<PartyContextFields?> = _partyContextFields.asStateFlow()

    private val _contracts = MutableStateFlow<List<ContractUiModel>>(emptyList())
    val contracts: StateFlow<List<ContractUiModel>> = _contracts.asStateFlow()

    private val _previewHtml = MutableStateFlow<String?>(null)
    val previewHtml: StateFlow<String?> = _previewHtml.asStateFlow()

    private val _isPreviewLoading = MutableStateFlow(false)
    val isPreviewLoading: StateFlow<Boolean> = _isPreviewLoading.asStateFlow()

    private var chatMessageCounter = 0L
    private val _chatMessages = MutableStateFlow<List<ContractChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ContractChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private var selectedCategoryId: String? = null
    private var selectedTemplateId: String? = null
    private var categoryBoundSessionId: String? = null
    private var templateBoundSessionId: String? = null

    val clientId: String = clientIdStorage.get()
    private val userId: String = diiaStorage.getMobileUuid()

    init {
        _chatMessages.value = listOf(initialGreetingMessage())
    }

    fun clearError() {
        _error.value = null
    }

    fun clearPreview() {
        _previewHtml.value = null
    }

    suspend fun ensureSession(withLoading: Boolean = true): String {
        if (withLoading) _isLoading.value = true
        return runCatching {
            _sessionId.value ?: repository.createSession(userId = userId, clientId = clientId).also {
                _sessionId.value = it
                categoryBoundSessionId = null
                templateBoundSessionId = null
            }
        }.onFailure { _error.value = it.message }
            .also { if (withLoading) _isLoading.value = false }
            .getOrNull().orEmpty()
    }

    suspend fun selectCategory(categoryId: String) {
        val session = ensureSession()
        if (session.isEmpty()) return
        selectedCategoryId = categoryId
        selectedTemplateId = null
        categoryBoundSessionId = null
        templateBoundSessionId = null
        _isLoading.value = true
        runCatching {
            repository.setCategory(session, categoryId, clientId)
            categoryBoundSessionId = session
            loadPartySchema(categoryId)
            refreshSessionSchema(withLoading = false)
        }
            .onFailure { _error.value = it.message }
        _isLoading.value = false
    }

    suspend fun selectTemplate(templateId: String) {
        val session = _sessionId.value ?: ensureSession()
        if (session.isEmpty()) return
        selectedTemplateId = templateId
        templateBoundSessionId = null
        _isLoading.value = true
        runCatching {
            repository.setTemplate(session, templateId, clientId)
            templateBoundSessionId = session
        }
            .onFailure { _error.value = it.message }
        _isLoading.value = false
    }

    suspend fun setFillingMode(isBothSides: Boolean) {
        val session = _sessionId.value ?: ensureSession()
        if (session.isEmpty()) return
        val mode = if (isBothSides) "full" else "partial"
        _isLoading.value = true
        runCatching { repository.setFillingMode(session, mode, clientId) }
            .onFailure { _error.value = it.message }
        _isLoading.value = false
        _fillingMode.value = mode
    }

    suspend fun saveRoleData(
        roleId: String,
        personTypeId: String,
        fields: Map<String, String>,
    ): ContractUiModel? {
        val session = _sessionId.value ?: ensureSession()
        if (session.isEmpty()) return null
        val contextApplied = applySessionContext(session)
        if (!contextApplied) return null
        val role = roleForId(roleId)
        if (role?.claimedBy != null && role.claimedBy != clientId) {
            _error.value = "Роль уже зайнята іншою стороною"
            return null
        }
        _isLoading.value = true
        val result = runCatching {
            val shouldBindContext = role?.claimedBy == null || role.claimedBy == clientId && role.personType != personTypeId
            if (shouldBindContext) {
                repository.setPartyContext(session, roleId, personTypeId, clientId, userId)
            }
            refreshSessionSchema(withLoading = false)
            val contractKeys = _contractFields.value.map { it.key }.toSet()
            val contractData = fields.filterKeys { contractKeys.contains(it) }
            val roleFields = fields.filterKeys { !contractKeys.contains(it) }
            val normalizedRoleFields = roleFields.mapKeys { (key, _) -> normalizeRoleFieldKey(roleId, key) }
            val missingContract = _contractFields.value
                .filter { it.required && contractData[it.key].isNullOrBlank() }
                .map { it.label }
            val mainRoleId = _mainRole.value
            // Only validate contract fields if we know for sure this is the main role
            // If mainRoleId is not loaded yet, assume this is NOT the main role (safer approach)
            val isMainRole = !mainRoleId.isNullOrEmpty() && mainRoleId == roleId
            if (isMainRole) {
                contractData.forEach { (key, value) ->
                    repository.upsertField(
                        sessionId = session,
                        field = key,
                        value = value,
                        roleId = null,
                        clientId = clientId
                    )
                }
                if (missingContract.isNotEmpty()) {
                    throw IllegalStateException("Заповніть обов'язкові умови договору: ${missingContract.joinToString(", ")}")
                }
            }
            if (normalizedRoleFields.isNotEmpty()) {
                repository.sync(
                    sessionId = session,
                    request = SyncRequestDto(
                        category_id = selectedCategoryId,
                        template_id = selectedTemplateId,
                        parties = mapOf(
                            roleId to SyncPartyDto(
                                person_type = personTypeId,
                                fields = normalizedRoleFields
                            )
                        )
                    ),
                    clientId = clientId
                )
            }
            if (!isMainRole) {
                // Refresh session list to get updated status/time from backend or just update locally
                val (subtitle, status) = mapStatusLabel(null)
                val draft = ContractUiModel(
                    id = session,
                    title = "Договір",
                    subtitle = subtitle,
                    status = status,
                    lastUpdated = "", // TODO: Use current time or fetch from backend
                    iconRes = ua.gov.diia.ui_base.R.drawable.ic_doc_cert,
                    isFilled = true, // Assume filled if sync succeeded
                    isSigned = false,
                    history = emptyList()
                )
                _contracts.value = if (_contracts.value.any { it.id == session }) {
                    _contracts.value.map { if (it.id == session) draft else it }
                } else {
                    _contracts.value + draft
                }
                return@runCatching draft
            }
            repository.order(session, clientId)
            val contract = repository.contract(session, clientId)
            val history = runCatching { repository.fetchHistory(session, clientId) }
                .onFailure { _error.value = it.message }
                .getOrDefault(emptyList())
            val lastUpdated = history.firstOrNull()?.date.orEmpty()
            val updatedContract = contract.toUiModel(sessionId = session, history = history, lastUpdated = lastUpdated)
            _contracts.value = if (_contracts.value.any { it.id == session }) {
                _contracts.value.map { current ->
                    if (current.id == session) updatedContract else current
                }
            } else {
                _contracts.value + updatedContract
            }
            updatedContract
        }.onFailure { _error.value = it.message }
            .getOrNull()
        _isLoading.value = false
        return result
    }

    suspend fun updatePartyContext(roleId: String, personTypeId: String) {
        val session = _sessionId.value ?: ensureSession()
        if (session.isEmpty()) return
        val role = roleForId(roleId)
        if (role?.claimedBy != null && role.claimedBy != clientId) {
            _error.value = "Роль уже зайнята іншою стороною"
            return
        }
        _isLoading.value = true
        runCatching {
            val shouldBindContext = role?.claimedBy == null || role.claimedBy == clientId && role.personType != personTypeId
            if (shouldBindContext) {
                repository.setPartyContext(session, roleId, personTypeId, clientId, userId)
            }
            val fields = repository.fetchPartyFields(session, clientId)
            _partyContextFields.value = PartyContextFields(
                roleId = roleId,
                personTypeId = personTypeId,
                fields = fields
            )
            refreshSessionSchema(withLoading = false)
        }.onFailure { _error.value = it.message }
        _isLoading.value = false
    }

    suspend fun refreshSessionSchema(
        scope: String = "all",
        dataMode: String = "status",
        withLoading: Boolean = true
    ) {
        val session = _sessionId.value ?: ensureSession()
        if (session.isEmpty()) return
        val contextApplied = applySessionContext(session)
        if (!contextApplied) return
        if (withLoading) _isLoading.value = true
        runCatching {
            repository.fetchSessionSchema(session, scope, dataMode, clientId)
        }.onSuccess { schema ->
            val baseRoles = _partySchema.value?.roles?.associateBy { it.id }.orEmpty()
            val updated = schema.parties.map { role ->
                val base = baseRoles[role.id]
                role.copy(
                    label = role.label.ifEmpty { base?.label.orEmpty() },
                    allowedPersonTypes = if (role.allowedPersonTypes.isEmpty()) {
                        base?.allowedPersonTypes ?: emptyList()
                    } else {
                        role.allowedPersonTypes
                    }
                )
            }
            if (updated.isNotEmpty()) {
                _sessionParties.value = updated
                val myRole = updated.firstOrNull { it.claimedBy == clientId }?.id
                if (myRole != null) _currentUserRole.value = myRole
            }
            if (schema.contractFields.isNotEmpty()) {
                _contractFields.value = schema.contractFields
            }
            _mainRole.value = schema.mainRole ?: _mainRole.value ?: _partySchema.value?.mainRole
            _fillingMode.value = schema.fillingMode ?: _fillingMode.value
        }.onFailure { _error.value = it.message }
        if (withLoading) _isLoading.value = false
    }

    private suspend fun loadPartySchema(categoryId: String) {
        runCatching { repository.fetchCategoryPartySchema(categoryId) }
            .onSuccess { schema ->
                _partySchema.value = schema
                if (schema.roles.isNotEmpty()) {
                    _sessionParties.value = schema.roles
                }
                if (!schema.mainRole.isNullOrEmpty()) {
                    _mainRole.value = schema.mainRole
                }
            }
            .onFailure { _error.value = it.message }
    }

    suspend fun loadMySessions() {
        _isLoading.value = true
        runCatching {
            val clientSessions = repository.fetchMySessions(clientId)
            val userSessions = runCatching { repository.fetchUserSessions(userId) }
                .getOrDefault(emptyList())
            (clientSessions + userSessions).distinctBy { it.id }
        }
            .onSuccess { _contracts.value = it }
            .onFailure { _error.value = it.message }
        _isLoading.value = false
    }

    suspend fun startEditingSession(sessionId: String) {
        _isLoading.value = true
        _sessionId.value = sessionId
        selectedCategoryId = null
        selectedTemplateId = null
        categoryBoundSessionId = sessionId
        templateBoundSessionId = sessionId
        _partyContextFields.value = null
        runCatching {
            refreshSessionSchema(scope = "all", dataMode = "values", withLoading = false)
        }.onFailure { _error.value = it.message }
        _isLoading.value = false
    }

    suspend fun fetchContractHistory(sessionId: String): List<HistoryUiModel> {
        return runCatching { repository.fetchHistory(sessionId, clientId) }
            .onFailure { _error.value = it.message }
            .getOrDefault(emptyList())
    }

    suspend fun signContract(sessionId: String, currentContract: ContractUiModel? = null): ContractUiModel? {
        _isLoading.value = true
        val result = runCatching {
            repository.signContract(sessionId, clientId)
            val updatedContractDto = repository.contract(sessionId, clientId)
            val history = runCatching { repository.fetchHistory(sessionId, clientId) }
                .onFailure { _error.value = it.message }
                .getOrDefault(emptyList())
            val lastUpdated = history.firstOrNull()?.date.orEmpty()
            val updatedContract = updatedContractDto.toUiModel(sessionId, history = history, lastUpdated = lastUpdated)
            val mergedContract = currentContract?.copy(
                subtitle = updatedContract.subtitle,
                status = updatedContract.status,
                isSigned = updatedContract.isSigned,
                history = history,
                lastUpdated = updatedContract.lastUpdated
            ) ?: updatedContract.copy(history = history)
            _contracts.value = _contracts.value.map { contract ->
                if (contract.id == sessionId) {
                    contract.copy(
                        subtitle = mergedContract.subtitle,
                        status = mergedContract.status,
                        isSigned = mergedContract.isSigned
                    )
                } else {
                    contract
                }
            }
            mergedContract
        }.onFailure { _error.value = it.message }
            .getOrNull()
        _isLoading.value = false
        return result
    }

    suspend fun loadContractPreview(sessionId: String) {
        _isPreviewLoading.value = true
        runCatching { repository.fetchContractPreviewHtml(sessionId, clientId) }
            .onSuccess { _previewHtml.value = it }
            .onFailure { _error.value = it.message }
        _isPreviewLoading.value = false
    }

    suspend fun sendChatMessage(message: String) {
        val text = message.trim()
        if (text.isEmpty()) return
        val session = _sessionId.value ?: ensureSession(withLoading = false)
        if (session.isEmpty()) {
            _error.value = _error.value ?: "Не вдалося створити сесію"
            return
        }
        val userMessage = ContractChatMessage(
            id = nextChatMessageId(),
            isUser = true,
            text = text
        )
        val typingMessage = ContractChatMessage(
            id = nextChatMessageId(),
            isUser = false,
            text = "",
            isTyping = true
        )
        _chatMessages.value = _chatMessages.value + userMessage + typingMessage
        _isChatSending.value = true
        try {
            val response = runCatching { repository.chat(session, text, clientId) }
                .onFailure { _error.value = it.message }
                .getOrNull()
            response?.sessionId?.let { _sessionId.value = it }
            val replyText = response?.reply?.ifBlank { null } ?: "Не вдалося отримати відповідь. Спробуйте ще раз."
            val updatedMessages = _chatMessages.value.toMutableList()
            val typingIndex = updatedMessages.indexOfFirst { it.id == typingMessage.id }
            if (typingIndex >= 0) {
                updatedMessages[typingIndex] = updatedMessages[typingIndex].copy(
                    isTyping = false,
                    text = replyText
                )
            } else {
                updatedMessages.add(
                    ContractChatMessage(
                        id = nextChatMessageId(),
                        isUser = false,
                        text = replyText
                    )
                )
            }
            _chatMessages.value = updatedMessages
        } finally {
            _isChatSending.value = false
        }
    }

    fun resetChat() {
        chatMessageCounter = 0
        _chatMessages.value = listOf(initialGreetingMessage())
    }

    private fun buildContractDeepLink(sessionId: String): Uri =
        Uri.Builder()
            .scheme("https")
            .encodedAuthority("diia.app")
            .appendPath("contract")
            .appendPath(sessionId)
            .build()

    suspend fun shareContractLink(context: Context, sessionId: String) {
        if (sessionId.isBlank()) {
            _error.value = _error.value ?: context.getString(R.string.contracts_share_error)
            return
        }
        val deepLink = buildContractDeepLink(sessionId).toString()
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.contracts_share_message, deepLink))
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    suspend fun joinSessionFromDeepLink(sessionId: String): ContractUiModel? {
        if (sessionId.isBlank()) {
            _error.value = "Некоректне посилання на договір"
            return null
        }
        _isLoading.value = true
        val result = runCatching {
            selectedCategoryId = null
            selectedTemplateId = null
            val joinedSession = repository.createSession(optionalSessionId = sessionId, userId = userId, clientId = clientId)
            if (joinedSession.isBlank()) {
                throw IllegalStateException("Не вдалося приєднатися до договору")
            }
            _sessionId.value = joinedSession
            categoryBoundSessionId = joinedSession
            templateBoundSessionId = joinedSession
            _partyContextFields.value = null
            refreshSessionSchema(scope = "all", dataMode = "values", withLoading = false)
            val history = runCatching { repository.fetchHistory(joinedSession, clientId) }
                .getOrDefault(emptyList())
            val lastUpdated = history.firstOrNull()?.date.orEmpty()
            val contract = repository.contract(joinedSession, clientId)
                .toUiModel(sessionId = joinedSession, history = history, lastUpdated = lastUpdated)
            _contracts.value = listOf(contract) + _contracts.value.filterNot { it.id == contract.id }
            contract
        }.onFailure { _error.value = it.message }
            .getOrNull()
        _isLoading.value = false
        return result
    }

    suspend fun downloadContract(context: Context, sessionId: String, final: Boolean = true) {
        // Отримуємо актуальну інформацію про контракт, щоб мати свіжий download_url
        val info = runCatching { repository.contract(sessionId, clientId) }
            .onFailure { _error.value = it.message }
            .getOrNull()
        val rawUrl = info?.document_url
        if (rawUrl.isNullOrBlank()) {
            _error.value = "Посилання на завантаження недоступне"
            return
        }
        val base = (BuildConfig.CONTRACTS_BASE_URL.takeIf { it.isNotBlank() }
            ?: BuildConfig.SERVER_URL).trimEnd('/')
        val url = if (rawUrl.startsWith("http")) rawUrl else "$base$rawUrl"
        
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }

    private fun initialGreetingMessage(): ContractChatMessage =
        ContractChatMessage(
            id = nextChatMessageId(),
            isUser = false,
            text = "Привіт! Я AI-агент, який допоможе створити кастомний договір. Опишіть свою ситуацію або оберіть один із запитів нижче."
        )

    private fun nextChatMessageId(): Long = ++chatMessageCounter

    private suspend fun applySessionContext(session: String): Boolean {
        selectedCategoryId?.let { categoryId ->
            if (categoryBoundSessionId != session) {
                val applied = runCatching { repository.setCategory(session, categoryId, clientId) }
                    .onSuccess { categoryBoundSessionId = session }
                    .onFailure { _error.value = it.message }
                    .isSuccess
                if (!applied) return false
            }
        }
        selectedTemplateId?.let { templateId ->
            if (templateBoundSessionId != session) {
                val applied = runCatching { repository.setTemplate(session, templateId, clientId) }
                    .onSuccess { templateBoundSessionId = session }
                    .onFailure { _error.value = it.message }
                    .isSuccess
                if (!applied) return false
            }
        }
        return true
    }

    private fun normalizeRoleFieldKey(roleId: String, key: String): String {
        return when {
            key.startsWith("$roleId.") -> key.removePrefix("$roleId.")
            key.contains(".") -> key.substringAfterLast(".")
            else -> key
        }
    }
}

private fun ua.gov.diia.opensource.data.contracts.api.ContractResponseDto.toUiModel(
    sessionId: String,
    history: List<HistoryUiModel> = emptyList(),
    lastUpdated: String = ""
): ContractUiModel {
    val signed = is_signed == true
    val (subtitle, contractStatus) = mapStatusLabel(status)
    val title = "Договір"
    return ContractUiModel(
        id = sessionId,
        title = title,
        subtitle = subtitle,
        status = contractStatus,
        lastUpdated = lastUpdated,
        iconRes = ua.gov.diia.ui_base.R.drawable.ic_doc_cert,
        isFilled = true,
        isSigned = signed,
        history = history
    )
}

private fun mapStatusLabel(raw: String?): Pair<String, ContractStatus> {
    return when (raw?.lowercase()) {
        "ready_to_sign", "ready", "pending_signature" -> "Статус: Очікує підписання" to ContractStatus.PENDING_SIGNATURE
        "completed", "signed", "active" -> "Статус: Активний" to ContractStatus.ACTIVE
        else -> "Статус: Чернетка" to ContractStatus.DRAFT
    }
}
