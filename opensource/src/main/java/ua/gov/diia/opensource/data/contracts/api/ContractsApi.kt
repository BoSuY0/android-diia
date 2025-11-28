package ua.gov.diia.opensource.data.contracts.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import okhttp3.ResponseBody
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class ContractCategoryDto(
    val id: String,
    val label: String
)

data class ContractTemplateDto(
    val id: String,
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "name")
    val name: String? = null,
    val file: String? = null
)

data class ContractTemplatesResponse(
    @Json(name = "category_id")
    val categoryId: String? = null,
    @Json(name = "templates")
    val templates: List<ContractTemplateDto> = emptyList()
)

data class ChatRequestDto(
    @Json(name = "session_id")
    val sessionId: String? = null,
    val message: String,
    /** Якщо true - скидає історію чату на бекенді перед відправкою повідомлення */
    val reset: Boolean = false
)

data class ChatResponseDto(
    @Json(name = "session_id")
    val sessionId: String,
    val reply: String,
    val actions: List<ChatActionDto>? = null
)

/**
 * Дія-кнопка, яку LLM може повернути в чаті.
 * @param type тип дії: "navigate_filling_mode", "confirm_category", etc.
 * @param label текст кнопки
 * @param payload додаткові дані (category_id, template_id, etc.)
 */
data class ChatActionDto(
    val type: String,
    val label: String,
    val payload: Map<String, String>? = null
)

interface ContractsApi {
    // ==================== Health & Profile ====================
    
    @GET("/healthz")
    suspend fun healthz(): HealthResponseDto

    @GET("/healthz/detailed")
    suspend fun healthzDetailed(
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): HealthDetailedResponseDto

    @GET("/me/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String?
    ): ProfileResponseDto

    // ==================== Categories & Templates ====================
    
    @GET("/categories")
    suspend fun getCategories(): List<ContractCategoryDto>

    @GET("/categories/{categoryId}/templates")
    suspend fun getTemplates(
        @Path("categoryId") categoryId: String
    ): ContractTemplatesResponse

    @POST("/categories/find")
    suspend fun findCategory(
        @Body body: FindCategoryRequest
    ): FindCategoryResponseDto

    @GET("/categories/{categoryId}/entities")
    suspend fun getCategoryEntities(
        @Path("categoryId") categoryId: String
    ): CategoryEntitiesResponseDto

    // ==================== Sessions ====================
    
    @POST("/sessions")
    suspend fun createSession(
        @Body body: CreateSessionRequestDto,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): SessionResponseDto

    /** Приєднатися до існуючої сесії (для deep-link) */
    @POST("/sessions/join")
    suspend fun joinSession(
        @Body body: JoinSessionRequestDto,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): JoinSessionResponseDto

    @GET("/sessions/{sessionId}")
    suspend fun getSession(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): SessionDetailsDto

    @POST("/chat")
    suspend fun chat(
        @Body body: ChatRequestDto,
        @Header("X-User-ID") userId: String? = null,
        @Header("Authorization") authorization: String? = null
    ): ChatResponseDto

    @POST("/sessions/{sessionId}/category")
    suspend fun setCategory(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    )

    @POST("/sessions/{sessionId}/template")
    suspend fun setTemplate(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    )

    @POST("/sessions/{sessionId}/filling-mode")
    suspend fun setFillingMode(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    )

    @POST("/sessions/{sessionId}/party-context")
    suspend fun setPartyContext(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    )

    @POST("/sessions/{sessionId}/fields")
    suspend fun upsertField(
        @Path("sessionId") sessionId: String,
        @Body body: UpsertFieldRequest,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): UpsertFieldResponse

    @POST("/sessions/{sessionId}/sync")
    suspend fun sync(
        @Path("sessionId") sessionId: String,
        @Body body: SyncRequestDto,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): SyncResponseDto

    @GET("/sessions/{sessionId}/requirements")
    suspend fun getRequirements(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): RequirementsResponseDto

    // ==================== Contract Build & Sign ====================
    
    @POST("/sessions/{sessionId}/build")
    suspend fun buildContract(
        @Path("sessionId") sessionId: String,
        @Body body: BuildContractRequest,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): BuildContractResponseDto

    @POST("/sessions/{sessionId}/order")
    suspend fun order(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): OrderResponseDto

    @GET("/sessions/{sessionId}/contract")
    suspend fun getContract(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): ContractResponseDto

    @Streaming
    @GET("/sessions/{sessionId}/contract/download")
    suspend fun downloadContract(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("final") isFinal: Boolean? = null
    ): ResponseBody

    @POST("/sessions/{sessionId}/contract/sign")
    suspend fun signContract(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): SignContractResponseDto

    // ==================== User Sessions ====================
    
    @GET("/my-sessions")
    suspend fun getMySessions(
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): List<SessionSummaryDto>

