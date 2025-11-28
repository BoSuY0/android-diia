package ua.gov.diia.opensource.data.contracts.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.gov.diia.opensource.data.contracts.api.BuildContractRequest
import ua.gov.diia.opensource.data.contracts.api.CategoryPartiesResponseDto
import ua.gov.diia.opensource.data.contracts.api.CategorySchemaResponseDto
import ua.gov.diia.opensource.data.contracts.api.CreateSessionRequestDto
import ua.gov.diia.opensource.data.contracts.api.ChatRequestDto
import ua.gov.diia.opensource.data.contracts.api.ChatResponseDto
import ua.gov.diia.opensource.data.contracts.api.ContractCategoryDto
import ua.gov.diia.opensource.data.contracts.api.ContractResponseDto
import ua.gov.diia.opensource.data.contracts.api.ContractTemplateDto
import ua.gov.diia.opensource.data.contracts.api.ContractsApi
import ua.gov.diia.opensource.data.contracts.api.HistoryResponseDto
import ua.gov.diia.opensource.data.contracts.api.JoinSessionRequestDto
import ua.gov.diia.opensource.data.contracts.api.JoinSessionResponseDto
import ua.gov.diia.opensource.data.contracts.api.OrderResponseDto
import ua.gov.diia.opensource.data.contracts.api.RequirementsResponseDto
import ua.gov.diia.opensource.data.contracts.api.PartyFieldDto
import ua.gov.diia.opensource.data.contracts.api.SessionResponseDto
import ua.gov.diia.opensource.data.contracts.api.SessionDetailsDto
import ua.gov.diia.opensource.data.contracts.api.SessionSummaryDto
import ua.gov.diia.opensource.data.contracts.api.SessionSchemaResponseDto
import ua.gov.diia.opensource.data.contracts.api.HistoryEntryDto
import ua.gov.diia.opensource.data.contracts.api.SyncPartyDto
import ua.gov.diia.opensource.data.contracts.api.SyncRequestDto
import ua.gov.diia.opensource.data.contracts.api.SyncResponseDto
import ua.gov.diia.opensource.data.contracts.api.UpsertFieldRequest
import ua.gov.diia.opensource.data.contracts.api.UpsertFieldResponse
import ua.gov.diia.opensource.ui.compose.ContractCategory
import ua.gov.diia.opensource.ui.compose.ContractTemplate
import ua.gov.diia.opensource.ui.compose.HistoryUiModel
import okhttp3.ResponseBody
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ContractsRepository @Inject constructor(
    private val api: ContractsApi
) {

    suspend fun fetchCategories(): List<ContractCategory> =
        api.getCategories().map { it.toDomain() }

    suspend fun fetchTemplates(categoryId: String): List<ContractTemplate> =
        api.getTemplates(categoryId).templates.map { it.toDomain() }

    suspend fun createSession(
        optionalSessionId: String? = null,
        creatorUserId: String? = null,
        categoryId: String? = null,
        templateId: String? = null,
        fillingMode: String? = null,
        role: String? = null,
        personType: String? = null,
        userId: String
    ): String {
        val response: SessionResponseDto = api.createSession(
            CreateSessionRequestDto(
                sessionId = optionalSessionId,
                userId = creatorUserId,
                categoryId = categoryId,
                templateId = templateId,
                fillingMode = fillingMode,
                role = role,
                personType = personType
            ),
            userId
        )
        return response.session_id
    }

    suspend fun chat(sessionId: String?, message: String, userId: String, reset: Boolean = false): ChatResponseDto =
        api.chat(ChatRequestDto(sessionId = sessionId, message = message, reset = reset), userId)

    /**
     * Приєднатися до існуючої сесії (для deep-link).
     * На відміну від createSession, цей метод не створює нову сесію,
     * а приєднує користувача до існуючої.
     */
    suspend fun joinSession(
        sessionId: String,
        userId: String,
        role: String? = null,
        personType: String? = null
    ): JoinSessionResponseDto =
        api.joinSession(
            JoinSessionRequestDto(sessionId = sessionId, role = role, personType = personType),
            userId
        )

    suspend fun setCategory(sessionId: String, categoryId: String, userId: String) {
        api.setCategory(sessionId, mapOf("category_id" to categoryId), userId)
    }

    suspend fun setTemplate(sessionId: String, templateId: String, userId: String) {
        api.setTemplate(sessionId, mapOf("template_id" to templateId), userId)
    }

    suspend fun setFillingMode(sessionId: String, mode: String, userId: String) {
        api.setFillingMode(sessionId, mapOf("mode" to mode), userId)
    }

    suspend fun setPartyContext(sessionId: String, role: String, personType: String, userId: String, bodyUserId: String) {
        api.setPartyContext(
            sessionId,
            mapOf("role" to role, "person_type" to personType, "user_id" to bodyUserId),
            userId
        )
    }

    suspend fun upsertField(sessionId: String, field: String, value: String, roleId: String?, userId: String): UpsertFieldResponse =
        api.upsertField(
            sessionId = sessionId,
            body = UpsertFieldRequest(field = field, value = value, role = roleId),
            userId = userId
        )

    suspend fun sync(sessionId: String, request: SyncRequestDto, userId: String): SyncResponseDto {
        val response = api.sync(sessionId, request, userId)
        val status = response.status?.lowercase()
        if (status == null || status == "ready" || status == "partial") return response
        throw IllegalStateException("Не вдалося зберегти дані")
    }

    suspend fun order(sessionId: String, userId: String): OrderResponseDto =
        api.order(sessionId, userId, userIdQuery = userId)

    suspend fun contract(sessionId: String, userId: String): ContractResponseDto =
        api.getContract(sessionId, userId, userIdQuery = userId)

    suspend fun buildContract(
        sessionId: String,
        userId: String,
        templateId: String? = null
    ) = api.buildContract(
        sessionId,
        BuildContractRequest(templateId = templateId),
        userId,
        userIdQuery = userId
    )

    suspend fun signContract(sessionId: String, userId: String) =
        api.signContract(sessionId, userId, userIdQuery = userId)

    suspend fun fetchMySessions(userId: String): List<ua.gov.diia.opensource.ui.compose.ContractUiModel> =
        api.getMySessions(userId, userIdQuery = userId).map { it.toContractUiModel() }

    suspend fun fetchUserSessions(userId: String): List<ua.gov.diia.opensource.ui.compose.ContractUiModel> =
        api.getUserSessions(userId, callerUserId = userId).map { it.toContractUiModel() }

    suspend fun findSessionSummary(sessionId: String, userId: String): SessionSummaryDto? {
        val mySessions = api.getMySessions(userId, userIdQuery = userId)
        val mine = mySessions.firstOrNull { it.session_id == sessionId }
        if (mine != null) return mine
        val userSessions = api.getUserSessions(userId, callerUserId = userId)
        return userSessions.firstOrNull { it.session_id == sessionId }
    }

    suspend fun findCategoryByTemplateId(templateId: String): String? {
        val categories = fetchCategories()
        categories.forEach { category ->
            val templates = runCatching { fetchTemplates(category.id) }.getOrNull().orEmpty()
            if (templates.any { it.id == templateId }) {
                return category.id
            }
        }
        return null
    }

    suspend fun fetchCategoryPartySchema(categoryId: String): ContractPartySchema =
        api.getCategoryParties(categoryId).toDomain()

    suspend fun fetchCategorySchema(categoryId: String): ContractPartySchema =
        api.getCategorySchema(categoryId).toDomain()

    suspend fun fetchCategoryEntities(categoryId: String): List<ContractPartyField> =
        api.getCategoryEntities(categoryId).entities.map { dto ->
            ContractPartyField(
                key = dto.field,
                label = dto.label,
                required = dto.required
            )
        }

    suspend fun fetchSessionSchema(
        sessionId: String,
        scope: String,
        dataMode: String,
        userId: String
    ): SessionPartySchema =
        api.getSessionSchema(sessionId, scope, dataMode, userId).toDomain()

    suspend fun fetchSessionDetails(sessionId: String, userId: String): SessionDetailsDto =
        api.getSession(sessionId, userId)

    suspend fun fetchPartyFields(sessionId: String, userId: String): List<ContractPartyField> =
        api.getPartyFields(sessionId, userId).fields.map { it.toDomain() }

    suspend fun fetchHistory(sessionId: String, userId: String): List<HistoryUiModel> =
        api.getHistory(sessionId, userId, userIdQuery = userId).toDomain()

    suspend fun fetchContractPreviewHtml(sessionId: String, userId: String): String =
        withContext(Dispatchers.IO) {
            api.getContractPreview(sessionId, userId, userIdQuery = userId).use { body ->
                // Бекенд повертає готовий HTML-прев'ю (без DOCX). Читаємо як UTF-8 текст.
                body.string()
            }
        }

    suspend fun fetchRequirements(sessionId: String, userId: String): RequirementsResponseDto =
        api.getRequirements(sessionId, userId)

    suspend fun downloadContractFile(sessionId: String, userId: String, isFinal: Boolean = true): ResponseBody =
        withContext(Dispatchers.IO) {
            api.downloadContract(sessionId, userId, isFinal = isFinal)
        }
}

