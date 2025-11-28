package ua.gov.diia.opensource.ui.compose

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import ua.gov.diia.diia_storage.DiiaStorage
import ua.gov.diia.diia_storage.model.PreferenceKey
import ua.gov.diia.diia_storage.store.Preferences
import ua.gov.diia.opensource.BuildConfig
import ua.gov.diia.opensource.R
import ua.gov.diia.opensource.data.contracts.api.SessionDetailsDto
import ua.gov.diia.opensource.data.contracts.api.SessionSummaryDto
import ua.gov.diia.opensource.data.contracts.api.SyncPartyDto
import ua.gov.diia.opensource.data.contracts.api.SyncRequestDto
import ua.gov.diia.opensource.data.contracts.repo.ContractPartyField
import ua.gov.diia.opensource.data.contracts.repo.ContractPartyRole
import ua.gov.diia.opensource.data.contracts.repo.ContractPartySchema
import ua.gov.diia.opensource.data.contracts.repo.ContractsRepository
import ua.gov.diia.opensource.data.contracts.storage.ClientIdStorage
import javax.inject.Inject

data class PartyContextFields(
    val roleId: String,
    val personTypeId: String,
    val fields: List<ContractPartyField>
)

/**
 * Режим заповнення договору.
 */
enum class FillingMode(val apiValue: String) {
    /** Заповнює тільки свою частину */
    PARTIAL("partial"),
    /** Заповнює за обидві сторони */
    FULL("full");

    companion object {
        fun fromApiValue(value: String?): FillingMode = when (value?.lowercase()) {
            "full", "both" -> FULL
            else -> PARTIAL
        }
    }
}