    @GET("/users/{userId}/sessions")
    suspend fun getUserSessions(
        @Path("userId") userId: String,
        @Header("X-User-ID") callerUserId: String?,
        @Header("Authorization") authorization: String? = null
    ): List<SessionSummaryDto>

    @GET("/user-documents/{sessionId}")
    suspend fun getUserDocument(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): UserDocumentResponseDto

    @GET("/categories/{categoryId}/parties")
    suspend fun getCategoryParties(
        @Path("categoryId") categoryId: String
    ): CategoryPartiesResponseDto

    @GET("/categories/{categoryId}/schema")
    suspend fun getCategorySchema(
        @Path("categoryId") categoryId: String
    ): CategorySchemaResponseDto

    // ==================== Schema & Fields ====================
    
    @GET("/sessions/{sessionId}/schema")
    suspend fun getSessionSchema(
        @Path("sessionId") sessionId: String,
        @Query("scope") scope: String,
        @Query("data_mode") dataMode: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): SessionSchemaResponseDto

    @GET("/sessions/{sessionId}/party-fields")
    suspend fun getPartyFields(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null
    ): PartyFieldsResponseDto

    @GET("/sessions/{sessionId}/history")
    suspend fun getHistory(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): HistoryResponseDto

    @Streaming
    @GET("/sessions/{sessionId}/contract/preview")
    suspend fun getContractPreview(
        @Path("sessionId") sessionId: String,
        @Header("X-User-ID") userId: String?,
        @Header("Authorization") authorization: String? = null,
        @Query("user_id") userIdQuery: String? = null
    ): ResponseBody
}

data class SessionResponseDto(val session_id: String)

data class CreateSessionRequestDto(
    @Json(name = "session_id")
    val sessionId: String? = null,
    @Json(name = "user_id")
    val userId: String? = null,
    @Json(name = "category_id")
    val categoryId: String? = null,
    @Json(name = "template_id")
    val templateId: String? = null,
    @Json(name = "filling_mode")
    val fillingMode: String? = null,
    val role: String? = null,
    @Json(name = "person_type")
    val personType: String? = null
)

// ==================== Join Session DTOs ====================

/** Запит на приєднання до існуючої сесії (для deep-link) */
data class JoinSessionRequestDto(
    @Json(name = "session_id")
    val sessionId: String,
    val role: String? = null,
    @Json(name = "person_type")
    val personType: String? = null
)

/** Відповідь на приєднання до сесії */
data class JoinSessionResponseDto(
    @Json(name = "session_id")
    val sessionId: String,
    @Json(name = "category_id")
    val categoryId: String? = null,
    @Json(name = "template_id")
    val templateId: String? = null,
    val state: String? = null,
    @Json(name = "status_effective")
    val statusEffective: String? = null,
    @Json(name = "is_signed")
    val isSigned: Boolean? = null,
    @Json(name = "role_claimed")
    val roleClaimed: String? = null,
    @Json(name = "required_roles")
    val requiredRoles: List<String> = emptyList(),
    @Json(name = "available_roles")
    val availableRoles: List<String> = emptyList()
)

data class CategorySchemaResponseDto(
    @Json(name = "category_id")
    val categoryId: String? = null,
    @Json(name = "main_role")
    val mainRole: String? = null,
    val roles: List<CategorySchemaRoleDto> = emptyList(),
    @Json(name = "person_types")
    val personTypes: List<PersonTypeDto> = emptyList()
)

data class CategorySchemaRoleDto(
    val id: String,
    val label: String = "",
    @Json(name = "allowed_person_types")
    val allowedPersonTypes: List<String> = emptyList()
)

data class SyncPartyDto(
    val person_type: String,
    val fields: Map<String, String>
)

data class SyncRequestDto(
    val category_id: String? = null,
    val template_id: String? = null,
    val parties: Map<String, SyncPartyDto>
)

/**
 * Відсутні поля для конкретної ролі у відповіді sync.
 * Новий формат бекенду:
 * {
 *   "missing_fields": [{"key": "name", "label": "ПІБ", "error": "Помилка валідації"}],
 *   "role_label": "Орендодавець",
 *   "errors": {}
 * }
 */
data class SyncRoleMissingDto(
    @Json(name = "missing_fields")
    val missingFields: List<RequirementsMissingFieldDto> = emptyList(),
    @Json(name = "role_label")
    val roleLabel: String? = null,
    val errors: Map<String, String>? = null
)

/**
 * Інформація про відсутні поля у відповіді sync.
 * contract - список відсутніх полів контракту
 * roles - мапа ролей та їх відсутніх полів (старий формат для сумісності)
 * roles_detailed - детальна інформація по ролях (новий формат з labels)
 */