private fun ContractCategoryDto.toDomain(): ContractCategory =
    ContractCategory(id = id, title = label)

private fun ContractTemplateDto.toDomain(): ContractTemplate =
    ContractTemplate(
        id = id,
        name = label ?: name.orEmpty(),
        file = file.orEmpty()
    )

data class ContractPartySchema(
    val roles: List<ContractPartyRole>,
    val personTypes: List<ContractPersonType>,
    val mainRole: String? = null
)

data class ContractPersonType(
    val id: String,
    val label: String,
    val fields: List<ContractPartyField>
)

data class ContractPartyField(
    val key: String,
    val label: String,
    val required: Boolean,
    val type: String? = null,
    val value: String? = null,
    val status: String? = null,
    val error: String? = null
)

data class ContractPartyRole(
    val id: String,
    val label: String,
    val allowedPersonTypes: List<String>,
    val claimedBy: String? = null,
    val personType: String? = null,
    val fields: List<ContractPartyField> = emptyList()
)

data class SessionPartySchema(
    val parties: List<ContractPartyRole>,
    val contractFields: List<ContractPartyField> = emptyList(),
    val personTypes: List<ContractPersonType> = emptyList(),
    val mainRole: String? = null,
    val fillingMode: String? = null
)

private fun CategoryPartiesResponseDto.toDomain(): ContractPartySchema = ContractPartySchema(
    roles = roles.map { it.toDomain() },
    personTypes = personTypes.map { it.toDomain() },
    mainRole = mainRole
)

