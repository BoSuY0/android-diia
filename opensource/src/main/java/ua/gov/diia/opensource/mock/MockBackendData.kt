package ua.gov.diia.opensource.mock

import android.util.Base64
import ua.gov.diia.auth_bankid.BankIdConst
import ua.gov.diia.auth_bankid.model.AuthBank
import ua.gov.diia.auth_bankid.model.AuthBanks
import ua.gov.diia.core.data.data_source.network.api.ApiSettings
import ua.gov.diia.core.data.data_source.network.api.notification.ApiNotificationsPublic
import ua.gov.diia.core.models.RefreshToken
import ua.gov.diia.core.models.SuccessResponse
import ua.gov.diia.core.models.Token
import ua.gov.diia.core.models.appversion.AppSettingsInfo
import ua.gov.diia.core.models.auth.FaceRecoConfig
import ua.gov.diia.core.models.auth.Fld
import ua.gov.diia.core.models.dialogs.TemplateDialogButton
import ua.gov.diia.core.models.dialogs.TemplateDialogData
import ua.gov.diia.core.models.dialogs.TemplateDialogModel
import ua.gov.diia.core.models.dialogs.TemplateDialogModelWithProcessCode
import ua.gov.diia.core.models.share.ShareDataResponse
import ua.gov.diia.core.models.share.ShareDataResponse.LinkAction
import ua.gov.diia.core.models.share.ShareDataResponse.OwnerRequestScreen
import ua.gov.diia.core.models.share.ShareDataResponse.Qr
import ua.gov.diia.feed.models.News
import ua.gov.diia.login.model.LoginToken
import ua.gov.diia.opensource.model.documents.Docs
import ua.gov.diia.publicservice.models.PublicServiceCategory
import ua.gov.diia.publicservice.models.PublicServiceTab
import ua.gov.diia.publicservice.models.PublicServicesCategories
import ua.gov.diia.verification.model.ActivityViewActionButton
import ua.gov.diia.verification.model.VerificationMethodsData
import ua.gov.diia.verification.model.VerificationMethodsData.DisabledMethod
import ua.gov.diia.verification.model.VerificationUrl
import ua.gov.diia.verification.ui.VerificationSchema
import ua.gov.diia.core.models.common_compose.general.DiiaResponse
import ua.gov.diia.core.models.ITN
import ua.gov.diia.documents.models.QRUrl
import ua.gov.diia.documents.models.VerificationCodesOrgResponse
import ua.gov.diia.ui_base.components.DiiaResourceIcon

/**
 * Centralized mock data used for a backend-less debug build.
 *
 * This file intentionally contains only plain data constructors.
 * You can wire it into the app via fake Retrofit implementations or repositories,
 * or use it from a custom OkHttp interceptor.
 */
object MockBackendData {

    // region Auth / tokens (ApiAuth, ApiLogin)

    private fun generateMockJwtToken(expiresInSeconds: Long = 3600L): String {
        val headerJson = """{"alg":"HS256","typ":"JWT"}"""
        val expSeconds = System.currentTimeMillis() / 1000L + expiresInSeconds
        val payloadJson = """{"exp":$expSeconds}"""
        val header = Base64.encodeToString(headerJson.toByteArray(), Base64.NO_WRAP)
        val payload = Base64.encodeToString(payloadJson.toByteArray(), Base64.NO_WRAP)
        val signature = "mock-signature"
        return "$header.$payload.$signature"
    }

    fun faceRecoConfig(isLowRamDevice: Boolean): FaceRecoConfig =
        FaceRecoConfig(
            fld = Fld(
                version = "1.0.0-mock",
                config = if (isLowRamDevice) """{"quality":"low"}""" else """{"quality":"high"}"""
            )
        )

    fun testToken(requestId: String?, userInfo: Map<String, String>): Token =
        Token(
            token = generateMockJwtToken(),
            template = null
        )

    fun refreshToken(oldToken: String?): RefreshToken =
        RefreshToken(
            token = generateMockJwtToken(),
            template = null
        )

    fun loginToken(processId: String): LoginToken =
        LoginToken(
            token = generateMockJwtToken()
        )

    fun serviceAccountToken(uuid: String, mobileUid: String): Token =
        Token(
            token = generateMockJwtToken(),
            template = null
        )

    // endregion

    // region Settings / notifications (ApiSettings, ApiNotificationsPublic)

    fun appSettingsInfo(): AppSettingsInfo =
        AppSettingsInfo(
            actions = emptyList()
        )

    fun sendAppVersionBody(): Any = object {}

