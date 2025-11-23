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
    val message: String
)

data class ChatResponseDto(
    @Json(name = "session_id")
    val sessionId: String,
    val reply: String
)

interface ContractsApi {
    @GET("/categories")
    suspend fun getCategories(): List<ContractCategoryDto>

    @GET("/categories/{categoryId}/templates")
    suspend fun getTemplates(
        @Path("categoryId") categoryId: String
    ): ContractTemplatesResponse

    @POST("/sessions")
    suspend fun createSession(
        @Body body: Map<String, String?> = emptyMap(),
        @Header("X-Client-ID") clientId: String?
    ): SessionResponseDto

    @POST("/chat")
    suspend fun chat(
        @Body body: ChatRequestDto,
        @Header("X-Client-ID") clientId: String? = null
    ): ChatResponseDto

    @POST("/sessions/{sessionId}/category")
    suspend fun setCategory(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-Client-ID") clientId: String?
    )

    @POST("/sessions/{sessionId}/template")
    suspend fun setTemplate(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-Client-ID") clientId: String?
    )

    @POST("/sessions/{sessionId}/filling-mode")
    suspend fun setFillingMode(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-Client-ID") clientId: String?
    )

    @POST("/sessions/{sessionId}/party-context")
    suspend fun setPartyContext(
        @Path("sessionId") sessionId: String,
        @Body body: Map<String, String>,
        @Header("X-Client-ID") clientId: String?
    )

    @POST("/sessions/{sessionId}/fields")
    suspend fun upsertField(
        @Path("sessionId") sessionId: String,
        @Body body: UpsertFieldRequest,
        @Header("X-Client-ID") clientId: String?
    ): UpsertFieldResponse

    @POST("/sessions/{sessionId}/sync")
    suspend fun sync(
        @Path("sessionId") sessionId: String,
        @Body body: SyncRequestDto,
        @Header("X-Client-ID") clientId: String?
    ): SyncResponseDto

    @POST("/sessions/{sessionId}/order")
    suspend fun order(
        @Path("sessionId") sessionId: String,
        @Header("X-Client-ID") clientId: String?,
        @Query("client_id") clientIdQuery: String? = null
    ): OrderResponseDto

    @GET("/sessions/{sessionId}/contract")
    suspend fun getContract(
        @Path("sessionId") sessionId: String,
        @Header("X-Client-ID") clientId: String?,
        @Query("client_id") clientIdQuery: String? = null
    ): ContractResponseDto

    @POST("/sessions/{sessionId}/contract/sign")
    suspend fun signContract(
        @Path("sessionId") sessionId: String,
        @Header("X-Client-ID") clientId: String?,
        @Query("client_id") clientIdQuery: String? = null
    ): SignContractResponseDto

    @GET("/my-sessions")
    suspend fun getMySessions(
        @Header("X-Client-ID") clientId: String?,
        @Query("client_id") clientIdQuery: String? = null
    ): List<SessionSummaryDto>

    @GET("/users/{userId}/sessions")
    suspend fun getUserSessions(
        @Path("userId") userId: String
    ): List<SessionSummaryDto>

    @GET("/categories/{categoryId}/parties")
    suspend fun getCategoryParties(
        @Path("categoryId") categoryId: String
    ): CategoryPartiesResponseDto

    @GET("/sessions/{sessionId}/schema")
    suspend fun getSessionSchema(
        @Path("sessionId") sessionId: String,
        @Query("scope") scope: String,
        @Query("data_mode") dataMode: String,
        @Header("X-Client-ID") clientId: String?
    ): SessionSchemaResponseDto

    @GET("/sessions/{sessionId}/party-fields")
    suspend fun getPartyFields(
        @Path("sessionId") sessionId: String,
        @Header("X-Client-ID") clientId: String?
    ): PartyFieldsResponseDto

    @GET("/sessions/{sessionId}/history")
    suspend fun getHistory(
        @Path("sessionId") sessionId: String,
        @Header("X-Client-ID") clientId: String?,
        @Query("client_id") clientIdQuery: String? = null
    ): HistoryResponseDto

    @Streaming
    @GET("/sessions/{sessionId}/contract/preview")
    suspend fun getContractPreview(
        @Path("sessionId") sessionId: String,
        @Header("X-Client-ID") clientId: String?,
        @Query("client_id") clientIdQuery: String? = null
    ): ResponseBody
}

data class SessionResponseDto(val session_id: String)

data class SyncPartyDto(
    val person_type: String,
    val fields: Map<String, String>
)

data class SyncRequestDto(
    val category_id: String? = null,
    val template_id: String? = null,
    val parties: Map<String, SyncPartyDto>
)

data class SyncMissingDto(
    val contract: List<String> = emptyList(),
    val roles: Map<String, List<String>> = emptyMap()
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
    val status: String? = null,
    val document_url: String? = null,
    val preview_url: String? = null,
    val client_roles: List<String>? = null,
    val is_signed: Boolean? = null,
    val signatures: List<String>? = null
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
    val is_signed: Boolean? = null
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
    @Json(name = "sign_history")
    val signHistory: List<SignHistoryDto> = emptyList()
)

data class SignHistoryDto(
    val timestamp: String? = null,
    @Json(name = "client_id")
    val clientId: String? = null,
    val roles: List<String>? = null,
    val state: String? = null
)