private fun CategorySchemaResponseDto.toDomain(): ContractPartySchema = ContractPartySchema(
    roles = roles.map { role ->
        ContractPartyRole(
            id = role.id,
            label = role.label,
            allowedPersonTypes = role.allowedPersonTypes
        )
    },
    personTypes = personTypes.map { it.toDomain() },
    mainRole = mainRole
)

private fun SessionSchemaResponseDto.toDomain(): SessionPartySchema {
    // Витягуємо унікальні personTypes з allowedTypes всіх parties
    val extractedPersonTypes = parties
        .flatMap { party -> party.allowedTypes }
        .filter { it.value != null && it.label != null }
        .distinctBy { it.value }
        .map { allowedType ->
            ContractPersonType(
                id = allowedType.value!!,
                label = allowedType.label!!,
                fields = emptyList() // Поля беруться з party.fields, не з personType
            )
        }
    
    return SessionPartySchema(
        parties = parties.map { dto ->
            ContractPartyRole(
                id = dto.id ?: dto.personType ?: "",
                label = dto.label.orEmpty(),
                allowedPersonTypes = dto.allowedTypes.mapNotNull { it.value },
                claimedBy = dto.claimedBy,
                personType = dto.personType,
                fields = dto.fields.map { it.toDomain() }
            )
        },
        contractFields = contract?.fields?.map { it.toDomain() } ?: emptyList(),
        personTypes = extractedPersonTypes,
        mainRole = mainRole,
        fillingMode = fillingMode
    )
}

private fun ua.gov.diia.opensource.data.contracts.api.PersonTypeDto.toDomain(): ContractPersonType =
    ContractPersonType(
        id = id,
        label = label,
        fields = fields.map { it.toDomain() }
    )

private fun ua.gov.diia.opensource.data.contracts.api.PartyRoleDto.toDomain(): ContractPartyRole =
    ContractPartyRole(
        id = id,
        label = label.orEmpty(),
        allowedPersonTypes = allowedPersonTypes
    )

