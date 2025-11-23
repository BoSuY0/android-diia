package ua.gov.diia.opensource.ui.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.compose.ui.platform.ComposeView
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.BorderStroke
import dagger.hilt.android.AndroidEntryPoint
import ua.gov.diia.opensource.R
import ua.gov.diia.ui_base.R as UiBaseR
import ua.gov.diia.ui_base.components.theme.Alabaster
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.BlackAlpha10
import ua.gov.diia.ui_base.components.theme.BlackAlpha54
import ua.gov.diia.ui_base.components.theme.BlackAlpha80
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.White
import ua.gov.diia.ui_base.components.theme.WhiteAlpha20
import kotlin.math.roundToInt

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
    val steps = listOf(
        DiiaIdStep(
            title = stringResource(id = R.string.diia_id_step_verify_title),
            description = stringResource(id = R.string.diia_id_step_verify_desc),
            icon = UiBaseR.drawable.ic_check_shield
        ),
        DiiaIdStep(
            title = stringResource(id = R.string.diia_id_step_sign_title),
            description = stringResource(id = R.string.diia_id_step_sign_desc),
            icon = UiBaseR.drawable.ic_doc_cert
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Alabaster)
    ) {
        DiiaIdHeader(onBackClick = onBackClick)
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = White,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.diia_id_description),
                            description = stringResource(id = R.string.diia_id_hint),
                            icon = UiBaseR.drawable.ic_info_about
                        )
                    }
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.diia_id_security_title),
                            description = stringResource(id = R.string.diia_id_security_desc),
                            icon = UiBaseR.drawable.ic_check_shield
                        )
                    }
                    item {
                        StepList(
                            title = stringResource(id = R.string.diia_id_title),
                            steps = steps
                        )
                    }
                }

                SwipeToSignBar(
                    title = stringResource(id = R.string.diia_id_swipe_title),
                    onSignSwipe = onSignSwipe
                )
            }
        }
    }
}

@Composable
private fun DiiaIdHeader(onBackClick: () -> Unit) {
    val headerBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0E0E0E), Color(0xFF161F2E))
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(WhiteAlpha20, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.diia_id_title),
                    style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.SemiBold),
                    color = White
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.diia_id_title),
                    style = DiiaTextStyle.heroText,
                    color = White
                )
                Text(
                    text = stringResource(id = R.string.diia_id_subtitle),
                    style = DiiaTextStyle.t1BigText,
                    color = WhiteAlpha20
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    description: String,
    icon: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, BlackAlpha10)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(BlackAlpha10, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Black
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                    color = Black
                )
                Text(
                    text = description,
                    style = DiiaTextStyle.t3TextBody,
                    color = BlackAlpha54
                )
            }
        }
    }
}

@Composable
private fun StepList(
    title: String,
    steps: List<DiiaIdStep>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, BlackAlpha10)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )

            AnimatedContent(
                targetState = steps,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut()).using(SizeTransform(clip = false))
                },
                label = "diiaIdSteps"
            ) { list ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    list.forEachIndexed { index, step ->
                        StepItem(
                            index = index + 1,
                            step = step
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    index: Int,
    step: DiiaIdStep
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF10141F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.Bold),
                color = White
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = step.title,
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )
            Text(
                text = step.description,
                style = DiiaTextStyle.t3TextBody,
                color = BlackAlpha54
            )
        }
        Icon(
            painter = painterResource(id = step.icon),
            contentDescription = null,
            tint = Black,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SwipeToSignBar(
    title: String,
    onSignSwipe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
            color = Black
        )
        SwipeToSignButton(
            text = stringResource(id = R.string.diia_id_swipe_to_sign),
            onSigned = onSignSwipe
        )
        Text(
            text = stringResource(id = R.string.diia_id_swipe_hint),
            style = DiiaTextStyle.t4TextSmallDescription,
            color = BlackAlpha80
        )
    }
}

@Composable
private fun SwipeToSignButton(
    text: String,
    onSigned: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val handleSize = 62.dp
    val handleSizePx = with(density) { handleSize.toPx() }
    var trackWidth by remember { mutableStateOf(0f) }
    val maxOffset by remember(trackWidth) {
        mutableStateOf((trackWidth - handleSizePx).coerceAtLeast(0f))
    }
    var offsetPx by remember { mutableFloatStateOf(0f) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(offsetPx, maxOffset) {
        if (!completed && maxOffset > 0f && offsetPx >= maxOffset) {
            completed = true
            onSigned()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .onSizeChanged { trackWidth = it.width.toFloat() }
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF111827), Color(0xFF0B1021))))
            .draggable(
                state = rememberDraggableState { delta ->
                    if (!completed) {
                        offsetPx = (offsetPx + delta).coerceIn(0f, maxOffset)
                    }
                },
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    if (!completed && offsetPx < maxOffset * 0.5f) {
                        offsetPx = 0f
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
            color = White
        )

        val offsetX = offsetPx.roundToInt()
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX, 0) }
                .size(handleSize)
                .clip(RoundedCornerShape(14.dp))
                .background(White)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = UiBaseR.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Black
            )
        }
    }
}

private data class DiiaIdStep(
    val title: String,
    val description: String,
    val icon: Int
)