@HiltViewModel
class ContractsFlowViewModel @Inject constructor(
    private val repository: ContractsRepository,
    clientIdStorage: ClientIdStorage,
    private val diiaStorage: DiiaStorage
) : ViewModel() {

    companion object {
        /** PreferenceKey для збереження стану "Більше не показувати intro" */
        private val PREF_SKIP_CONTRACTS_INTRO = object : PreferenceKey(
            "skip_contracts_intro",
            Preferences.Scopes.USER_PREFERENCES,
            Boolean::class.java
        ) {}
    }

    /**
     * Перевіряє чи потрібно показувати intro екран
     */
    fun shouldSkipIntro(): Boolean {
        return diiaStorage.getBoolean(PREF_SKIP_CONTRACTS_INTRO, false)
    }

    /**
     * Зберігає стан "Більше не показувати" для intro екрану
     */
    fun setSkipIntro(skip: Boolean) {
        diiaStorage.set(PREF_SKIP_CONTRACTS_INTRO, skip)
    }

    private fun roleForId(roleId: String): ContractPartyRole? =
        _sessionParties.value.firstOrNull { it.id == roleId }

    /**
     * Парсить помилку API та витягує детальне повідомлення
     */
    private fun parseApiError(e: Throwable): String {
        return when (e) {
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val json = JSONObject(errorBody ?: "{}")
                    // Спочатку пробуємо detail.message (FastAPI формат)
                    json.optJSONObject("detail")?.optString("message")?.takeIf { it.isNotEmpty() }
                        // Потім detail як рядок
                        ?: json.optString("detail")?.takeIf { it.isNotEmpty() && it != "null" }
                        // Потім просто message
                        ?: json.optString("message")?.takeIf { it.isNotEmpty() && it != "null" }
                        // Якщо нічого немає - показуємо код помилки
                        ?: "Помилка сервера: ${e.code()}"
                } catch (_: Exception) {
                    "Помилка сервера: ${e.code()}"
                }
            }
            else -> e.message ?: "Невідома помилка"
        }
    }

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

    private val _fillingMode = MutableStateFlow(FillingMode.PARTIAL)
    val fillingMode: StateFlow<FillingMode> = _fillingMode.asStateFlow()

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

    private var _selectedCategoryId: String? = null
    val selectedCategoryId: String? get() = _selectedCategoryId
    
    private var _selectedTemplateId: String? = null
    val selectedTemplateId: String? get() = _selectedTemplateId
    private var categoryBoundSessionId: String? = null
    private var templateBoundSessionId: String? = null

    private val _clientId: String = clientIdStorage.get()
    private val userId: String = diiaStorage.getMobileUuid()
    
    // Для UI використовуємо userId, бо бекенд зберігає claimedBy як userId
    val clientId: String get() = userId

    init {
        _chatMessages.value = listOf(initialGreetingMessage())
    }

    fun clearError() {
        _error.value = null
    }

    fun clearPreview() {
        _previewHtml.value = null
    }

    /**
     * Створює сесію з усіма параметрами (відкладене створення).
     * Викликати лише коли потрібно зберегти дані.
     */
    private suspend fun createSessionWithParams(
        roleId: String,
        personTypeId: String,
        withLoading: Boolean = true
    ): String {
        if (withLoading) _isLoading.value = true
        return runCatching {
            repository.createSession(
                creatorUserId = userId,
                categoryId = selectedCategoryId,
                templateId = selectedTemplateId,
                fillingMode = _fillingMode.value.apiValue,
                role = roleId,
                personType = personTypeId,
                userId = userId  // Використовуємо userId для всіх запитів - роль прив'язана до нього
            ).also {
                _sessionId.value = it
                categoryBoundSessionId = it
                templateBoundSessionId = it
                _currentUserRole.value = roleId
            }
        }.onFailure { _error.value = parseApiError(it) }
            .also { if (withLoading) _isLoading.value = false }
            .getOrNull().orEmpty()
    }

    /**
     * Створює пусту сесію (для AI чату або deep link).
     */
    suspend fun ensureSession(withLoading: Boolean = true): String {
        if (_sessionId.value != null) return _sessionId.value!!
        if (withLoading) _isLoading.value = true
        return runCatching {
            repository.createSession(creatorUserId = userId, userId = userId).also {  // Узгоджуємо header з body
                _sessionId.value = it
                categoryBoundSessionId = null
                templateBoundSessionId = null
            }
        }.onFailure { _error.value = parseApiError(it) }
            .also { if (withLoading) _isLoading.value = false }
            .getOrNull().orEmpty()
    }

    /**
     * Вибір категорії - зберігає локально, завантажує схему без створення сесії.
     */
    suspend fun selectCategory(categoryId: String) {
        _selectedCategoryId = categoryId
        _selectedTemplateId = null
        _sessionId.value = null  // Скидаємо стару сесію
        categoryBoundSessionId = null
        templateBoundSessionId = null
        _currentUserRole.value = null
        _isLoading.value = true
        runCatching {
            loadCategorySchema(categoryId)  // Завантажуємо схему без сесії
        }.onFailure { _error.value = parseApiError(it) }
        _isLoading.value = false
    }

    /**
     * Синхронізує контекст (категорію та шаблон) без скидання сесії.
     * Використовується при переході з AI чату, де сесія вже створена та налаштована.
     */
    suspend fun syncSessionContext(categoryId: String, templateId: String?) {
        android.util.Log.d("ContractsFlow", "syncSessionContext: categoryId=$categoryId, templateId=$templateId, currentSessionId=${_sessionId.value}")
        _selectedCategoryId = categoryId
        _selectedTemplateId = templateId
        // Не скидаємо _sessionId, оскільки ми продовжуємо поточну сесію
        
        // Якщо сесія прив'язана до шаблону/категорії на бекенді, оновлюємо локальні прив'язки
        if (_sessionId.value != null) {
            categoryBoundSessionId = _sessionId.value
            if (templateId != null) {
                templateBoundSessionId = _sessionId.value
            } else {
                // Якщо templateId не передано — спробуємо отримати з бекенду
                val sessionId = _sessionId.value!!
                val details = runCatching { repository.fetchSessionDetails(sessionId, userId) }.getOrNull()
                val backendTemplateId = details?.templateId?.takeIf { it.isNotBlank() }
                if (!backendTemplateId.isNullOrBlank()) {
                    _selectedTemplateId = backendTemplateId
                    templateBoundSessionId = sessionId
                    android.util.Log.d("ContractsFlow", "syncSessionContext: got templateId from backend: $backendTemplateId")
                }
            }
        }

        _isLoading.value = true
        runCatching {
            loadCategorySchema(categoryId)
        }.onFailure { _error.value = parseApiError(it) }
        _isLoading.value = false
    }

    /**
     * Вибір шаблону - зберігає локально, без API виклику.
     */
    suspend fun selectTemplate(templateId: String) {
        _selectedTemplateId = templateId
        templateBoundSessionId = null
    }

    /**
     * Вибір режиму заповнення - зберігає локально, без API виклику.
     */
    fun setFillingMode(isBothSides: Boolean) {
        _fillingMode.value = if (isBothSides) FillingMode.FULL else FillingMode.PARTIAL
    }

    suspend fun saveRoleData(
        roleId: String,
        personTypeId: String,
        fields: Map<String, String>,
    ): ContractUiModel? {
        // Якщо сесії ще немає - створюємо з усіма параметрами
        val isNewSession = _sessionId.value == null
        android.util.Log.d("ContractsFlow", "saveRoleData: isNewSession=$isNewSession, _sessionId=${_sessionId.value}, categoryId=$_selectedCategoryId, templateId=$_selectedTemplateId")
        
        // Якщо шаблон не вибрано локально, але сесія існує — спробуємо отримати з бекенду
        if (!isNewSession && _selectedTemplateId.isNullOrBlank()) {
            val existingSession = _sessionId.value!!
            android.util.Log.d("ContractsFlow", "saveRoleData: templateId is null, fetching from backend...")
            val details = runCatching { repository.fetchSessionDetails(existingSession, userId) }.getOrNull()
            val backendTemplateId = details?.templateId?.takeIf { it.isNotBlank() }
            if (!backendTemplateId.isNullOrBlank()) {
                _selectedTemplateId = backendTemplateId
                templateBoundSessionId = existingSession
                android.util.Log.d("ContractsFlow", "saveRoleData: got templateId from backend: $backendTemplateId")
            }
        }
        
        val session = if (isNewSession) {
            createSessionWithParams(roleId, personTypeId, withLoading = false)
        } else {
            _sessionId.value!!
        }
        if (session.isEmpty()) return null
        
        // Перевіряємо роль тільки для існуючої сесії
        if (!isNewSession) {
            val role = roleForId(roleId)
            if (role?.claimedBy != null && role.claimedBy != userId) {
                _error.value = "Роль уже зайнята іншою стороною"
                return null
            }
            
            // Перевіряємо чи користувач вже володіє іншою роллю в цій сесії
            val existingUserRole = _currentUserRole.value
            val isBothSidesMode = _fillingMode.value == FillingMode.FULL
            if (!isBothSidesMode && existingUserRole != null && existingUserRole != roleId) {
                _error.value = "Ви вже вибрали іншу роль в цій сесії. Створіть новий договір для іншої ролі."
                return null
            }
        }
        
        _isLoading.value = true
        val result = runCatching {
            // Для існуючої сесії потрібно встановити контекст ролі
            if (!isNewSession) {
                val role = roleForId(roleId)
                val shouldBindContext = role?.claimedBy == null || (role.claimedBy == userId && role.personType != personTypeId)
                if (shouldBindContext) {
                    repository.setPartyContext(session, roleId, personTypeId, userId = userId, bodyUserId = userId)
                }
            }
            refreshSessionSchema(withLoading = false)
            val contractKeys = _contractFields.value.map { it.key }.toSet()
            val contractData = fields.filterKeys { contractKeys.contains(it) }
            val roleFields = fields.filterKeys { !contractKeys.contains(it) }
            val normalizedRoleFields = roleFields.mapKeys { (key, _) -> normalizeRoleFieldKey(roleId, key) }
            
            // Оптимізація: використовуємо sync API для збереження всіх полів одним запитом
            // замість окремих upsertField для кожного поля (значно швидше)
            val allRoleFields = normalizedRoleFields + contractData
            if (allRoleFields.isNotEmpty()) {
                val syncRequest = SyncRequestDto(
                    category_id = selectedCategoryId,
                    template_id = selectedTemplateId,
                    parties = mapOf(
                        roleId to SyncPartyDto(
                            person_type = personTypeId,
                            fields = allRoleFields
                        )
                    )
                )
                val syncResponse = repository.sync(session, syncRequest, userId)
                
                // Обробляємо помилки валідації з sync response
                // Показуємо локалізоване повідомлення про поля які не пройшли валідацію
                // Спочатку пробуємо новий детальний формат (roles_detailed), потім fallback на roles
                val roleLabels = _sessionParties.value.associate { it.id to it.label }
                
                val errorMessages = if (!syncResponse.missing?.rolesDetailed.isNullOrEmpty()) {
                    // Новий формат: rolesDetailed містить label напряму
                    syncResponse.missing?.rolesDetailed?.entries
                        ?.filter { it.value.missingFields.isNotEmpty() }
                        ?.map { (role, data) ->
                            val roleLabel = data.roleLabel ?: roleLabels[role] ?: role
                            val fieldLabels = data.missingFields.map { field ->
                                field.label ?: field.key ?: field.field ?: "?"
                            }
                            "$roleLabel: ${fieldLabels.joinToString(", ")}"
                        }
                        .orEmpty()
                } else {
                    // Старий формат: roles містить RequirementsMissingFieldDto з label
                    val missingRoles = syncResponse.missing?.roles.orEmpty()
                    if (missingRoles.isNotEmpty()) {
                        // Оновлюємо схему щоб отримати labels для fallback
                        refreshSessionSchema(withLoading = false)
                        // Формуємо мапу полів для fallback пошуку
                        val allFields = mutableMapOf<String, String>()
                        _sessionParties.value.forEach { role -> 
                            role.fields.forEach { field ->
                                val simpleKey = if (field.key.startsWith("${role.id}.")) {
                                    field.key.removePrefix("${role.id}.")
                                } else {
                                    field.key
                                }
                                allFields[simpleKey] = field.label
                                allFields["${role.id}.$simpleKey"] = field.label
                                allFields[field.key] = field.label
                            }
                        }
                        _contractFields.value.forEach { field ->
                            allFields[field.key] = field.label
                        }
                        
                        missingRoles.entries
                            .filter { it.value.missingFields.isNotEmpty() }
                            .map { (role, data) ->
                                // Використовуємо roleLabel з відповіді бекенду, якщо є
                                val roleLabel = data.roleLabel ?: roleLabels[role] ?: role
                                val fieldLabels = data.missingFields.map { field ->
                                    // Якщо є помилка валідації, показуємо її
                                    val errorSuffix = field.error?.let { " ($it)" } ?: ""
                                    val label = field.label 
                                        ?: field.key?.let { key -> allFields[key] ?: allFields["$role.$key"] }
                                        ?: field.field?.let { f -> allFields[f] ?: allFields["$role.$f"] }
                                        ?: field.key 
                                        ?: field.field 
                                        ?: "?"
                                    "$label$errorSuffix"
                                }
                                "$roleLabel: ${fieldLabels.joinToString(", ")}"
                            }
                    } else {
                        emptyList()
                    }
                }
                
                if (errorMessages.isNotEmpty()) {
                    _error.value = "Не вдалося зберегти поля: ${errorMessages.joinToString("; ")}"
                }
            }
            // Після збереження полів отримуємо поточний статус контракту (без виклику order)
            // order викликається окремо, коли ВСІ обов'язкові поля заповнені
            val contract = repository.contract(session, userId)
            val history = runCatching { repository.fetchHistory(session, userId) }
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
        }.onFailure { _error.value = parseApiError(it) }
            .getOrNull()
        _isLoading.value = false
        return result
    }

    /**
     * Перевіряє готовність контракту та будує документ якщо всі поля заповнені.
     * Викликати після того, як всі ролі заповнили свої дані.
     * @return ContractUiModel якщо документ успішно побудовано, null якщо не готовий або помилка
     */
    suspend fun buildContract(): ContractUiModel? {
        val session = _sessionId.value ?: return null
        _isLoading.value = true
        val result = runCatching {
            // Перевіряємо готовність через /requirements
            val requirements = repository.fetchRequirements(session, userId)
            
            // ОБОВ'ЯЗКОВО перевіряємо готовність УСІХ сторін (isReadyAll)
            // canBuildContract НЕ є достатньою умовою - він може бути true коли тільки одна сторона заповнила
            val isReadyAll = requirements.isReadyAll ?: requirements.isReady
            val isReadySelf = requirements.isReadySelf ?: requirements.isReady
            
            if (isReadyAll != true) {
                // Якщо своя частина готова, але інші сторони ні - показуємо спеціальне повідомлення
                if (isReadySelf == true) {
                    throw IllegalStateException("Ваша частина заповнена, але інші сторони ще не заповнили свої дані")
                }
                // Контракт ще не готовий - показуємо що саме відсутнє
                // Локалізуємо назви ролей та полів
                val roleLabels = _sessionParties.value.associate { it.id to it.label }
                val allFields = _sessionParties.value.flatMap { role -> role.fields.map { "${role.id}.${it.key}" to it.label } }.toMap() +
                    _contractFields.value.associate { it.key to it.label }
                
                // Спочатку пробуємо новий детальний формат (roles_detailed)
                val missingRoles = if (!requirements.missing?.rolesDetailed.isNullOrEmpty()) {
                    requirements.missing?.rolesDetailed?.entries
                        ?.filter { it.value.missingFields.isNotEmpty() }
                        ?.map { (role, data) ->
                            // Використовуємо role_label з бекенду або fallback на локальні labels
                            val roleLabel = data.roleLabel ?: roleLabels[role] ?: role
                            val fieldLabels = data.missingFields.map { field ->
                                field.label ?: field.key ?: field.field ?: "?"
                            }
                            "$roleLabel: ${fieldLabels.joinToString(", ")}"
                        }
                        .orEmpty()
                } else {
                    // Fallback на старий формат (roles)
                    requirements.missing?.roles?.entries
                        ?.filter { it.value.isNotEmpty() }
                        ?.map { (role, fields) ->
                            val roleLabel = roleLabels[role] ?: role
                            val fieldLabels = fields.map { field ->
                                field.label ?: field.key ?: field.field 
                                    ?: allFields["$role.${field.key}"] 
                                    ?: field.key 
                                    ?: "?"
                            }
                            "$roleLabel: ${fieldLabels.joinToString(", ")}"
                        }
                        .orEmpty()
                }
                val missingContract = requirements.missing?.contract?.map { fieldKey ->
                    allFields[fieldKey] ?: fieldKey
                }.orEmpty()
                val allMissing = (missingContract + missingRoles).filter { it.isNotBlank() }
                if (allMissing.isNotEmpty()) {
                    throw IllegalStateException("Не заповнені поля: ${allMissing.joinToString("; ")}")
                }
                throw IllegalStateException("Не всі сторони заповнили свої дані")
            }
            // Все готово - викликаємо order для побудови документа
            repository.order(session, userId)
            // Отримуємо оновлений статус контракту
            val contract = repository.contract(session, userId)
            val history = runCatching { repository.fetchHistory(session, userId) }
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
        }.onFailure { _error.value = parseApiError(it) }
            .getOrNull()
        _isLoading.value = false
        return result
    }

    /**
     * Перевіряє готовність контракту без побудови документа.
     * @return true якщо всі обов'язкові поля УСІХ сторін заповнені
     */
    suspend fun checkContractReady(): Boolean {
        val session = _sessionId.value ?: return false
        return runCatching {
            val requirements = repository.fetchRequirements(session, userId)
            // Перевіряємо ТІЛЬКИ isReadyAll - canBuildContract може бути true навіть коли не всі готові
            val isReadyAll = requirements.isReadyAll ?: requirements.isReady
            isReadyAll == true
        }.getOrDefault(false)
    }
    
    /**
     * Перевіряє готовність тільки своєї частини контракту.
     * @return Pair(isReadySelf, isReadyAll) - готовність своєї частини і всіх сторін
     */
    suspend fun checkReadinessStatus(): Pair<Boolean, Boolean> {
        val session = _sessionId.value ?: return Pair(false, false)
        return runCatching {
            val requirements = repository.fetchRequirements(session, userId)
            val isReadySelf = requirements.isReadySelf ?: requirements.isReady ?: false
            val isReadyAll = requirements.isReadyAll ?: requirements.isReady ?: false
            Pair(isReadySelf, isReadyAll)
        }.getOrDefault(Pair(false, false))
    }

    suspend fun updatePartyContext(roleId: String, personTypeId: String) {
        // Для нових договорів (без сесії) - просто оновлюємо локальний контекст
        // Поля беремо зі схеми категорії
        val existingSession = _sessionId.value
        if (existingSession == null) {
            // Немає сесії - використовуємо поля з partySchema
            val schema = _partySchema.value
            val personType = schema?.personTypes?.firstOrNull { it.id == personTypeId }
            val fields = personType?.fields ?: emptyList()
            _partyContextFields.value = PartyContextFields(
                roleId = roleId,
                personTypeId = personTypeId,
                fields = fields
            )
            return
        }
        
        // Для існуючої сесії - синхронізуємо з сервером
        val role = roleForId(roleId)
        if (role?.claimedBy != null && role.claimedBy != userId) {
            _error.value = "Роль уже зайнята іншою стороною"
            return
        }
        _isLoading.value = true
        runCatching {
            val shouldBindContext = role?.claimedBy == null || role.claimedBy == userId && role.personType != personTypeId
            if (shouldBindContext) {
                repository.setPartyContext(existingSession, roleId, personTypeId, userId = userId, bodyUserId = userId)
            }
            // Спочатку завантажуємо party-fields для структури полів
            val baseFields = repository.fetchPartyFields(existingSession, userId)
            // Встановлюємо базову структуру
            _partyContextFields.value = PartyContextFields(
                roleId = roleId,
                personTypeId = personTypeId,
                fields = baseFields
            )
            // Потім завантажуємо схему з values - це оновить partyContextFields зі збереженими значеннями
            refreshSessionSchema(dataMode = "values", withLoading = false)
        }.onFailure { _error.value = parseApiError(it) }
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
        if (!contextApplied) {
            if (withLoading) _isLoading.value = false
            return
        }
        if (withLoading) _isLoading.value = true
        runCatching {
            repository.fetchSessionSchema(session, scope, dataMode, userId)
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
                val myRole = updated.firstOrNull { it.claimedBy == userId }?.id
                if (myRole != null) _currentUserRole.value = myRole
                
                // Оновлюємо partyContextFields зі значеннями полів з schema (dataMode = "values")
                val currentContext = _partyContextFields.value
                if (currentContext != null) {
                    val roleFromSchema = updated.firstOrNull { it.id == currentContext.roleId }
                    if (roleFromSchema != null && roleFromSchema.fields.isNotEmpty()) {
                        val schemaValues = roleFromSchema.fields.associate { it.key to it.value }
                        val updatedFields = currentContext.fields.map { field ->
                            val schemaValue = schemaValues[field.key]
                            if (!schemaValue.isNullOrEmpty()) field.copy(value = schemaValue) else field
                        }
                        _partyContextFields.value = currentContext.copy(fields = updatedFields)
                    }
                }
            } else if (_partySchema.value?.roles?.isNotEmpty() == true) {
                // Fallback: якщо бекенд не повернув ролі, використовуємо схему категорії
                _sessionParties.value = _partySchema.value?.roles.orEmpty()
            } else if (!selectedCategoryId.isNullOrBlank()) {
                // Спробуємо підтягнути схему категорії динамічно
                runCatching { repository.fetchCategoryPartySchema(selectedCategoryId!!) }
                    .onSuccess { categorySchema ->
                        _partySchema.value = categorySchema
                        if (categorySchema.roles.isNotEmpty()) {
                            _sessionParties.value = categorySchema.roles
                            _mainRole.value = _mainRole.value ?: categorySchema.mainRole
                        }
                    }
            }
            // Оновлюємо contractFields зі схеми бекенду
            // ВАЖЛИВО: якщо бекенд повернув пустий список - це означає, що користувач
            // НЕ МАЄ права бачити умови договору (наприклад, орендар в partial mode).
            // Fallback з категорії використовуємо ТІЛЬКИ якщо сесії ще не існує.
            _contractFields.value = schema.contractFields
            // Якщо схема сесії не дає personTypes/ролей - підтягнемо схему категорії
            if (_partySchema.value == null && !selectedCategoryId.isNullOrBlank()) {
                runCatching { repository.fetchCategoryPartySchema(selectedCategoryId!!) }
                    .onSuccess { schemaByCategory ->
                        _partySchema.value = schemaByCategory
                        if (schemaByCategory.roles.isNotEmpty() && _sessionParties.value.isEmpty()) {
                            _sessionParties.value = schemaByCategory.roles
                        }
                        if (!schemaByCategory.mainRole.isNullOrEmpty() && _mainRole.value.isNullOrEmpty()) {
                            _mainRole.value = schemaByCategory.mainRole
                        }
                    }
            }
            // Оновлюємо partySchema з personTypes з session schema (важливо для редагування)
            // Це забезпечує наявність personTypes навіть коли редагуємо існуючу сесію
            if (schema.personTypes.isNotEmpty() || _partySchema.value == null) {
                val existingPersonTypes = _partySchema.value?.personTypes.orEmpty()
                val mergedPersonTypes = if (schema.personTypes.isNotEmpty()) {
                    schema.personTypes
                } else {
                    existingPersonTypes
                }
                _partySchema.value = ContractPartySchema(
                    roles = updated.ifEmpty { _partySchema.value?.roles.orEmpty() },
                    personTypes = mergedPersonTypes,
                    mainRole = schema.mainRole ?: _partySchema.value?.mainRole
                )
            }
            _mainRole.value = schema.mainRole ?: _mainRole.value ?: _partySchema.value?.mainRole
            _fillingMode.value = schema.fillingMode?.let { FillingMode.fromApiValue(it) } ?: _fillingMode.value
            if (schema.fillingMode.isNullOrBlank() && _sessionParties.value.isNotEmpty()) {
                val allRolesFree = _sessionParties.value.all { it.claimedBy.isNullOrBlank() }
                if (allRolesFree) {
                    _fillingMode.value = FillingMode.FULL
                }
            }
        }.onFailure { _error.value = parseApiError(it) }
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
            .onFailure { _error.value = parseApiError(it) }
    }

    /**
     * Завантажує схему категорії (ролі, типи персон, контрактні поля) БЕЗ створення сесії.
     * Використовує:
     * - GET /categories/{id}/schema - ролі та person_types
     * - GET /categories/{id}/entities - контрактні поля
     */
    private suspend fun loadCategorySchema(categoryId: String) {
        // Завантажуємо схему ролей
        runCatching { repository.fetchCategorySchema(categoryId) }
            .onSuccess { schema ->
                _partySchema.value = schema
                if (schema.roles.isNotEmpty()) {
                    _sessionParties.value = schema.roles
                }
                if (!schema.mainRole.isNullOrEmpty()) {
                    _mainRole.value = schema.mainRole
                }
            }
            .onFailure { _error.value = parseApiError(it) }
        
        // Завантажуємо контрактні поля (Умови договору)
        runCatching { repository.fetchCategoryEntities(categoryId) }
            .onSuccess { fields ->
                if (fields.isNotEmpty()) {
                    _contractFields.value = fields
                }
            }
            .onFailure { /* Ігноруємо помилку - поля можуть бути опціональними */ }
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
            .onFailure { _error.value = parseApiError(it) }
        _isLoading.value = false
        refreshContractStatusesAsync()
    }

    suspend fun startEditingSession(sessionId: String) {
        _isLoading.value = true
        _sessionId.value = sessionId
        val contractInfo = runCatching { repository.contract(sessionId, userId) }
            .onFailure { _error.value = parseApiError(it) }
            .getOrNull()
        val sessionSummary: SessionSummaryDto? = runCatching { repository.findSessionSummary(sessionId, userId) }.getOrNull()
        val sessionDetails: SessionDetailsDto? = runCatching { repository.fetchSessionDetails(sessionId, userId) }
            .onFailure { _error.value = _error.value ?: parseApiError(it) }
            .getOrNull()
        val templateFromServer = contractInfo?.templateId ?: sessionDetails?.templateId
        val categoryFromServer = contractInfo?.categoryId ?: sessionDetails?.categoryId
        _selectedTemplateId = templateFromServer ?: sessionSummary?.template_id ?: selectedTemplateId
        _selectedCategoryId = categoryFromServer ?: selectedCategoryId

        // Хак для хакатону: якщо шаблон не прийшов, мапимо за назвою
        val titleHint = sessionSummary?.title?.lowercase()
        val isLeaseContract = titleHint?.contains("оренди") == true || titleHint?.contains("lease") == true
        if (selectedTemplateId.isNullOrBlank() && isLeaseContract) {
            _selectedTemplateId = "lease_flat"
        }
        if (selectedCategoryId.isNullOrBlank() && _selectedTemplateId == "lease_flat") {
            _selectedCategoryId = "lease_real_estate"
        }
        if (selectedCategoryId.isNullOrBlank() && !selectedTemplateId.isNullOrBlank()) {
            _selectedCategoryId = runCatching { repository.findCategoryByTemplateId(selectedTemplateId!!) }
                .getOrNull()
        }
        if (selectedCategoryId.isNullOrBlank() && selectedTemplateId?.startsWith("lease_") == true) {
            // Хак для хакатон-сценарію: всі lease_* належать до нерухомості
            _selectedCategoryId = "lease_real_estate"
        }
        categoryBoundSessionId = if (!selectedCategoryId.isNullOrBlank() && categoryFromServer != null) sessionId else null
        templateBoundSessionId = if (!selectedTemplateId.isNullOrBlank() && templateFromServer != null) sessionId else null
        sessionDetails?.fillingMode?.let { _fillingMode.value = FillingMode.fromApiValue(it) }

        val userRoleFromDetails = sessionDetails?.roleOwners?.entries
            ?.firstOrNull { it.value == userId }
            ?.key
        if (userRoleFromDetails != null) {
            _currentUserRole.value = userRoleFromDetails
            val userPersonType = sessionDetails.partyTypes?.get(userRoleFromDetails)
            if (!userPersonType.isNullOrBlank()) {
                val fields = runCatching { repository.fetchPartyFields(sessionId, userId) }
                    .getOrDefault(emptyList())
                _partyContextFields.value = PartyContextFields(
                    roleId = userRoleFromDetails,
                    personTypeId = userPersonType,
                    fields = fields
                )
            }
        } else {
            _partyContextFields.value = null
        }

        selectedCategoryId?.let { loadCategorySchema(it) }
        reloadSchemaWithFallback(sessionId)
        _isLoading.value = false
    }

    suspend fun fetchContractHistory(sessionId: String): List<HistoryUiModel> {
        return runCatching { repository.fetchHistory(sessionId, userId) }
            .onFailure { _error.value = parseApiError(it) }
            .getOrDefault(emptyList())
    }

    suspend fun signContract(sessionId: String, currentContract: ContractUiModel? = null): ContractUiModel? {
        _isLoading.value = true
        val result = runCatching {
            val contractInfo = repository.contract(sessionId, userId)
            // Беремо templateId з локального стейту, контракту або summary (бо /contract може його не віддавати)
            val sessionSummary = runCatching { repository.findSessionSummary(sessionId, userId) }.getOrNull()
            val templateId = listOfNotNull(selectedTemplateId, contractInfo.templateId, sessionSummary?.template_id)
                .firstOrNull { it.isNotBlank() }
            if (templateId.isNullOrBlank()) {
                _error.value = _error.value ?: "Не вказано шаблон договору. Виберіть шаблон і спробуйте ще раз."
                return@runCatching null
            }
            _selectedTemplateId = templateId
            // Якщо бекенд не повернув template_id, зафіксуємо його явно
            if (contractInfo.templateId.isNullOrBlank()) {
                runCatching { repository.setTemplate(sessionId, templateId, userId) }
                    .onFailure { _error.value = _error.value ?: parseApiError(it) }
            }
            repository.buildContract(sessionId, userId, templateId)
            repository.signContract(sessionId, userId)
            val updatedContractDto = repository.contract(sessionId, userId)
            val history = runCatching { repository.fetchHistory(sessionId, userId) }
                .onFailure { _error.value = parseApiError(it) }
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
        }.onFailure { _error.value = parseApiError(it) }
            .getOrNull()
        _isLoading.value = false
        return result
    }

    fun loadContractPreview(sessionId: String) {
        android.util.Log.d("ContractsFlow", "loadContractPreview: sessionId=$sessionId, _sessionId=${_sessionId.value}, templateId=$_selectedTemplateId")
        viewModelScope.launch {
            _isPreviewLoading.value = true
            
            // Якщо templateId не встановлено локально — спробуємо завантажити з бекенду
            if (_selectedTemplateId.isNullOrBlank()) {
                android.util.Log.d("ContractsFlow", "loadContractPreview: templateId is null, fetching from backend...")
                val details = runCatching { repository.fetchSessionDetails(sessionId, userId) }.getOrNull()
                val contractInfo = runCatching { repository.contract(sessionId, userId) }.getOrNull()
                val templateFromBackend = contractInfo?.templateId?.takeIf { it.isNotBlank() }
                    ?: details?.templateId?.takeIf { it.isNotBlank() }
                
                if (!templateFromBackend.isNullOrBlank()) {
                    _selectedTemplateId = templateFromBackend
                    android.util.Log.d("ContractsFlow", "loadContractPreview: got templateId from backend: $templateFromBackend")
                } else {
                    android.util.Log.w("ContractsFlow", "loadContractPreview: template not selected on backend either")
                    _error.value = "Виберіть шаблон договору перед переглядом"
                    _isPreviewLoading.value = false
                    return@launch
                }
            }
            
            runCatching { repository.fetchContractPreviewHtml(sessionId, userId) }
                .onSuccess { _previewHtml.value = it }
                .onFailure { _error.value = parseApiError(it) }
            _isPreviewLoading.value = false
        }
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
            // Передаємо reset=true якщо це перше повідомлення після resetChat()
            val shouldReset = _shouldResetChatOnNextMessage
            _shouldResetChatOnNextMessage = false
            
            val response = runCatching { repository.chat(session, text, userId, reset = shouldReset) }
                .onFailure { _error.value = parseApiError(it) }
                .getOrNull()
            response?.sessionId?.let { _sessionId.value = it }
            val replyText = response?.reply?.ifBlank { null } ?: "Не вдалося отримати відповідь. Спробуйте ще раз."
            // Парсимо actions з відповіді бекенду
            val actions = response?.actions?.map { dto ->
                ChatAction(
                    type = dto.type,
                    label = dto.label,
                    payload = dto.payload.orEmpty()
                )
            }.orEmpty()
            val updatedMessages = _chatMessages.value.toMutableList()
            val typingIndex = updatedMessages.indexOfFirst { it.id == typingMessage.id }
            if (typingIndex >= 0) {
                updatedMessages[typingIndex] = updatedMessages[typingIndex].copy(
                    isTyping = false,
                    text = replyText,
                    actions = actions
                )
            } else {
                updatedMessages.add(
                    ContractChatMessage(
                        id = nextChatMessageId(),
                        isUser = false,
                        text = replyText,
                        actions = actions
                    )
                )
            }
            _chatMessages.value = updatedMessages
        } finally {
            _isChatSending.value = false
        }
    }

    /**
     * Скидає чат: очищає локальні повідомлення і скидає історію на бекенді.
     * Наступне повідомлення буде надіслано з reset=true.
     */
    fun resetChat() {
        chatMessageCounter = 0
        _chatMessages.value = listOf(initialGreetingMessage())
        // Скидаємо історію на бекенді при наступному повідомленні
        _shouldResetChatOnNextMessage = true
    }
    
    private var _shouldResetChatOnNextMessage = false

    private fun buildContractDeepLink(sessionId: String): Uri =
        Uri.Builder()
            .scheme(BuildConfig.DEEP_LINK_SCHEME)
            .encodedAuthority(BuildConfig.DEEP_LINK_HOST)
            .appendPath("contract")
            .appendPath(sessionId)
            .build()

    fun getContractShareLink(context: Context, sessionId: String): String? {
        if (sessionId.isBlank()) {
            _error.value = _error.value ?: context.getString(R.string.contracts_share_error)
            return null
        }
        return buildContractDeepLink(sessionId).toString()
    }

    /**
     * Приєднатися до існуючої сесії через deep-link.
     * Використовує спеціальний ендпоінт /sessions/join замість createSession.
     */
    suspend fun joinSessionFromDeepLink(sessionId: String): ContractUiModel? {
        android.util.Log.d("ContractsFlowVM", "joinSessionFromDeepLink: sessionId=$sessionId, userId=$userId")
        if (sessionId.isBlank()) {
            _error.value = "Некоректне посилання на договір"
            android.util.Log.e("ContractsFlowVM", "joinSessionFromDeepLink: sessionId is blank")
            return null
        }
        _isLoading.value = true
        val result = runCatching {
            // Використовуємо спеціальний ендпоінт join замість createSession
            android.util.Log.d("ContractsFlowVM", "joinSessionFromDeepLink: calling repository.joinSession...")
            val joinResponse = repository.joinSession(sessionId, userId)
            android.util.Log.d("ContractsFlowVM", "joinSessionFromDeepLink: joinResponse=$joinResponse")
            
            // Встановлюємо контекст з відповіді
            _selectedCategoryId = joinResponse.categoryId
            _selectedTemplateId = joinResponse.templateId
            _sessionId.value = joinResponse.sessionId
            categoryBoundSessionId = if (joinResponse.categoryId != null) joinResponse.sessionId else null
            templateBoundSessionId = if (joinResponse.templateId != null) joinResponse.sessionId else null
            _partyContextFields.value = null
            
            // Завантажуємо схему сесії
            refreshSessionSchema(scope = "all", dataMode = "values", withLoading = false)
            
            // Отримуємо контракт та історію
            val history = runCatching { repository.fetchHistory(joinResponse.sessionId, userId) }
                .getOrDefault(emptyList())
            val lastUpdated = history.firstOrNull()?.date.orEmpty()
            val contract = repository.contract(joinResponse.sessionId, userId)
                .toUiModel(sessionId = joinResponse.sessionId, history = history, lastUpdated = lastUpdated)
            _contracts.value = listOf(contract) + _contracts.value.filterNot { it.id == contract.id }
            contract
        }.onFailure { e ->
            // Покращена обробка помилок для deep-link
            android.util.Log.e("ContractsFlowVM", "joinSessionFromDeepLink FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
            val errorMessage = when (e) {
                is HttpException -> {
                    android.util.Log.e("ContractsFlowVM", "joinSessionFromDeepLink: HTTP ${e.code()}")
                    when (e.code()) {
                        404 -> "Договір не знайдено. Можливо, він був видалений або посилання некоректне."
                        403 -> "Ви не маєте доступу до цього договору."
                        401 -> "Необхідна авторизація для доступу до договору."
                        else -> parseApiError(e)
                    }
                }
                else -> parseApiError(e)
            }
            _error.value = errorMessage
        }.getOrNull()
        _isLoading.value = false
        return result
    }

    suspend fun downloadContract(context: Context, sessionId: String, final: Boolean = true) {
        val info = runCatching { repository.contract(sessionId, userId) }
            .onFailure { _error.value = parseApiError(it) }
            .getOrNull()
        val fileName = resolveContractFileName(info?.documentUrl, sessionId)
        val mimeType = resolveContractMimeType(fileName)

        val downloadResult = runCatching {
            val body = repository.downloadContractFile(sessionId, userId, isFinal = final)
            saveContractToDownloads(context, body, fileName, mimeType)
        }.recoverCatching { throwable ->
            val isFinalNotFound = throwable is HttpException && throwable.code() == 404
            if (final && isFinalNotFound) {
                val body = repository.downloadContractFile(sessionId, userId, isFinal = false)
                saveContractToDownloads(context, body, fileName, mimeType)
            } else {
                throw throwable
            }
        }

        downloadResult
            .onSuccess {
                Toast.makeText(
                    context,
                    context.getString(R.string.contracts_download_saved, fileName),
                    Toast.LENGTH_LONG
                ).show()
            }
            .onFailure { throwable ->
                val message = when (throwable) {
                    is IOException -> context.getString(R.string.contracts_error_download_unavailable)
                    else -> parseApiError(throwable)
                }
                _error.value = message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
    }

    private fun resolveContractFileName(documentUrl: String?, sessionId: String): String {
        val rawName = documentUrl
            ?.substringAfterLast('/')
            ?.substringBefore('?')
            ?.takeIf { it.isNotBlank() }
        val base = rawName ?: "contract_$sessionId.docx"
        val hasExtension = base.substringAfterLast('.', "").isNotEmpty()
        return if (hasExtension) base else "$base.docx"
    }

    private fun resolveContractMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension.isEmpty()) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }

    private suspend fun saveContractToDownloads(
        context: Context,
        body: ResponseBody,
        fileName: String,
        mimeType: String
    ): Uri = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Unable to create download entry")
            resolver.openOutputStream(uri)?.use { output ->
                body.use { response ->
                    response.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
            } ?: throw IOException("Unable to open download stream")
            uri
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()
            val targetFile = File(dir, fileName)
            body.use { response ->
                response.byteStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", targetFile)
        }
    }

    suspend fun loadContractDetails(sessionId: String): ContractUiModel? {
        val info = runCatching { repository.contract(sessionId, userId) }
            .onFailure { _error.value = parseApiError(it) }
            .getOrNull()
        
        // Оновлюємо локальний контекст для підтримки signContract та інших операцій
        // Спершу пробуємо з /contract, потім fallback на session summary
        var templateFromApi = info?.templateId?.takeIf { it.isNotBlank() }
        var categoryFromApi = info?.categoryId?.takeIf { it.isNotBlank() }
        
        // Fallback: якщо /contract не повернув template_id, пробуємо session summary
        if (templateFromApi == null || categoryFromApi == null) {
            val summary = runCatching { repository.findSessionSummary(sessionId, userId) }.getOrNull()
            if (templateFromApi == null) templateFromApi = summary?.template_id?.takeIf { it.isNotBlank() }
            // category_id зазвичай не в summary, але можна шукати через sessionDetails
            if (categoryFromApi == null) {
                val details = runCatching { repository.fetchSessionDetails(sessionId, userId) }.getOrNull()
                categoryFromApi = details?.categoryId?.takeIf { it.isNotBlank() }
                if (templateFromApi == null) templateFromApi = details?.templateId?.takeIf { it.isNotBlank() }
            }
        }
        
        templateFromApi?.let { _selectedTemplateId = it }
        categoryFromApi?.let { _selectedCategoryId = it }
        
        val history = runCatching { repository.fetchHistory(sessionId, userId) }
            .onFailure { _error.value = parseApiError(it) }
            .getOrDefault(emptyList())
        val lastUpdated = history.firstOrNull()?.date.orEmpty()
        val contractUi = info?.toUiModel(sessionId, history, lastUpdated) ?: return null
        _contracts.value = _contracts.value.map { if (it.id == sessionId) contractUi else it }
        return contractUi
    }

    private fun initialGreetingMessage(): ContractChatMessage =
        ContractChatMessage(
            id = nextChatMessageId(),
            isUser = false,
            text = "Привіт! Я AI-агент, який допоможе створити кастомний договір. Опишіть свою ситуацію або оберіть один із запитів нижче."
        )

    private fun nextChatMessageId(): Long = ++chatMessageCounter

    private suspend fun applySessionContext(session: String): Boolean {
        selectedCategoryId?.takeIf { it.isNotBlank() }?.let { categoryId ->
            if (categoryBoundSessionId != session) {
                val applied = runCatching { repository.setCategory(session, categoryId, userId) }
                    .onSuccess { categoryBoundSessionId = session }
                    .onFailure { _error.value = parseApiError(it) }
                    .isSuccess
                if (!applied) return false
            }
        }
        selectedTemplateId?.takeIf { it.isNotBlank() }?.let { templateId ->
            if (templateBoundSessionId != session) {
                val applied = runCatching { repository.setTemplate(session, templateId, userId) }
                    .onSuccess { templateBoundSessionId = session }
                    .onFailure { _error.value = parseApiError(it) }
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

    /**
     * Фоново звіряє "активні/підписані" договори через детальний API,
     * щоб не завищувати лічильники, якщо бракує підписів другої сторони.
     */
    private fun refreshContractStatusesAsync() {
        val snapshot = _contracts.value
        if (snapshot.isEmpty()) return
        viewModelScope.launch {
            snapshot
                .filter { it.status == ContractStatus.ACTIVE || it.isSigned }
                .forEach { contract ->
                    val details = runCatching { repository.contract(contract.id, userId) }.getOrNull()
                    if (details != null) {
                        val updated = details.toUiModel(
                            sessionId = contract.id,
                            history = emptyList(),
                            lastUpdated = contract.lastUpdated
                        )
                        _contracts.value = _contracts.value.map { if (it.id == contract.id) updated else it }
                    }
                }
        }
    }

    /**
     * Перезавантажує схему сесії; якщо бекенд повернув порожній список ролей (sections=[]),
     * підтягуємо схему категорії як fallback і, за відсутності категорії, ставимо хакатонівський
     * дефолт для оренди ("lease_real_estate" + "lease_flat").
     */
    private suspend fun reloadSchemaWithFallback(sessionId: String) {
        runCatching {
            refreshSessionSchema(scope = "all", dataMode = "values", withLoading = false)
        }.onFailure { _error.value = parseApiError(it) }

        val rolesMissing = _sessionParties.value.isEmpty()
        val categoryMissing = selectedCategoryId.isNullOrBlank()
        val templateMissing = selectedTemplateId.isNullOrBlank()

        if (rolesMissing) {
            // Підтягуємо схему категорії, якщо вона є
            if (!categoryMissing) {
                runCatching { loadCategorySchema(selectedCategoryId!!) }
            } else if (categoryMissing && templateMissing) {
                // Хак для демо: якщо нічого не знаємо про сесію, пробуємо орендний шаблон
                _selectedTemplateId = selectedTemplateId ?: "lease_flat"
                _selectedCategoryId = selectedCategoryId ?: "lease_real_estate"
                categoryBoundSessionId = null
                templateBoundSessionId = null
                runCatching { loadCategorySchema(selectedCategoryId!!) }
            }

            // Після підстановки категорії/шаблону застосовуємо контекст і ще раз тягнемо схему
            runCatching {
                refreshSessionSchema(scope = "all", dataMode = "values", withLoading = false)
            }.onFailure { _error.value = parseApiError(it) }

            // Якщо й після цього ролей нема – показуємо хоч щось із схеми категорії
            if (_sessionParties.value.isEmpty() && _partySchema.value?.roles?.isNotEmpty() == true) {
                _sessionParties.value = _partySchema.value?.roles.orEmpty()
                _mainRole.value = _partySchema.value?.mainRole ?: _mainRole.value
            }
        }
    }
}

private fun ua.gov.diia.opensource.data.contracts.api.ContractResponseDto.toUiModel(
    sessionId: String,
    history: List<HistoryUiModel> = emptyList(),
    lastUpdated: String = ""
): ContractUiModel {
    // Використовуємо statusEffective від бекенда як основне джерело істини
    val effectiveStatus = statusEffective ?: status
    val normalizedStatus = effectiveStatus?.lowercase()
    val activeStatuses = setOf("completed", "signed", "active")

    // Клієнтська верифікація: перевіряємо чи ВСІХ required ролей підписано
    // Це workaround для бага бекенду, який може повернути is_signed=true передчасно
    val actuallyAllSigned = run {
        // Якщо бекенд каже is_signed=false, довіряємо
        if (isSigned != true) return@run false
        // Якщо немає списку required ролей, довіряємо бекенду
        val required = requiredRoles
        if (required.isNullOrEmpty()) return@run normalizedStatus in activeStatuses
        // Перевіряємо чи всі required ролі підписали
        val sigs = signatures ?: emptyMap()
        val allRequiredSigned = required.all { role -> sigs[role] == true }
        allRequiredSigned && normalizedStatus in activeStatuses
    }

    val (subtitle, contractStatus) = mapStatusLabel(effectiveStatus, actuallyAllSigned)
    val title = this.title ?: templateId ?: "Договір"
    return ContractUiModel(
        id = sessionId,
        title = title,
        subtitle = subtitle,
        status = contractStatus,
        lastUpdated = lastUpdated,
        iconRes = ua.gov.diia.ui_base.R.drawable.ic_doc_cert,
        isFilled = documentReady == true || canBuildContract == true,
        isSigned = actuallyAllSigned,
        history = history
    )
}


private fun mapStatusLabel(raw: String?, allSigned: Boolean = true): Pair<String, ContractStatus> {
    val normalized = raw?.lowercase()
    val isReadyToSign = normalized in listOf("ready_to_sign", "ready", "pending_signature", "ready_to_build")
    val isActive = normalized in listOf("completed", "signed", "active")
    val isDraftLike = normalized in listOf(
        "draft",
        "idle",
        "new",
        "ready_to_form",
        "pending_data",
        "collecting_fields",
        "template_selected",
        null
    )

    if (!allSigned) {
        return when {
            isReadyToSign || isActive -> "Статус: Очікує підпису" to ContractStatus.PENDING_SIGNATURE
            else -> "Статус: Чернетка" to ContractStatus.DRAFT
        }
    }

    return when {
        isActive -> "Статус: Підписано" to ContractStatus.ACTIVE
        isReadyToSign -> "Статус: Очікує підпису" to ContractStatus.PENDING_SIGNATURE
        isDraftLike -> "Статус: Чернетка" to ContractStatus.DRAFT
        else -> "Статус: Чернетка" to ContractStatus.DRAFT
    }
}