private fun PartyFieldDto.toDomain(): ContractPartyField {
    val isBooleanField = type?.lowercase() in listOf("boolean", "bool", "checkbox")
    return ContractPartyField(
        key = key ?: field.orEmpty(),
        label = label,
        required = required,
        type = type,
        value = when (val raw = value) {
            // Boolean значення для boolean полів конвертуємо в рядок, для інших - ігноруємо
            // Бекенд може повертати true як індикатор "поле заповнене" для текстових полів
            is Boolean -> if (isBooleanField) raw.toString() else null
            is Number -> raw.toString()
            is String -> {
                // Для НЕ-boolean полів ігноруємо рядки "true"/"false" - це артефакт попереднього багу
                val lowerValue = raw.lowercase()
                if (!isBooleanField && (lowerValue == "true" || lowerValue == "false")) {
                    null
                } else {
                    raw
                }
            }
            else -> raw?.toString()
        },
        status = status,
        error = error
    )
}

private fun HistoryResponseDto.toDomain(): List<HistoryUiModel> {
    val events = mutableListOf<HistoryUiModel>()
    
    // Спочатку додаємо загальний статус сесії
    val statusEvent = state?.ifBlank { null }?.let { statusValue ->
        val label = mapHistoryStateLabel(statusValue)
        HistoryUiModel(
            date = formatDisplayDate(updatedAt),
            description = "Статус: $label"
        )
    }
    statusEvent?.let { events.add(it) }
    
    // Потім додаємо всі записи з history
    events += history.mapNotNull { it.toDomain() }
    
    return events
}

private fun HistoryEntryDto.toDomain(): HistoryUiModel? {
    val date = formatDisplayDate(ts)
    val description = when (type) {
        "field_update" -> {
            // Використовуємо label з бекенду, якщо є, інакше фолбек на маппінг
            val fieldLabel = label?.takeIf { it.isNotBlank() } ?: mapFieldKeyLabel(key)
            val roleLabel = mapRoleLabel(role)
            "$fieldLabel: ${normalized ?: value ?: "—"} ($roleLabel)"
        }
        "sign" -> {
            val rolesText = roles?.takeIf { it.isNotEmpty() }?.joinToString(", ") { mapRoleLabel(it) } ?: "Ролі не вказані"
            val stateText = mapHistoryStateLabel(state) ?: "подія"
            "Підпис: $stateText ($rolesText)"
        }
        "session_created" -> "Сесію створено"
        "party_joined" -> {
            val roleLabel = mapRoleLabel(role)
            "Сторона приєдналась: $roleLabel"
        }
        else -> "Подія: ${type ?: "невідома"}"
    }
    return HistoryUiModel(
        date = date,
        description = description
    )
}

