package ua.gov.diia.opensource.data.contracts.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.gov.diia.opensource.data.contracts.api.CategoryPartiesResponseDto
import ua.gov.diia.opensource.data.contracts.api.ChatRequestDto
import ua.gov.diia.opensource.data.contracts.api.ChatResponseDto
import ua.gov.diia.opensource.data.contracts.api.ContractCategoryDto
import ua.gov.diia.opensource.data.contracts.api.ContractResponseDto
import ua.gov.diia.opensource.data.contracts.api.ContractTemplateDto
import ua.gov.diia.opensource.data.contracts.api.ContractsApi
import ua.gov.diia.opensource.data.contracts.api.HistoryResponseDto
import ua.gov.diia.opensource.data.contracts.api.OrderResponseDto
import ua.gov.diia.opensource.data.contracts.api.PartyFieldDto
import ua.gov.diia.opensource.data.contracts.api.SessionResponseDto
import ua.gov.diia.opensource.data.contracts.api.SessionSchemaResponseDto
import ua.gov.diia.opensource.data.contracts.api.SignHistoryDto
import ua.gov.diia.opensource.data.contracts.api.SyncPartyDto
import ua.gov.diia.opensource.data.contracts.api.SyncRequestDto
import ua.gov.diia.opensource.data.contracts.api.SyncResponseDto
import ua.gov.diia.opensource.data.contracts.api.UpsertFieldRequest
import ua.gov.diia.opensource.data.contracts.api.UpsertFieldResponse
import ua.gov.diia.opensource.ui.compose.ContractCategory
import ua.gov.diia.opensource.ui.compose.ContractTemplate
import ua.gov.diia.opensource.ui.compose.HistoryUiModel
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

    suspend fun createSession(optionalSessionId: String? = null, userId: String? = null, clientId: String): String {
        val response: SessionResponseDto = api.createSession(
            mapOf(
                "session_id" to optionalSessionId,
                "user_id" to userId
            ),
            clientId
        )
        return response.session_id
    }

    suspend fun chat(sessionId: String?, message: String, clientId: String): ChatResponseDto =
        api.chat(ChatRequestDto(sessionId = sessionId, message = message), clientId)

    suspend fun setCategory(sessionId: String, categoryId: String, clientId: String) {
        api.setCategory(sessionId, mapOf("category_id" to categoryId), clientId)
    }

    suspend fun setTemplate(sessionId: String, templateId: String, clientId: String) {
        api.setTemplate(sessionId, mapOf("template_id" to templateId), clientId)
    }

    suspend fun setFillingMode(sessionId: String, mode: String, clientId: String) {
        api.setFillingMode(sessionId, mapOf("mode" to mode), clientId)
    }

    suspend fun setPartyContext(sessionId: String, role: String, personType: String, clientId: String, userId: String) {
        api.setPartyContext(
            sessionId,
            mapOf("role" to role, "person_type" to personType, "user_id" to userId),
            clientId
        )
    }

    suspend fun upsertField(sessionId: String, field: String, value: String, roleId: String?, clientId: String): UpsertFieldResponse =
        api.upsertField(
            sessionId = sessionId,
            body = UpsertFieldRequest(field = field, value = value, role = roleId),
            clientId = clientId
        )

    suspend fun sync(sessionId: String, request: SyncRequestDto, clientId: String): SyncResponseDto {
        val response = api.sync(sessionId, request, clientId)
        val status = response.status?.lowercase()
        if (status == null || status == "ready" || status == "partial") return response
        throw IllegalStateException("Не вдалося зберегти дані")
    }

    suspend fun order(sessionId: String, clientId: String): OrderResponseDto =
        api.order(sessionId, clientId, clientId)

    suspend fun contract(sessionId: String, clientId: String): ContractResponseDto =
        api.getContract(sessionId, clientId, clientId)

    suspend fun signContract(sessionId: String, clientId: String) =
        api.signContract(sessionId, clientId, clientId)

    suspend fun fetchMySessions(clientId: String): List<ua.gov.diia.opensource.ui.compose.ContractUiModel> =
        api.getMySessions(clientId, clientId).map { it.toContractUiModel() }

    suspend fun fetchUserSessions(userId: String): List<ua.gov.diia.opensource.ui.compose.ContractUiModel> =
        api.getUserSessions(userId).map { it.toContractUiModel() }

    suspend fun fetchCategoryPartySchema(categoryId: String): ContractPartySchema =
        api.getCategoryParties(categoryId).toDomain()

    suspend fun fetchSessionSchema(
        sessionId: String,
        scope: String,
        dataMode: String,
        clientId: String
    ): SessionPartySchema =
        api.getSessionSchema(sessionId, scope, dataMode, clientId).toDomain()

    suspend fun fetchPartyFields(sessionId: String, clientId: String): List<ContractPartyField> =
        api.getPartyFields(sessionId, clientId).fields.map { it.toDomain() }

    suspend fun fetchHistory(sessionId: String, clientId: String): List<HistoryUiModel> =
        api.getHistory(sessionId, clientId, clientId).toDomain()

    suspend fun fetchContractPreviewHtml(sessionId: String, clientId: String): String =
        withContext(Dispatchers.IO) {
            api.getContractPreview(sessionId, clientId, clientId).use { body ->
                // Бекенд повертає готовий HTML-прев'ю (без DOCX). Читаємо як UTF-8 текст.
                body.string()
            }
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
    val mainRole: String? = null,
    val fillingMode: String? = null
)

