package ua.gov.diia.opensource.deeplinkprocessor

import androidx.navigation.NavDirections
import ua.gov.diia.core.models.SingleDeeplinkProcessor
import ua.gov.diia.core.models.deeplink.DeepLinkAction
import ua.gov.diia.core.models.deeplink.DeepLinkActionJoinContract
import ua.gov.diia.opensource.NavMainDirections

class DeepLinkActionJoinContractProcessor : SingleDeeplinkProcessor {
    override suspend fun handleDeepLinkAction(linkAction: DeepLinkAction): NavDirections? {
        val action = linkAction as DeepLinkActionJoinContract
        android.util.Log.d("DeepLinkJoinContract", "handleDeepLinkAction: sessionId=${action.sessionId}")
        return NavMainDirections.actionHomeFToContracts(
            sessionId = action.sessionId,
            openDetails = false,
            openCreationMenu = false
        )
    }

    override fun isHandled(action: DeepLinkAction): Boolean = action is DeepLinkActionJoinContract
}