private fun mapFieldKeyLabel(key: String?): String {
    if (key.isNullOrBlank()) return "Поле"
    // Маппінг ключів полів на людські назви
    return when (key) {
        "lessor.name" -> "Ім'я орендодавця"
        "lessor.address" -> "Адреса орендодавця"
        "lessee.name" -> "Ім'я орендаря"
        "lessee.address" -> "Адреса орендаря"
        "premises_return_deadline" -> "Термін повернення"
        "document_number" -> "Номер документа"
        "notice_period_days" -> "Термін повідомлення (днів)"
        "end_date" -> "Дата закінчення"
        "start_date" -> "Дата початку"
        "city" -> "Місто"
        "rent_price_month" -> "Місячна орендна плата"
        "payment_due_day" -> "День оплати"
        "penalty_rate" -> "Ставка пені"
        "area_sqm" -> "Площа (кв.м)"
        "total_area_sqm" -> "Загальна площа (кв.м)"
        "object_address" -> "Адреса об'єкта"
        "contract_date" -> "Дата договору"
        "purpose" -> "Призначення"
        "payment_form" -> "Форма оплати"
        "first_payment_date" -> "Дата першого платежу"
        "payment_delay_days" -> "Дозволена затримка (днів)"
        "repair_notice_days" -> "Повідомлення про ремонт (днів)"
        "lock_change_notice" -> "Повідомлення про зміну замків"
        "term_change_consent" -> "Згода на зміну умов"
        "improvement_consent_type" -> "Тип згоди на покращення"
        "sublease_consent_type" -> "Тип згоди на суборенду"
        "act_signing_period" -> "Термін підписання акта"
        else -> key.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

private fun mapRoleLabel(role: String?): String {
    return when (role?.lowercase()) {
        "lessor" -> "Орендодавець"
        "lessee" -> "Орендар"
        "landlord" -> "Орендодавець"
        "tenant" -> "Орендар"
        "seller" -> "Продавець"
        "buyer" -> "Покупець"
        "discloser" -> "Сторона, що розкриває"
        "receiver" -> "Сторона, що отримує"
        else -> role ?: "—"
    }
}

private fun ua.gov.diia.opensource.data.contracts.api.SessionSummaryDto.toContractUiModel(): ua.gov.diia.opensource.ui.compose.ContractUiModel {
    // Використовуємо status_effective від бекенда як основне джерело істини
    val effectiveStatus = status_effective ?: state
    val normalizedStatus = effectiveStatus?.lowercase()
    val activeStatuses = setOf("completed", "signed", "active")
    
    // Клієнтська верифікація: перевіряємо чи ВСІХ required ролей підписано
    // Це workaround для бага бекенду, який може повернути is_signed=true передчасно
    val actuallyAllSigned = run {
        // Якщо бекенд каже is_signed=false, довіряємо
        if (is_signed != true) return@run false
        // Якщо є available_roles (незайняті ролі) - не всі підписали
        if (!available_roles.isNullOrEmpty()) return@run false
        // Якщо немає списку required ролей, довіряємо бекенду
        val required = required_roles
        if (required.isNullOrEmpty()) return@run normalizedStatus in activeStatuses
        // Перевіряємо чи всі required ролі підписали
        val sigs = signatures ?: emptyMap()
        val allRequiredSigned = required.all { role -> sigs[role] == true }
        allRequiredSigned && normalizedStatus in activeStatuses
    }
    
    val (subtitleValue, contractStatus) = mapStatusLabel(effectiveStatus, actuallyAllSigned)
    val titleValue = title ?: template_id ?: "Договір"
    val formattedUpdatedAt = formatDisplayDate(updated_at)
    return ua.gov.diia.opensource.ui.compose.ContractUiModel(
        id = session_id,
        title = titleValue,
        subtitle = subtitleValue,
        status = contractStatus,
        lastUpdated = formattedUpdatedAt,
        iconRes = ua.gov.diia.ui_base.R.drawable.ic_doc_cert,
        isFilled = true,
        isSigned = actuallyAllSigned,
        history = emptyList()
    )
}


private fun mapStatusLabel(raw: String?, allSigned: Boolean = true): Pair<String, ua.gov.diia.opensource.ui.compose.ContractStatus> {
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
            isReadyToSign || isActive -> "Статус: Очікує підпису" to ua.gov.diia.opensource.ui.compose.ContractStatus.PENDING_SIGNATURE
            else -> "Статус: Чернетка" to ua.gov.diia.opensource.ui.compose.ContractStatus.DRAFT
        }
    }

    return when {
        isActive -> "Статус: Підписано" to ua.gov.diia.opensource.ui.compose.ContractStatus.ACTIVE
        isReadyToSign -> "Статус: Очікує підпису" to ua.gov.diia.opensource.ui.compose.ContractStatus.PENDING_SIGNATURE
        isDraftLike -> "Статус: Чернетка" to ua.gov.diia.opensource.ui.compose.ContractStatus.DRAFT
        else -> "Статус: Чернетка" to ua.gov.diia.opensource.ui.compose.ContractStatus.DRAFT
    }
}

private fun mapHistoryStateLabel(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    val mapped = when (value.lowercase()) {
        "collecting_fields" -> "Заповнення даних"
        "draft", "idle", "new" -> "Чернетка"
        "ready_to_build", "ready_to_form", "pending_data" -> "Готовий до формування"
        "ready", "ready_to_sign", "pending_signature" -> "Очікує підписання"
        "signed", "completed", "active" -> "Підписано"
        "ordered" -> "Замовлено"
        "building" -> "Формування документу"
        "built" -> "Документ сформовано"
        else -> null
    }
    return mapped ?: value.replace('_', ' ')
        .replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
}

private fun formatDisplayDate(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return ""
    val targetFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    return runCatching {
        OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(targetFormatter)
    }.getOrElse {
        runCatching {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .format(targetFormatter)
        }.getOrDefault(value)
    }
}