private fun CategoryPartiesResponseDto.toDomain(): ContractPartySchema = ContractPartySchema(
    roles = roles.map { it.toDomain() },
    personTypes = personTypes.map { it.toDomain() },
    mainRole = mainRole
)

private fun SessionSchemaResponseDto.toDomain(): SessionPartySchema = SessionPartySchema(
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
    mainRole = mainRole,
    fillingMode = fillingMode
)

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

private fun PartyFieldDto.toDomain(): ContractPartyField =
    ContractPartyField(
        key = key ?: field.orEmpty(),
        label = label,
        required = required,
        type = type,
        value = when (val raw = value) {
            is Boolean -> if (raw) raw.toString() else null
            is Number -> raw.toString()
            is String -> raw
            else -> raw?.toString()
        },
        status = status,
        error = error
    )

private fun HistoryResponseDto.toDomain(): List<HistoryUiModel> {
    val events = mutableListOf<HistoryUiModel>()
    val statusEvent = state?.ifBlank { null }?.let { statusValue ->
        val label = mapHistoryStateLabel(statusValue)
        HistoryUiModel(
            date = formatDisplayDate(updatedAt),
            description = "Статус: $label"
        )
    }
    statusEvent?.let { events.add(it) }
    events += signHistory.mapNotNull { it.toDomain() }
    return events
}

private fun SignHistoryDto.toDomain(): HistoryUiModel? {
    val date = formatDisplayDate(timestamp)
    val rolesText = roles?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Ролі не вказані"
    val stateText = mapHistoryStateLabel(state) ?: "подія"
    val description = "Підпис: $stateText ($rolesText)"
    return HistoryUiModel(
        date = date,
        description = description
    )
}

private fun ua.gov.diia.opensource.data.contracts.api.SessionSummaryDto.toContractUiModel(): ua.gov.diia.opensource.ui.compose.ContractUiModel {
    val (subtitleValue, contractStatus) = mapStatusLabel(state)
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
        isSigned = is_signed == true,
        history = emptyList()
    )
}

private fun mapStatusLabel(raw: String?): Pair<String, ua.gov.diia.opensource.ui.compose.ContractStatus> {
    return when (raw?.lowercase()) {
        "ready_to_sign", "ready", "pending_signature" -> "Статус: Очікує підписання" to ua.gov.diia.opensource.ui.compose.ContractStatus.PENDING_SIGNATURE
        "completed", "signed", "active" -> "Статус: Активний" to ua.gov.diia.opensource.ui.compose.ContractStatus.ACTIVE
        else -> "Статус: Чернетка" to ua.gov.diia.opensource.ui.compose.ContractStatus.DRAFT
    }
}

private fun mapHistoryStateLabel(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    val mapped = when (value.lowercase()) {
        "collecting_fields" -> "Заповнення даних"
        "draft" -> "Чернетка"
        "ready", "ready_to_sign", "pending_signature" -> "Очікує підписання"
        "signed", "completed", "active" -> "Підписано"
        "ordered" -> "Замовлено"
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