data class SyncMissingDto(
    val contract: List<String> = emptyList(),
    val roles: Map<String, SyncRoleMissingDto> = emptyMap(),
    @Json(name = "roles_detailed")
    val rolesDetailed: Map<String, RoleDetailedMissingDto>? = null
)

data class SyncResponseDto(
    val status: String? = null,
    val missing: SyncMissingDto? = null,
    val session_id: String? = null,
    val document_url: String? = null
)

data class UpsertFieldRequest(
    val field: String,
    val value: String,
    val role: String? = null
)

data class UpsertFieldResponse(
    val ok: Boolean? = null,
    val field: String? = null,
    val status: String? = null,
    val error: String? = null,
    val can_build_contract: Boolean? = null,
    val state: String? = null
)

data class OrderResponseDto(
    val ok: Boolean? = null,
    val status: String? = null,
    val download_url: String? = null,
    val message: String? = null
)

data class ContractResponseDto(
    @Json(name = "session_id")
    val sessionId: String? = null,
    @Json(name = "category_id")
    val categoryId: String? = null,
    @Json(name = "template_id")
    val templateId: String? = null,
    val title: String? = null,
    val status: String? = null,
    /** Канонічний статус від бекенда - використовувати замість локального обчислення */
    @Json(name = "status_effective")
    val statusEffective: String? = null,
    @Json(name = "is_signed")
    val isSigned: Boolean? = null,
    val signatures: Map<String, Boolean>? = null,
    @Json(name = "client_roles")
    val clientRoles: List<String>? = null,
    @Json(name = "can_build_contract")
    val canBuildContract: Boolean? = null,
    @Json(name = "document_ready")
    val documentReady: Boolean? = null,
    @Json(name = "document_url")
    val documentUrl: String? = null,
    @Json(name = "preview_url")
    val previewUrl: String? = null,
    @Json(name = "role_labels")
    val roleLabels: Map<String, String>? = null,
    /** Список ролей, які повинні підписати договір */
    @Json(name = "required_roles")
    val requiredRoles: List<String>? = null
)

data class SignContractResponseDto(
    val ok: Boolean? = null,
    val is_signed: Boolean? = null,
    val signatures: Map<String, Boolean>? = null
)

data class SessionSummaryDto(
    val session_id: String,
    val template_id: String? = null,
    val title: String? = null,
    val updated_at: String? = null,
    val state: String? = null,
    /** Канонічний статус від бекенда */
    val status_effective: String? = null,
    val is_signed: Boolean? = null,
    /** Підписи по ролях */
    val signatures: Map<String, Boolean>? = null,
    /** Список ролей, які повинні підписати */
    val required_roles: List<String>? = null,
    /** Список доступних ролей для приєднання */
    val available_roles: List<String>? = null
)

data class CategoryPartiesResponseDto(
    val roles: List<PartyRoleDto> = emptyList(),
    @Json(name = "person_types")
    val personTypes: List<PersonTypeDto> = emptyList(),
    @Json(name = "main_role")
    val mainRole: String? = null
)

data class PartyRoleDto(
    val id: String,
    val label: String? = null,
    @Json(name = "allowed_person_types")
    val allowedPersonTypes: List<String> = emptyList()
)