    fun mockAppVersionNotification(): Unit = Unit

    fun mockPushTokenResponse(pushToken: ua.gov.diia.core.models.PushToken): Unit = Unit

    fun mockAppStatusResponse(appStatus: ua.gov.diia.core.models.AppStatus): Unit = Unit

    // endregion

    // region Verification / authorization (ApiVerification)

    fun verificationMethodsAuthorization(
        processId: String? = null,
        selectedMethod: String? = null
    ): VerificationMethodsData =
        VerificationMethodsData(
            title = "Виберіть спосіб авторизації",
            methods = listOf(
                BankIdConst.VERIFICATION_METHOD_BANK_ID,
                BankIdConst.VERIFICATION_METHOD_MONO,
                BankIdConst.VERIFICATION_METHOD_PRIVAT
            ),
            actionButton = ActivityViewActionButton(
                action = "continue"
            ),
            processId = processId ?: "mock-process-1",
            skipAuthMethods = false,
            template = null,
            disabledMethods = listOf(
                DisabledMethod(
                    description = "Тимчасово недоступний у мок-режимі",
                    code = BankIdConst.VERIFICATION_METHOD_MONO
                )
            )
        )

    fun verificationUrl(
        methodCode: String,
        processId: String,
        bankCode: String?
    ): VerificationUrl =
        VerificationUrl(
            authUrl = "https://mock.diia.gov.ua/auth/$methodCode?processId=$processId&bank=$bankCode",
            token = "mock-auth-url-token",
            template = null
        )

    fun completeVerificationStep(
        methodCode: String,
        requestId: String,
        processId: String,
        bankCode: String?
    ): TemplateDialogModelWithProcessCode =
        TemplateDialogModelWithProcessCode(
            processCode = 0,
            template = TemplateDialogModel(
                key = "mock-complete-$methodCode",
                type = "INFO",
                isClosable = true,
                data = TemplateDialogData(
                    icon = null,
                    title = "Мок: авторизація виконана",
                    description = "method=$methodCode, requestId=$requestId, processId=$processId, bank=$bankCode",
                    mainButton = TemplateDialogButton(
                        name = "OK",
                        action = "getToken"
                    ),
                    alternativeButton = null
                )
            )
        )

    // endregion

    // region BankID (ApiBankId)

    fun authBanks(): AuthBanks =
        AuthBanks(
            value = listOf(
                AuthBank(
                    id = "privatbank",
                    logoUrl = null,
                    name = "ПриватБанк",
                    workable = true
                ),
                AuthBank(
                    id = "monobank",
                    logoUrl = null,
                    name = "monobank",
                    workable = false
                ),
                AuthBank(
                    id = "oschad",
                    logoUrl = null,
                    name = "Ощадбанк",
                    workable = false
                )
            )
        )

    // endregion

    // region Login (ApiLogin)

    fun authenticationOtpScreen(
        processId: String,
        nfcAvailable: Boolean
    ): DiiaResponse =
        DiiaResponse(
            topGroup = null,
            body = emptyList(),
            centeredBody = null,
            bottomGroup = null,
            processCode = "0",
            template = null,
            ratingForm = null,
            nextStep = null,
            code = null
        )

    // endregion

    // region Documents (ApiDocs)

    fun docs(): Docs =
        Docs(
            driverLicense = null,
            documentsTypeOrder = emptyList()
        )

    fun docsManual(): ua.gov.diia.core.models.document.ManualDocs =
        ua.gov.diia.core.models.document.ManualDocs(
            listOf()
        )

    fun itn(): ITN =
        ITN(
            birthDay = null,
            currentDate = null,
            expirationDate = null,
            fName = null,
            itn = "0000000000",
            lName = null,
            mName = null
        )

    fun shareUrl(docName: String?, documentId: String?): QRUrl =
        QRUrl(
            id = "mock-share",
            link = "https://mock.diia.gov.ua/share/$docName/$documentId",
            shareCode = "MOCK-SHARE-CODE",
            timerText = "59:59",
            timerTime = 3599
        )

    fun verificationCodesOrg(
        documentType: String?,
        documentId: String?,
        localization: String
    ): VerificationCodesOrgResponse =
        VerificationCodesOrgResponse(
            verificationCodesOrg = null
        )

    // endregion

    // region Feed (ApiFeed)

    fun feedScreen(): DiiaResponse =
        DiiaResponse(
            topGroup = null,
            body = emptyList(),
            centeredBody = null,
            bottomGroup = null,
            processCode = "0",
            template = null,
            ratingForm = null,
            nextStep = null,
            code = null
        )

