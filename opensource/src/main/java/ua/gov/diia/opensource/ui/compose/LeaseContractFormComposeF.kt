package ua.gov.diia.opensource.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ua.gov.diia.ui_base.components.infrastructure.ServiceScreen
import ua.gov.diia.ui_base.components.infrastructure.collectAsEffect
import ua.gov.diia.ui_base.navigation.BaseNavigation

class LeaseContractFormComposeF : Fragment() {

    private var composeView: ComposeView? = null
    private val viewModel: LeaseContractFormComposeVM by viewModels()

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
            val toolbar = viewModel.toolbarData
            val body = viewModel.bodyData
            val bottom = viewModel.bottomData

            viewModel.navigation.collectAsEffect { navigation ->
                when (navigation) {
                    is BaseNavigation.Back -> {
                        findNavController().popBackStack()
                    }
                }
            }

            ServiceScreen(
                toolbar = toolbar,
                body = body,
                bottom = bottom,
                onEvent = viewModel::onUIAction
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}