data class PersonTypeDto(
    @Json(name = "person_type")
    val id: String,
    val label: String,
    val fields: List<PartyFieldDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PartyFieldDto(
    val field: String? = null,
    val key: String? = null,
    val label: String,
    val required: Boolean = false,
    val type: String? = null,
    val value: Any? = null,
    val status: String? = null,
    val error: String? = null
)

data class SessionSchemaResponseDto(
    val parties: List<SessionPartyDto> = emptyList(),
    @Json(name = "main_role")
    val mainRole: String? = null,
    @Json(name = "filling_mode")
    val fillingMode: String? = null,
    val contract: ContractSchemaDto? = null
)

data class SessionPartyDto(
    @Json(name = "role")
    val id: String? = null,
    val label: String? = null,
    @Json(name = "person_type")
    val personType: String? = null,
    @Json(name = "allowed_types")
    val allowedTypes: List<AllowedTypeDto> = emptyList(),
    @Json(name = "claimed_by")
    val claimedBy: String? = null,
    val fields: List<PartyFieldDto> = emptyList()
)

data class AllowedTypeDto(
    val value: String? = null,
    val label: String? = null
)

data class PartyFieldsResponseDto(
    val fields: List<PartyFieldDto> = emptyList()
)

data class ContractSchemaDto(
    val title: String? = null,
    val subtitle: String? = null,
    val fields: List<PartyFieldDto> = emptyList()
)

data class HistoryResponseDto(
    @Json(name = "session_id")
    val sessionId: String? = null,
    val state: String? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null,
    val history: List<HistoryEntryDto> = emptyList()
)

data class HistoryEntryDto(
    val ts: String? = null,
    val type: String? = null,
    val key: String? = null,
    val label: String? = null,
    @Json(name = "user_id")
    val userId: String? = null,
    val role: String? = null,
    val roles: List<String>? = null,
    val value: String? = null,
    val normalized: String? = null,
    val valid: Boolean? = null,
    val source: String? = null,
    val state: String? = null
)

// ==================== Health & Profile DTOs ====================

data class HealthResponseDto(
    val status: String
)

data class HealthDetailedResponseDto(
    val status: String,
    @Json(name = "docx_ok")
    val docxOk: Boolean? = null,
    @Json(name = "redis_ok")
    val redisOk: Boolean? = null,
    @Json(name = "contracts_db_ok")
    val contractsDbOk: Boolean? = null
)

data class ProfileResponseDto(
    @Json(name = "user_id")
    val userId: String? = null,
    @Json(name = "full_name")
    val fullName: String? = null,
    @Json(name = "tax_id")
    val taxId: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val raw: Map<String, Any?>? = null
)

// ==================== Category DTOs ====================

data class FindCategoryRequest(
    val query: String
)

data class FindCategoryResponseDto(
    val ok: Boolean,
    @Json(name = "category_id")
    val categoryId: String? = null,
    val label: String? = null,
    val error: String? = null
)

data class CategoryEntitiesResponseDto(
    @Json(name = "category_id")
    val categoryId: String? = null,
    val entities: List<CategoryEntityFieldDto> = emptyList()
)

data class CategoryEntityFieldDto(
    val field: String,
    val label: String,
    val required: Boolean = false
)

// ==================== Session Details DTO ====================

data class SessionDetailsDto(
    @Json(name = "session_id")
    val sessionId: String,
    @Json(name = "category_id")
    val categoryId: String? = null,
    @Json(name = "template_id")
    val templateId: String? = null,
    val title: String? = null,
    val state: String? = null,
    @Json(name = "filling_mode")
    val fillingMode: String? = null,
    @Json(name = "can_build_contract")
    val canBuildContract: Boolean? = null,
    // progress може бути як числом, так і об'єктом (API повертає {})
    val progress: Any? = null,
    @Json(name = "role_owners")
    val roleOwners: Map<String, String>? = null,
    @Json(name = "party_types")
    val partyTypes: Map<String, String>? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null
)

// ==================== Requirements DTO ====================

data class RequirementsResponseDto(
    @Json(name = "session_id")
    val sessionId: String? = null,
    val state: String? = null,
    @Json(name = "can_build_contract")
    val canBuildContract: Boolean? = null,
    val missing: RequirementsMissingDto? = null,
    @Json(name = "is_ready")
    val isReady: Boolean? = null,
    /** Готовність поточного користувача (тільки його поля заповнені) */
    @Json(name = "is_ready_self")
    val isReadySelf: Boolean? = null,
    /** Готовність усіх сторін (всі обов'язкові поля всіх ролей заповнені) */
    @Json(name = "is_ready_all")
    val isReadyAll: Boolean? = null
)

/**
 * Інформація про відсутні поля у відповіді /requirements.
 * Бекенд повертає два формати:
 * - roles: старий формат для backward compatibility: role -> [field_keys]
 * - roles_detailed: новий детальний формат: role -> { role, role_label, missing_fields: [...] }
 */
data class RequirementsMissingDto(
    val contract: List<String> = emptyList(),
    /** Старий формат: role -> список ключів полів (просто рядки) */
    val roles: Map<String, List<RequirementsMissingFieldDto>>? = null,
    /** Новий детальний формат з labels */
    @Json(name = "roles_detailed")
    val rolesDetailed: Map<String, RoleDetailedMissingDto>? = null
)

/**
 * Поле з інформацією про missing/validation у форматі {key, label, error}.
 */
data class RequirementsMissingFieldDto(
    val key: String? = null,
    val field: String? = null,
    val label: String? = null,
    val error: String? = null
)

/**
 * Детальна інформація про відсутні поля ролі (новий формат roles_detailed).
 */
data class RoleDetailedMissingDto(
    val role: String? = null,
    @Json(name = "role_label")
    val roleLabel: String? = null,
    @Json(name = "missing_fields")
    val missingFields: List<RequirementsMissingFieldDto> = emptyList()
)

// ==================== Build Contract DTOs ====================

data class BuildContractRequest(
    @Json(name = "template_id")
    val templateId: String? = null
)

data class BuildContractResponseDto(
    val ok: Boolean? = null,
    @Json(name = "document_url")
    val documentUrl: String? = null,
    @Json(name = "file_path")
    val filePath: String? = null,
    val error: String? = null
)

// ==================== User Document DTO ====================

data class UserDocumentResponseDto(
    @Json(name = "session_id")
    val sessionId: String? = null,
    @Json(name = "document_type")
    val documentType: String? = null,
    val data: Map<String, Any?>? = null
)