    fun newsList(): News =
        News(
            items = emptyList(),
            processCode = 0,
            template = null,
            total = 0
        )

    fun newsDetails(): DiiaResponse =
        DiiaResponse(
            topGroup = null,
            body = emptyList(),
            centeredBody = null,
            bottomGroup = null,
            processCode = "0",
            template = null,
            ratingForm = null,
            nextStep = null,
            code = null
        )

    fun enemyShareLink(): ShareDataResponse =
        ShareDataResponse(
            link = "https://mock.diia.gov.ua/share/enemy",
            ownerRequestScreen = OwnerRequestScreen(
                title = "Моковий екран шерингу",
                description = "Тут міг би бути реальний опис.",
                qr = Qr(
                    ttl = "3600",
                    value = "MOCK-QR-VALUE"
                ),
                linkAction = LinkAction(
                    icon = "",
                    name = "Відкрити посилання"
                )
            ),
            processCode = 0,
            template = null
        )

    // endregion

    // region Public services (ApiPublicServices)

    fun publicServices(): PublicServicesCategories {
        val tabs = listOf(
            PublicServiceTab(
                code = "popular",
                name = "Популярні послуги"
            )
        )
        val categories = listOf(
            PublicServiceCategory(
                code = "ic_document",
                sortOrder = 1,
                icon = DiiaResourceIcon.STACK.code,
                name = "Створити юридичний договір",
                status = ua.gov.diia.publicservice.models.CategoryStatus.active,
                visibleSearch = true,
                publicServices = listOf(
                    ua.gov.diia.publicservice.models.PublicService(
                        sortOrder = 1,
                        search = "договір оренди житла",
                        code = "legalLeaseContract",
                        name = "Договір оренди житла",
                        status = ua.gov.diia.publicservice.models.CategoryStatus.active,
                        contextMenu = null,
                        startFromDiiaCard = false
                    ),
                    ua.gov.diia.publicservice.models.PublicService(
                        sortOrder = 2,
                        search = "трудовий договір",
                        code = "legalEmploymentContract",
                        name = "Трудовий договір",
                        status = ua.gov.diia.publicservice.models.CategoryStatus.active,
                        contextMenu = null,
                        startFromDiiaCard = false
                    ),
                    ua.gov.diia.publicservice.models.PublicService(
                        sortOrder = 3,
                        search = "договір на надання послуг",
                        code = "legalServicesContract",
                        name = "Договір на надання послуг",
                        status = ua.gov.diia.publicservice.models.CategoryStatus.active,
                        contextMenu = null,
                        startFromDiiaCard = false
                    ),
                    ua.gov.diia.publicservice.models.PublicService(
                        sortOrder = 4,
                        search = "договір підряду",
                        code = "legalWorkContract",
                        name = "Договір підряду",
                        status = ua.gov.diia.publicservice.models.CategoryStatus.active,
                        contextMenu = null,
                        startFromDiiaCard = false
                    ),
                    ua.gov.diia.publicservice.models.PublicService(
                        sortOrder = 5,
                        search = "договір купівлі-продажу",
                        code = "legalSaleContract",
                        name = "Договір купівлі-продажу",
                        status = ua.gov.diia.publicservice.models.CategoryStatus.active,
                        contextMenu = null,
                        startFromDiiaCard = false
                    )
                ),
                tabCode = "popular",
                tabCodes = listOf("popular"),
                chips = null
            )
        )
        return PublicServicesCategories(
            categories = categories,
            tabs = tabs,
            additionalElements = null
        )
    }

    fun checkPromo(): TemplateDialogModelWithProcessCode =
        TemplateDialogModelWithProcessCode(
            processCode = 0,
            template = TemplateDialogModel(
                key = "mock-promo",
                type = "INFO",
                isClosable = true,
                data = TemplateDialogData(
                    icon = null,
                    title = "Мок акція",
                    description = "Тут могла б бути ваша реклама.",
                    mainButton = TemplateDialogButton(
                        name = "OK",
                        action = "close"
                    ),
                    alternativeButton = null
                )
            )
        )

    fun subscribeToBeta(segmentId: Int?): SuccessResponse =
        SuccessResponse(success = true)

    fun publicServicePortalUrl(serviceCode: String): DiiaResponse =
        DiiaResponse(
            topGroup = null,
            body = emptyList(),
            centeredBody = null,
            bottomGroup = null,
            processCode = "0",
            template = null,
            ratingForm = null,
            nextStep = null,
            code = null
        )

    // endregion
}
