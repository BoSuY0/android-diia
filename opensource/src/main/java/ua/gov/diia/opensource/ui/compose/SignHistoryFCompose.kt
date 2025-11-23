package ua.gov.diia.opensource.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.components.infrastructure.HomeScreenTab
import ua.gov.diia.ui_base.components.infrastructure.collectAsEffect
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.navigation.BaseNavigation

@AndroidEntryPoint
class SignHistoryFCompose : Fragment() {

    private var composeView: ComposeView? = null
    private val viewModel: SignHistoryVM by viewModels()

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
        composeView?.setContent {
            val topBar = viewModel.topBarData
            val body = viewModel.bodyData

            viewModel.navigation.collectAsEffect { navigation ->
                when (navigation) {
                    is BaseNavigation.Back -> findNavController().popBackStack()
                    else -> Unit
                }
            }

            HomeScreenTab(
                topBar = topBar,
                body = body,
                onEvent = onEvent@{ action: UIAction ->
                    if (action.actionKey == UIActionKeysCompose.LIST_ITEM_MLC ||
                        action.actionKey == UIActionKeysCompose.LIST_ITEM_GROUP_ORG
                    ) {
                        val targetId = action.data ?: action.action?.type
                        if (targetId?.startsWith("sign_history_") == true) {
                            findNavController().navigate(R.id.nav_contracts)
                            return@onEvent
                        }
                    }
                    viewModel.onUIAction(action)
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}
