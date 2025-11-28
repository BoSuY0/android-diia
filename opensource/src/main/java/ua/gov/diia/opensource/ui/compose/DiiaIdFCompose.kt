package ua.gov.diia.opensource.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ua.gov.diia.opensource.R

@AndroidEntryPoint
class DiiaIdFCompose : Fragment() {

    private var composeView: ComposeView? = null

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
            DiiaIdScreen(
                onBackClick = { findNavController().popBackStack() },
                onSignSwipe = { findNavController().navigate(R.id.nav_contracts) }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}

@Composable
private fun DiiaIdScreen(
    onBackClick: () -> Unit,
    onSignSwipe: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        // Menu removed intentionally
    }
}
