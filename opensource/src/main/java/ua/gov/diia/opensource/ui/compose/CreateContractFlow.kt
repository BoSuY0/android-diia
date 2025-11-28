package ua.gov.diia.opensource.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ua.gov.diia.opensource.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.window.Dialog
import ua.gov.diia.opensource.data.contracts.repo.ContractPartyField
import ua.gov.diia.opensource.data.contracts.repo.ContractPartyRole
import ua.gov.diia.opensource.data.contracts.repo.ContractPersonType
import ua.gov.diia.ui_base.components.DiiaResourceIcon
import ua.gov.diia.ui_base.components.infrastructure.DataActionWrapper
import ua.gov.diia.ui_base.components.infrastructure.event.UIAction
import ua.gov.diia.ui_base.components.infrastructure.event.UIActionKeysCompose
import ua.gov.diia.ui_base.components.infrastructure.utils.SidePaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.TopPaddingMode
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiIcon
import ua.gov.diia.ui_base.components.infrastructure.utils.resource.UiText
import ua.gov.diia.ui_base.components.molecule.list.ListItemMlcData
import ua.gov.diia.ui_base.components.organism.list.ListItemGroupOrg
import ua.gov.diia.ui_base.components.organism.list.ListItemGroupOrgData
import ua.gov.diia.ui_base.components.theme.Alabaster
import ua.gov.diia.ui_base.components.theme.AshGrey
import ua.gov.diia.ui_base.components.theme.BlackSqueeze
import ua.gov.diia.ui_base.components.theme.AzureRadiance
import ua.gov.diia.ui_base.components.theme.AzureMist
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.gradientBluePosition01
import ua.gov.diia.ui_base.components.theme.gradientBluePosition02
import ua.gov.diia.ui_base.components.theme.BlackAlpha10
import ua.gov.diia.ui_base.components.theme.BlackAlpha20
import ua.gov.diia.ui_base.components.theme.BlackAlpha30
import ua.gov.diia.ui_base.components.theme.BlackAlpha80
import ua.gov.diia.ui_base.components.theme.BlackAlpha54
import ua.gov.diia.ui_base.components.theme.WhiteAlpha30
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.TropicalBlue
import ua.gov.diia.ui_base.components.theme.White
import ua.gov.diia.ui_base.components.theme.WhiteAlpha20
import ua.gov.diia.ui_base.components.theme.WhiteAlpha70
import ua.gov.diia.ui_base.R as UiBaseR
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Data Models ---

// --- Screens ---

@Composable
fun LegalContractsScreen(
    contractCategories: List<ContractCategory> = emptyList(),
    onBackClick: () -> Unit,
    onContractSelected: (String) -> Unit,
    onAiChatClick: () -> Unit,
    isLoading: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredContracts = contractCategories.filter { 
        it.title.contains(searchQuery, ignoreCase = true) 
    }

    // Використовуємо стандартні компоненти Дії
    val listData = ListItemGroupOrgData(
        itemsList = filteredContracts.map { contract ->
            ListItemMlcData(
                id = contract.id,
                label = UiText.DynamicString(contract.title),
                iconLeft = UiIcon.DrawableResource(DiiaResourceIcon.STACK_WHITE.code),
                iconRight = UiIcon.DrawableResource(DiiaResourceIcon.ELLIPSE_ARROW_RIGHT.code),
                action = DataActionWrapper(type = contract.id),
                componentId = UiText.DynamicString("contract_item_${contract.id}")
            )
        },
        componentId = UiText.DynamicString("contracts_list"),
        paddingTop = TopPaddingMode.NONE,
        paddingHorizontal = SidePaddingMode.LARGE
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSqueeze) // Стандартний фон Дії
            .statusBarsPadding()
    ) {
        // Navigation Panel - стандартний компонент
        DiiaNavigationPanel(
            title = stringResource(R.string.contracts_create_legal_title),
            onBackClick = onBackClick
        )

        // Основний контент
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок для вибору категорії
            item {
                Text(
                    text = "Оберіть категорію",
                    style = DiiaTextStyle.h2MediumHeading,
                    color = Black,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Search Input
            item {
                DiiaSearchInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = stringResource(R.string.contracts_search_placeholder),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Список категорій - стандартний ListItemGroupOrg
            if (filteredContracts.isNotEmpty()) {
                item {
                    ListItemGroupOrg(
                        data = listData,
                        onUIAction = { action: UIAction ->
                            if (action.actionKey == UIActionKeysCompose.LIST_ITEM_GROUP_ORG ||
                                action.actionKey == UIActionKeysCompose.LIST_ITEM_MLC
                            ) {
                                val id = action.data ?: action.action?.type
                                if (!id.isNullOrEmpty()) {
                                    onContractSelected(id)
                                }
                            }
                        }
                    )
                }
            }

            // AI Promo Banner
            item {
                DiiaAiPromoBanner(
                    title = stringResource(R.string.contracts_ai_promo_title),
                    subtitle = stringResource(R.string.contracts_ai_promo_subtitle),
                    onClick = onAiChatClick,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
fun ContractAiChatScreen(
    messages: List<ContractChatMessage>,
    isSending: Boolean,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onActionClick: (ChatAction) -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val hasUserMessages = messages.any { it.isUser }
    val sendScale by animateFloatAsState(
        targetValue = if (inputText.isNotBlank() && !isSending) 1.0f else 0.94f,
        animationSpec = tween(180, easing = FastOutSlowInEasing)
    )

    fun sendMessage(content: String) {
        val text = content.trim()
        if (text.isEmpty() || isSending) return
        inputText = ""
        coroutineScope.launch { onSendMessage(text) }
    }

    val quickPrompts = listOf(
        stringResource(R.string.contracts_prompt_rent_title) to stringResource(R.string.contracts_prompt_rent_subtitle),
        stringResource(R.string.contracts_prompt_employment_title) to stringResource(R.string.contracts_prompt_employment_subtitle),
        stringResource(R.string.contracts_prompt_liability_title) to stringResource(R.string.contracts_prompt_liability_subtitle)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSqueeze)
            .statusBarsPadding()
    ) {
        DiiaNavigationPanel(
            title = stringResource(R.string.contracts_ai_chat_title),
            onBackClick = onBackClick,
            trailingContent = {
                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_doc_edit_adress),
                        contentDescription = "Новий чат",
                        tint = Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages, key = { _, item -> item.id }) { _, message ->
                ContractChatMessageBubble(
                    message = message,
                    onActionClick = onActionClick
                )
            }
        }

        // Нижній блок вводу - стиль Дії
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            color = White,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Швидкі промпти
                AnimatedVisibility(
                    visible = !hasUserMessages,
                    enter = fadeIn(tween(250, easing = FastOutSlowInEasing)) + expandVertically(),
                    exit = fadeOut(tween(200, easing = FastOutLinearInEasing)) + shrinkVertically()
                ) {
                    QuickPromptPicker(
                        quickPrompts = quickPrompts,
                        onPick = { selection -> sendMessage(selection) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFEAF3FF), Color(0xFFF9F7FF))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    )
                }

                // Поле вводу
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Текстове поле - стиль Дії
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = White,
                        border = BorderStroke(1.dp, BlackAlpha10)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.contracts_ai_chat_placeholder),
                                        style = DiiaTextStyle.t2TextDescription,
                                        color = BlackAlpha54
                                    )
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = DiiaTextStyle.t2TextDescription.copy(color = Black),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Send
                                    ),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onSend = { sendMessage(inputText) }
                                    )
                                )
                            }
                        }
                    }
                    
                    // Кнопка надсилання - стиль Дії
                    FilledIconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isSending,
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Black,
                            contentColor = White,
                            disabledContainerColor = BlackAlpha10,
                            disabledContentColor = BlackAlpha54
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = sendScale
                                scaleY = sendScale
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = UiBaseR.drawable.ic_role_arrow_up),
                            contentDescription = "Надіслати",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegalContractsIntroScreen(
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    onSkipIntroChange: (Boolean) -> Unit = {}
) {
    val introText = """
        Тут ви зможете швидко зібрати юридичний договір. Що підготувати:
        • дані сторін: ПІБ/назва, контакти, РНОКПП або ЄДРПОУ;
        • адресу та короткий опис об'єкта (квартира, будинок тощо);
        • фінанси: щомісячну суму, завдаток (якщо є), дедлайн оплати;
        • строки дії договору (початок, кінець або безстроково).

        Порада: якщо заповнюєте за дві сторони — одразу внесіть їхні контакти, щоб відправити посилання на погодження. Якщо тільки за себе — достатньо ваших даних, другу сторону можна запросити пізніше.

        У демо-версії все зберігається локально на вашому пристрої. За потреби зможете змінити будь-яке поле перед відправкою чи підписанням.
    """.trimIndent()

    var skipIntro by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSqueeze)
            .statusBarsPadding()
    ) {
        // Navigation Panel - стиль Дії
        DiiaNavigationPanel(
            title = "Юридичні договори",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Заголовок
                    Text(
                        text = stringResource(R.string.contracts_intro_welcome),
                        style = DiiaTextStyle.h2MediumHeading,
                        color = Black
                    )
                    // Картка з інформацією - стиль Дії
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = White
                    ) {
                        Text(
                            text = introText,
                            style = DiiaTextStyle.t2TextDescription.copy(
                                lineHeight = DiiaTextStyle.t2TextDescription.fontSize * 1.4f
                            ),
                            color = Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Нижній блок з кнопкою - стиль Дії
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Чекбокс "Більше не показувати"
            DiiaCheckbox(
                checked = skipIntro,
                onCheckedChange = {
                    skipIntro = it
                    onSkipIntroChange(it)
                },
                label = stringResource(R.string.contracts_intro_skip),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Кнопка "Розпочати" - стиль Дії
            DiiaPrimaryButton(
                text = stringResource(R.string.contracts_intro_start),
                onClick = onStartClick
            )
        }
    }
}

@Composable
private fun SkipIntroToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(14.dp)
    val indicatorShape = RoundedCornerShape(6.dp)
    val containerColor by animateColorAsState(
        targetValue = if (checked) Color(0xFFF2F5FF) else Color(0xFFF8FAFD),
        label = "skipToggleBg"
    )
    val borderColor = if (checked) Color(0xFF7E5CFF) else BlackAlpha10
    val indicatorInteraction = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape),
        shape = containerShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) rowScope@{
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(indicatorShape)
                    .background(if (checked) Black else White)
                    .border(
                        width = 1.5.dp,
                        color = if (checked) Color(0xFF7E5CFF) else BlackAlpha20,
                        shape = indicatorShape
                    )
                    .clickable(
                        interactionSource = indicatorInteraction,
                        indication = LocalIndication.current,
                        role = Role.Checkbox,
                        onClick = { onCheckedChange(!checked) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                with(this@rowScope) {
                    AnimatedVisibility(
                        visible = checked,
                        enter = fadeIn(animationSpec = tween(150)) + expandVertically(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ),
                        exit = fadeOut(animationSpec = tween(100)) + shrinkVertically(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.contracts_intro_skip),
                style = DiiaTextStyle.t3TextBody,
                color = Black
            )
        }
    }
}

@Composable
fun ContractFillingModeScreen(
    onBackClick: () -> Unit,
    onModeSelected: (Boolean) -> Unit
) {
    val options = listOf(
        ModeOption(
            id = "both_sides",
            title = stringResource(R.string.contracts_filling_mode_both_title),
            subtitle = stringResource(R.string.contracts_filling_mode_both_subtitle),
            cta = stringResource(R.string.contracts_filling_mode_both_cta)
        ),
        ModeOption(
            id = "one_side",
            title = stringResource(R.string.contracts_filling_mode_one_title),
            subtitle = stringResource(R.string.contracts_filling_mode_one_subtitle),
            cta = stringResource(R.string.contracts_filling_mode_one_cta)
        )
    )
    var selectedId by remember { mutableStateOf(options.first().id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSqueeze)
            .statusBarsPadding()
    ) {
        // Navigation Panel - стиль Дії
        DiiaNavigationPanel(
            title = stringResource(R.string.contracts_filling_mode_title),
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.contracts_format_title),
                        style = DiiaTextStyle.h2MediumHeading,
                        color = Black
                    )
                    Text(
                        text = stringResource(R.string.contracts_format_subtitle),
                        style = DiiaTextStyle.t2TextDescription,
                        color = BlackAlpha54
                    )
                }
            }

            items(options) { option ->
                DiiaOptionCard(
                    title = option.title,
                    subtitle = option.subtitle,
                    cta = option.cta,
                    isSelected = selectedId == option.id,
                    onClick = {
                        selectedId = option.id
                        when (option.id) {
                            "both_sides" -> onModeSelected(true)
                            "one_side" -> onModeSelected(false)
                        }
                    }
                )
            }
        }
    }
}

private data class ModeOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val cta: String
)

@Composable
fun ContractRoleMenuScreen(
    roles: List<ContractPartyRole>,
    personTypes: List<ContractPersonType>,
    clientId: String,
    isBothSides: Boolean,
    onBackClick: () -> Unit,
    onRoleSelected: (String) -> Unit
) {
    val uiPersonTypes = remember(personTypes) {
        personTypes.map { it.toUiPersonType() }
    }
    val roleForms = remember(roles, uiPersonTypes, clientId) {
        roles.map { role ->
            val allowedIds = role.allowedPersonTypes.takeIf { it.isNotEmpty() } ?: uiPersonTypes.map { it.id }
            val allowedLabels = uiPersonTypes.filter { allowedIds.contains(it.id) }.joinToString(", ") { it.label }
            val occupiedByOther = role.claimedBy != null && role.claimedBy != clientId
            val ownedByMe = role.claimedBy == clientId
            RoleForm(
                id = role.id,
                title = role.label.ifBlank { role.id },
                description = if (allowedLabels.isNotEmpty()) "Доступні типи: $allowedLabels" else "Оберіть тип сторони",
                isOccupied = occupiedByOther,
                isOwnedByMe = ownedByMe
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSqueeze)
            .statusBarsPadding()
    ) {
        // Navigation Panel - стиль Дії
        DiiaNavigationPanel(
            title = stringResource(R.string.contracts_role_menu_title),
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.contracts_role_menu_heading),
                        style = DiiaTextStyle.h2MediumHeading,
                        color = Black
                    )
                    Text(
                        text = stringResource(if (isBothSides) R.string.contracts_role_menu_subtitle_both else R.string.contracts_role_menu_subtitle_one),
                        style = DiiaTextStyle.t2TextDescription,
                        color = BlackAlpha54
                    )
                }
            }

            items(roleForms) { role ->
                DiiaRoleCard(
                    title = role.title,
                    description = role.description,
                    isOccupied = role.isOccupied,
                    isOwnedByMe = role.isOwnedByMe,
                    onClick = { 
                        if (!role.isOccupied || role.isOwnedByMe) {
                            onRoleSelected(role.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RoleMenuCard(
    role: RoleForm,
    onSelect: () -> Unit
) {
    val statusText = when {
        role.isOwnedByMe -> stringResource(R.string.contracts_role_your_role)
        role.isOccupied -> stringResource(R.string.contracts_role_occupied)
        else -> stringResource(R.string.contracts_role_free)
    }
    val statusColor = when {
        role.isOwnedByMe -> Color(0xFF2196F3) // Blue for owned
        role.isOccupied -> Color(0xFFE74C3C)
        else -> Color(0xFF1E9E55)
    }
    val alphaBg = if (role.isOccupied && !role.isOwnedByMe) 0.06f else 0.12f
    val isClickable = !role.isOccupied || role.isOwnedByMe
    Surface(
        onClick = onSelect,
        enabled = isClickable,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        color = White,
        border = BorderStroke(1.dp, if (!isClickable) BlackAlpha10 else BlackAlpha20)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = role.title,
                    style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                    color = Black
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = alphaBg))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                        color = statusColor
                    )
                }
            }
            Text(
                text = role.description,
                style = DiiaTextStyle.t3TextBody,
                color = BlackAlpha54
            )
        }
    }
}

@Composable
private fun ModeCard(
    option: ModeOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val accent = BlackAlpha80
    val cardBorder = if (isSelected) BlackAlpha20 else BlackAlpha10
    val cardColor = if (isSelected) White.copy(alpha = 0.98f) else White
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        color = cardColor,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = option.title,
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )
            Text(
                text = option.subtitle,
                style = DiiaTextStyle.t3TextBody,
                color = BlackAlpha54
            )
            ModeCta(
                text = option.cta,
                highlightColor = accent
            )
        }
    }
}

@Composable
private fun ModeCta(
    text: String,
    highlightColor: Color
) {
    Text(
        text = text,
        style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
        color = highlightColor
    )
}

@Composable
fun ContractRoleSelectionScreen(
    contractType: String,
    roles: List<ContractPartyRole>,
    personTypes: List<ContractPersonType>,
    contractFields: List<ContractPartyField> = emptyList(),
    partyContext: PartyContextFields?,
    clientId: String,
    onBackClick: () -> Unit,
    onSaveSuccess: (ContractUiModel) -> Unit,
    onRemoteSave: suspend (roleId: String, personTypeId: String, fields: Map<String, String>) -> ContractUiModel? = { _, _, _ -> null },
    onPartyContextChanged: suspend (roleId: String, personTypeId: String) -> Unit = { _, _ -> },
    isBothSides: Boolean,
    initialSelectedRoleId: String? = null,
    isLoading: Boolean = false,
    backendError: String? = null,
    onClearError: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiPersonTypes = remember(roles, personTypes) {
        when {
            personTypes.isNotEmpty() -> {
                // Персон-типи є, але поля беремо з roles для більш точного відображення
                val roleFieldsMap = roles.associate { role ->
                    (role.personType ?: role.id) to role.fields.map { it.toUiField() }
                }
                personTypes.map { pt ->
                    val fieldsFromRole = roleFieldsMap[pt.id]?.takeIf { it.isNotEmpty() }
                    if (fieldsFromRole != null) {
                        pt.toUiPersonType().copy(fields = fieldsFromRole)
                    } else {
                        pt.toUiPersonType()
                    }
                }
            }
            roles.any { it.fields.isNotEmpty() } -> {
                roles.map { role ->
                    PersonType(
                        id = role.personType ?: role.id,
                        label = role.label.ifBlank { role.personType ?: role.id },
                        fields = role.fields.map { it.toUiField() }
                    )
                }
            }
            else -> emptyList()
        }
    }
    val rolesMap = roles.associateBy { it.id }
    val hasSchemaPersonTypes = personTypes.isNotEmpty()
    val roleForms = remember(roles, uiPersonTypes, clientId) {
        roles.map { role ->
            val allowedIds = role.allowedPersonTypes.takeIf { it.isNotEmpty() } ?: uiPersonTypes.map { it.id }
            val allowedLabels = uiPersonTypes.filter { allowedIds.contains(it.id) }.joinToString(", ") { it.label }
            val occupied = role.claimedBy != null && role.claimedBy != clientId
            RoleForm(
                id = role.id,
                title = role.label.ifBlank { role.id },
                description = if (allowedLabels.isNotEmpty()) "Доступні типи: $allowedLabels" else "Оберіть тип сторони",
                isOccupied = occupied
            )
        }
    }
    fun allowedTypesForRole(roleId: String): List<PersonType> {
        val allowedIds = if (hasSchemaPersonTypes) {
            rolesMap[roleId]?.allowedPersonTypes?.takeIf { it.isNotEmpty() } ?: uiPersonTypes.map { it.id }
        } else {
            listOf(rolesMap[roleId]?.personType ?: roleId)
        }
        return uiPersonTypes.filter { allowedIds.contains(it.id) }.ifEmpty { uiPersonTypes }
    }
    fun personTypesForRole(roleId: String): List<PersonType> {
        val base = allowedTypesForRole(roleId)
        val contextMatch = partyContext?.takeIf { it.roleId == roleId }
        // Беремо поля з ролі як fallback, якщо partyContext недоступний
        val roleFields = rolesMap[roleId]?.fields?.map { it.toUiField() }
        val roleLabel = rolesMap[roleId]?.label ?: roleId
        val rolePersonType = rolesMap[roleId]?.personType ?: "individual"
        
        return when {
            contextMatch != null && contextMatch.fields.isNotEmpty() -> {
                val contextFields = contextMatch.fields.map { it.toUiField() }
                if (base.isNotEmpty()) {
                    base.map { type ->
                        if (type.id == contextMatch.personTypeId) type.copy(fields = contextFields) else type
                    }
                } else {
                    // Якщо base порожній, створюємо персон-тип з полями контексту
                    listOf(PersonType(id = contextMatch.personTypeId, label = roleLabel, fields = contextFields))
                }
            }
            roleFields != null && roleFields.isNotEmpty() -> {
                // Використовуємо поля з ролі як fallback
                val selectedType = rolesMap[roleId]?.personType ?: base.firstOrNull()?.id ?: rolePersonType
                if (base.isNotEmpty()) {
                    base.map { type ->
                        if (type.id == selectedType || base.size == 1) type.copy(fields = roleFields) else type
                    }
                } else {
                    // Якщо base порожній, створюємо персон-тип з полями ролі
                    listOf(PersonType(id = selectedType, label = roleLabel, fields = roleFields))
                }
            }
            else -> base
        }
    }

    // For new contracts, auto-select first role; for editing, use provided role or let user choose
    var selectedRoleId by rememberSaveable { 
        mutableStateOf(initialSelectedRoleId.orEmpty())
    }

    // If the screen was opened before roles arrived (e.g. edit flow), align the selection once data is here
    LaunchedEffect(initialSelectedRoleId, roleForms) {
        val claimedByMe = roles.firstOrNull { it.claimedBy == clientId }?.id
        when {
            selectedRoleId.isNotBlank() -> {
                // If previously selected role disappeared (unlikely), fallback to first available
                if (roleForms.isNotEmpty() && roleForms.none { it.id == selectedRoleId }) {
                    selectedRoleId = roleForms.firstOrNull()?.id.orEmpty()
                }
            }
            !initialSelectedRoleId.isNullOrBlank() && roleForms.any { it.id == initialSelectedRoleId } -> {
                selectedRoleId = initialSelectedRoleId
            }
            !claimedByMe.isNullOrBlank() && roleForms.any { it.id == claimedByMe } -> {
                selectedRoleId = claimedByMe
            }
            roleForms.isNotEmpty() -> {
                selectedRoleId = roleForms.first().id
            }
        }
    }
    val selectedPersonType = remember {
        mutableStateMapOf<String, String>().apply {
            roleForms.forEach { role ->
                val allowed = allowedTypesForRole(role.id)
                val preferred = rolesMap[role.id]?.personType
                val resolved = preferred?.takeIf { allowed.any { type -> type.id == preferred } }
                    ?: allowed.firstOrNull()?.id.orEmpty()
                put(role.id, resolved)
            }
        }
    }
    val roleFieldValues = remember { mutableStateMapOf<String, SnapshotStateMap<String, String>>() }
    // Ініціалізуємо значення договору безпосередньо з contractFields (ключ = key -> value)
    val contractTermsValues = remember { mutableStateMapOf<String, String>() }
    val contractTermsFieldList = remember(contractFields) {
        val backendFields = contractFields.takeIf { it.isNotEmpty() }?.map { it.toUiField() }
        backendFields ?: emptyList()
    }
    
    // Застосовуємо збережені значення з contractFields (приходять з бекенду)
    // Використовуємо snapshotFlow для більш надійної синхронізації
    LaunchedEffect(contractFields) {
        android.util.Log.d("ContractFlow", "LaunchedEffect(contractFields): size=${contractFields.size}")
        contractFields.forEach { field ->
            val fieldValue = field.value
            android.util.Log.d("ContractFlow", "  Field: key=${field.key}, value=$fieldValue, status=${field.status}")
            if (!fieldValue.isNullOrEmpty()) {
                contractTermsValues[field.key] = fieldValue
                android.util.Log.d("ContractFlow", "  -> Saved to contractTermsValues[${field.key}] = $fieldValue")
            }
        }
        android.util.Log.d("ContractFlow", "contractTermsValues after update: $contractTermsValues")
    }
    
    // Додатковий SideEffect для синхронізації значень при кожній recomposition
    SideEffect {
        contractFields.forEach { field ->
            val fieldValue = field.value
            if (!fieldValue.isNullOrEmpty() && contractTermsValues[field.key] != fieldValue) {
                contractTermsValues[field.key] = fieldValue
            }
        }
    }
    
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var errorTimestamp by remember { mutableStateOf(0L) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastSavedContract by remember { mutableStateOf<ContractUiModel?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val contractTermsSectionId = "contract_terms"

    LaunchedEffect(roleForms, uiPersonTypes) {
        roleForms.forEach { role ->
            val allowed = allowedTypesForRole(role.id)
            val current = selectedPersonType[role.id]
            if (current.isNullOrEmpty() || allowed.none { it.id == current }) {
                val preferred = rolesMap[role.id]?.personType
                val resolved = preferred?.takeIf { allowed.any { type -> type.id == preferred } }
                    ?: allowed.firstOrNull()?.id.orEmpty()
                selectedPersonType[role.id] = resolved
            }
        }
        // For new contracts only (not editing), auto-select first role if none selected
        if (initialSelectedRoleId == null && selectedRoleId.isBlank() && roleForms.isNotEmpty()) {
            selectedRoleId = roleForms.first().id
        }
    }

    val currentPersonTypeId = selectedPersonType[selectedRoleId]

    // Завантажуємо дані при кожному відкритті екрану та при зміні ролі/типу
    LaunchedEffect(Unit, selectedRoleId, currentPersonTypeId) {
        if (!selectedRoleId.isNullOrEmpty() && !currentPersonTypeId.isNullOrEmpty()) {
            onPartyContextChanged(selectedRoleId, currentPersonTypeId.orEmpty())
        }
    }

    // Допоміжна функція для нормалізації ключів (видалення префікса ролі)
    fun normalizeFieldKey(roleId: String, key: String): String {
        return when {
            key.startsWith("$roleId.") -> key.removePrefix("$roleId.")
            key.contains(".") -> key.substringAfterLast(".")
            else -> key
        }
    }
    
    LaunchedEffect(partyContext) {
        if (partyContext != null &&
            partyContext.roleId == selectedRoleId &&
            partyContext.personTypeId == currentPersonTypeId
        ) {
            val fieldValues = roleFieldValues.getOrPut(selectedRoleId) { mutableStateMapOf() }
            val contractKeys = contractTermsFieldList.map { it.key }.toSet()
            partyContext.fields.forEach { field ->
                if (!field.value.isNullOrEmpty()) {
                    // Нормалізуємо ключ - бекенд може повертати "lessor.name", а UI очікує "name"
                    val normalizedKey = normalizeFieldKey(partyContext.roleId, field.key)
                    fieldValues[normalizedKey] = field.value.orEmpty()
                    if (contractKeys.contains(normalizedKey)) {
                        contractTermsValues[normalizedKey] = field.value.orEmpty()
                    }
                }
            }
        }
    }

    LaunchedEffect(saveSuccessMessage) {
        if (saveSuccessMessage != null) {
            delay(2400)
            saveSuccessMessage = null
        }
    }

    // Застосовуємо значення полів з roles (приходять з бекенду через sessionParties)
    LaunchedEffect(roles, partyContext) {
        android.util.Log.d("ContractFlow", "LaunchedEffect(roles, partyContext): roles.size=${roles.size}, partyContext=$partyContext")
        val contractKeys = contractTermsFieldList.map { it.key }.toSet()
        roles.forEach { role ->
            android.util.Log.d("ContractFlow", "  Role: id=${role.id}, fields.size=${role.fields.size}")
            val initialFields = role.fields.filter { !it.value.isNullOrEmpty() }
            android.util.Log.d("ContractFlow", "  Role ${role.id}: initialFields with value: ${initialFields.size}")
            if (initialFields.isNotEmpty()) {
                val stateMap = roleFieldValues.getOrPut(role.id) { mutableStateMapOf() }
                initialFields.forEach { field ->
                    val value = field.value.orEmpty()
                    // Нормалізуємо ключ - бекенд може повертати "lessor.name", а UI очікує "name"
                    val normalizedKey = normalizeFieldKey(role.id, field.key)
                    stateMap[normalizedKey] = value
                    android.util.Log.d("ContractFlow", "    -> roleFieldValues[${role.id}][$normalizedKey] = $value")
                    if (contractKeys.contains(normalizedKey)) {
                        contractTermsValues[normalizedKey] = value
                    }
                }
            }
            // Логуємо всі поля ролі для діагностики
            role.fields.forEach { field ->
                android.util.Log.d("ContractFlow", "    Field: key=${field.key}, value=${field.value}, status=${field.status}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackSqueeze)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            DiiaNavigationPanel(
                title = stringResource(R.string.contracts_role_title),
                onBackClick = onBackClick
            )

            val contentModifier = Modifier
                .weight(1f)
                .fillMaxWidth()

            // Умови договору доступні для будь-якої ролі, оскільки всі ролі рівноправні
            val hasContractTerms = contractTermsFieldList.isNotEmpty()
            val formSections = remember(roleForms, isBothSides, selectedRoleId, hasContractTerms) {
                val sections = mutableListOf<FormSection>()
                val multiRoleMode = isBothSides
                if (multiRoleMode) {
                    roleForms.forEach {
                        sections.add(FormSection(id = it.id, title = it.title, isContractTerms = false))
                    }
                    if (hasContractTerms) {
                        sections.add(
                            FormSection(
                                id = contractTermsSectionId,
                                title = context.getString(R.string.contracts_terms_title),
                                isContractTerms = true
                            )
                        )
                    }
                } else {
                    val roleId = selectedRoleId
                    if (roleId.isNotBlank()) {
                        val roleTitle = roleForms.firstOrNull { it.id == roleId }?.title ?: roleId
                        sections.add(
                            FormSection(
                                id = roleId,
                                title = roleTitle,
                                isContractTerms = false
                            )
                        )
                        if (hasContractTerms) {
                            sections.add(
                                FormSection(
                                    id = contractTermsSectionId,
                                    title = context.getString(R.string.contracts_terms_title),
                                    isContractTerms = true
                                )
                            )
                        }
                    }
                }
                sections
            }
            var selectedSectionId by remember {
                mutableStateOf(
                    selectedRoleId.takeIf { id ->
                        id.isNotBlank() && formSections.any { it.id == id }
                    } ?: formSections.firstOrNull()?.id.orEmpty()
                )
            }
            LaunchedEffect(formSections) {
                val preferredSection = selectedRoleId.takeIf { id ->
                    id.isNotBlank() && formSections.any { it.id == id }
                }
                selectedSectionId = preferredSection ?: formSections.firstOrNull()?.id.orEmpty()
            }

            Column(
                modifier = contentModifier
                    .background(BlackSqueeze)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IdentityReminderNote()

                if (roleForms.isEmpty()) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Text(
                            text = backendError ?: "Не вдалося завантажити ролі. Спробуйте повернутися та відкрити ще раз.",
                            style = DiiaTextStyle.t2TextDescription,
                            color = BlackAlpha80,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Не будуємо решту UI, якщо немає ролей
                    return@Column
                }

                val showSectionSelector = formSections.size > 1
                if (showSectionSelector) {
                    SectionSelector(
                        sections = formSections,
                        selectedSectionId = selectedSectionId,
                        onSelect = { sectionId ->
                            selectedSectionId = sectionId
                            if (sectionId != contractTermsSectionId) {
                                selectedRoleId = sectionId
                            }
                        }
                    )
                }

                // Логуємо стан перед відображенням
                android.util.Log.d("ContractFlow", "Rendering section: selectedSectionId=$selectedSectionId, contractTermsSectionId=$contractTermsSectionId")
                android.util.Log.d("ContractFlow", "  formSections: ${formSections.map { "${it.id}:${it.title}" }}")
                android.util.Log.d("ContractFlow", "  contractTermsFieldList.size=${contractTermsFieldList.size}")
                android.util.Log.d("ContractFlow", "  contractTermsValues=$contractTermsValues")
                
                when {
                    selectedSectionId == contractTermsSectionId -> {
                        android.util.Log.d("ContractFlow", "  -> Showing ContractTermsCard with ${contractTermsFieldList.size} fields")
                        ContractTermsCard(
                            values = contractTermsValues,
                            fields = contractTermsFieldList
                        )
                    }
                    selectedSectionId.isNotBlank() -> {
                        val role = roleForms.firstOrNull { it.id == selectedSectionId }
                        android.util.Log.d("ContractFlow", "  -> Showing RoleCardWithForm for role=${role?.id}, personTypes=${personTypesForRole(role?.id.orEmpty()).size}")
                        if (role != null) {
                            val availableTypes = personTypesForRole(role.id)
                            android.util.Log.d("ContractFlow", "    availableTypes: ${availableTypes.map { "${it.id}: ${it.fields.size} fields" }}")
                            RoleCardWithForm(
                                role = role,
                                personTypes = availableTypes,
                                selectedPersonTypeId = selectedPersonType[role.id] ?: availableTypes.firstOrNull()?.id.orEmpty(),
                                onSelectPersonType = { selectedPersonType[role.id] = it },
                                values = roleFieldValues.getOrPut(role.id) { mutableStateMapOf() },
                                isSelected = true,
                                isExpanded = true,
                                onToggle = {}
                            )
                        }
                    }
                }
            }

            // Нижня панель з кнопкою на тому ж рівні що і форма (без тіней)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Показуємо помилки від бекенду (валідація полів) та локальні помилки
                val displayError = backendError ?: validationError
                when {
                    displayError != null -> ErrorBanner(
                        message = displayError,
                        showTimestamp = errorTimestamp,
                        onDismiss = {
                            validationError = null
                            onClearError()
                        }
                    )
                    saveSuccessMessage != null -> SaveSuccessBanner(message = saveSuccessMessage!!)
                }

                val currentRole = roleForms.firstOrNull { it.id == selectedRoleId }
                val currentPersonTypeId = currentRole?.let {
                    selectedPersonType[it.id] ?: personTypesForRole(it.id).firstOrNull()?.id.orEmpty()
                }.orEmpty()
                val contractTermsVisible = formSections.any { it.id == contractTermsSectionId }

                Button(
                    onClick = {
                        if (isLoading) return@Button
                        val role = currentRole ?: return@Button
                        if (currentPersonTypeId.isEmpty()) return@Button
                        val fieldValues = roleFieldValues.getOrPut(role.id) { mutableStateMapOf() }.toMap()
                        val currentPerson = personTypesForRole(role.id).firstOrNull { it.id == currentPersonTypeId }
                        val missingRequired = currentPerson?.fields.orEmpty()
                            .filter { it.required && fieldValues[it.key].orEmpty().trim().isEmpty() }
                        val missingTerms = if (contractTermsVisible) {
                            contractTermsFieldList.filter { it.required && contractTermsValues[it.key].orEmpty().trim().isEmpty() }
                        } else emptyList()
                        val missingAll = (missingRequired + missingTerms).distinctBy { it.key }
                        if (missingAll.isNotEmpty()) {
                            // Обмежуємо кількість полів до 3, щоб не перевантажувати UI
                            val displayFields = missingAll.take(3)
                            val suffix = if (missingAll.size > 3) " та ще ${missingAll.size - 3}..." else ""
                            validationError = "Заповніть обов'язкові поля: " + displayFields.joinToString(", ") { it.label } + suffix
                            errorTimestamp = System.currentTimeMillis()
                            return@Button
                        }
                        validationError = null
                        onClearError() // Очищаємо помилку від бекенду перед новим збереженням
                        val mergedFields = fieldValues.toMutableMap().apply {
                            if (contractTermsVisible) {
                                putAll(contractTermsValues)
                            }
                        }
                        coroutineScope.launch {
                            val saved = runCatching {
                                onRemoteSave(role.id, currentPersonTypeId, mergedFields)
                            }.onFailure { error ->
                                validationError = error.message ?: context.getString(R.string.contracts_error_session_create)
                                errorTimestamp = System.currentTimeMillis()
                            }.getOrNull()
                            if (saved == null) {
                                // Якщо saved == null, то або виникла помилка (буде в validationError),
                                // або є помилки валідації від бекенду (будуть в backendError через _error)
                                // Валідні поля збережено на сервері
                                return@launch
                            }
                            lastSavedContract = saved
                            saveSuccessMessage = context.getString(R.string.contracts_save_success)
                            showSuccessDialog = true
                        }
                    },
                    enabled = !isLoading && currentRole != null && currentPersonTypeId.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Black,
                        contentColor = White,
                        disabledContainerColor = BlackAlpha20,
                        disabledContentColor = White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.contracts_save_button),
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                        color = White
                    )
                }
            }
        }
    }

    if (showSuccessDialog && lastSavedContract != null) {
        SaveSuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                lastSavedContract?.let { onSaveSuccess(it) }
            }
        )
    }
}

@Composable
private fun SaveSuccessBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color(0xFF0B8A3E)
        )
        Text(
            text = message,
            style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
            color = Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    showTimestamp: Long = System.currentTimeMillis(),
    onDismiss: (() -> Unit)? = null
) {
    var visible by remember(showTimestamp) { mutableStateOf(true) }
    
    // Автоматичне зникнення через 5 секунд
    // Використовуємо showTimestamp як ключ, щоб LaunchedEffect перезапускався
    // навіть якщо message той самий (повторна помилка)
    LaunchedEffect(showTimestamp) {
        visible = true
        delay(5000)
        visible = false
        onDismiss?.invoke()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF7E8))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFE08E00),
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = message,
                style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                color = Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SaveSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = White,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, BlackAlpha10)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "\uD83D\uDC4D", fontSize = 36.sp)
                Text(
                    text = "Дані успішно збережено",
                    style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.SemiBold),
                    color = Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Ми додали їх до документа. Перейдемо до деталей договору.",
                    style = DiiaTextStyle.t3TextBody,
                    color = BlackAlpha80,
                    textAlign = TextAlign.Center
                )
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Black),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 46.dp)
                ) {
                    Text(
                        text = "Добре",
                        style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun IdentityReminderNote() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFDFEFF),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFC266), Color(0xFF7E5CFF))
            )
        ),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "\uD83E\uDD1E",
                style = DiiaTextStyle.h3SmallHeading
            )
            Text(
                text = "Дія не знайшла в реєстрах усіх даних цієї сторони. Додайте відсутні поля або натисніть далі, якщо інформація не змінювалася.",
                style = DiiaTextStyle.t2TextDescription,
                color = Black
            )
        }
    }
}

data class ContractChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val isTyping: Boolean = false,
    val actions: List<ChatAction> = emptyList()
)

/**
 * Дія-кнопка в повідомленні чату.
 * @param type тип дії: "navigate_filling_mode", "confirm_category", etc.
 * @param label текст кнопки
 * @param payload додаткові дані (category_id, template_id, etc.)
 */
data class ChatAction(
    val type: String,
    val label: String,
    val payload: Map<String, String> = emptyMap()
)

data class ContractCategory(
    val id: String,
    val title: String
)

private data class RoleForm(
    val id: String,
    val title: String,
    val description: String,
    val isOccupied: Boolean,
    val isOwnedByMe: Boolean = false
)

private data class PersonType(
    val id: String,
    val label: String,
    val fields: List<PersonField>
)

private data class PersonField(
    val key: String,
    val label: String,
    val required: Boolean,
    val keyboardType: KeyboardType
)

private fun ContractPersonType.toUiPersonType(): PersonType =
    PersonType(
        id = id,
        label = label,
        fields = fields.map { it.toUiField() }
    )

private fun ContractPartyField.toUiField(): PersonField =
    PersonField(
        key = key,
        label = label,
        required = required,
        keyboardType = type.asKeyboardType()
    )

private fun String?.asKeyboardType(): KeyboardType = when (this?.lowercase()) {
    "number", "int", "integer", "numeric" -> KeyboardType.Number
    "phone" -> KeyboardType.Phone
    "email" -> KeyboardType.Email
    else -> KeyboardType.Text
}

@Composable
private fun PersonTypeSelector(
    personTypes: List<PersonType>,
    selectedId: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Тип особи",
            style = DiiaTextStyle.t3TextBody,
            color = BlackAlpha54
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personTypes.forEach { personType ->
                val isSelected = personType.id == selectedId
                Surface(
                    onClick = { if (enabled) onSelect(personType.id) },
                    enabled = enabled,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Black else White,
                    border = BorderStroke(1.dp, if (isSelected) Black else BlackAlpha20)
                ) {
                    Text(
                        text = personType.label,
                        style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                        color = if (isSelected) White else Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonFields(
    fields: List<PersonField>,
    values: SnapshotStateMap<String, String>,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        fields.forEach { field ->
            val current = values[field.key] ?: ""
            OutlinedTextField(
                value = current,
                onValueChange = { if (enabled) values[field.key] = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = field.label + if (field.required) " *" else "")
                },
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Black,
                    unfocusedBorderColor = BlackAlpha54,
                    focusedLabelColor = Black,
                    cursorColor = Black
                )
            )
        }
    }
}

@Composable
private fun RoleSelectionMenu(
    roles: List<RoleForm>,
    selectedRoleId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        roles.forEach { role ->
            val isSelected = role.id == selectedRoleId
            Surface(
                onClick = { onSelect(role.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) BlackAlpha10 else White,
                border = BorderStroke(1.dp, if (isSelected) BlackAlpha20 else BlackAlpha10),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp),
                tonalElevation = 0.dp,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = role.title,
                    style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

private data class FormSection(
    val id: String,
    val title: String,
    val isContractTerms: Boolean
)

@Composable
private fun SectionSelector(
    sections: List<FormSection>,
    selectedSectionId: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(sections) { section ->
            val isSelected = section.id == selectedSectionId
            Surface(
                onClick = { onSelect(section.id) },
                shape = RoundedCornerShape(12.dp),
                color = White,
                border = BorderStroke(1.dp, if (isSelected) BlackAlpha20 else BlackAlpha10),
                modifier = Modifier
                    .height(44.dp),
                tonalElevation = 0.dp,
                shadowElevation = if (isSelected) 4.dp else 2.dp
            ) {
                Text(
                    text = section.title,
                    style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ContractTermsCard(
    values: SnapshotStateMap<String, String>,
    fields: List<PersonField>,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = White,
        border = BorderStroke(1.dp, BlackAlpha10),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.contracts_terms_title),
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )
            Text(
                text = stringResource(R.string.contracts_terms_subtitle),
                style = DiiaTextStyle.t3TextBody,
                color = BlackAlpha54
            )
            PersonFields(
                fields = fields,
                values = values
            )
        }
    }
}

@Composable
fun GradientHintCard(
    text: String,
    modifier: Modifier = Modifier,
    leading: String? = "☝️"
) {
    val transition = rememberInfiniteTransition(label = "hintGradient")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintOffset"
    )

    val gradientColors = listOf(
        Color(0xFFB57AF4),
        Color(0xFF6AA4FF),
        Color(0xFF5FC4FF)
    )
    val brush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),
        end = Offset(200f + offset * 120f, 200f)
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FBFF),
        border = BorderStroke(1.5.dp, brush),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            leading?.let {
                Text(text = it, fontSize = DiiaTextStyle.t2TextDescription.fontSize)
            }
            Text(
                text = text,
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.Medium),
                color = Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun ContractChatMessageBubble(
    message: ContractChatMessage,
    onActionClick: (ChatAction) -> Unit = {}
) {
    val isTyping = message.isTyping
    val shape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (message.isUser) 20.dp else 10.dp,
        bottomEnd = if (message.isUser) 10.dp else 20.dp
    )
    val bubbleBrush = when {
        message.isUser -> Brush.linearGradient(
            colors = listOf(Color(0xFF0D111A), Color(0xFF1C2735))
        )
        isTyping -> Brush.linearGradient(
            colors = listOf(Color(0xFFF6F8FB), Color(0xFFEFF2F9))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFF2F6FF))
        )
    }
    val textColor = if (message.isUser) White else Color(0xFF0F172A)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            if (!message.isUser) {
                Text(
                    text = "Дія.AI",
                    style = DiiaTextStyle.t4TextSmallDescription.copy(fontWeight = FontWeight.SemiBold),
                    color = BlackAlpha54,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(min = 120.dp, max = 280.dp)
                    .shadow(if (message.isUser) 10.dp else 8.dp, shape = shape, clip = false)
                    .background(brush = bubbleBrush, shape = shape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 1f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
            ) {
                if (isTyping) {
                    TypingIndicatorDots()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = message.text,
                            style = DiiaTextStyle.t3TextBody,
                            color = textColor
                        )
                        // Рендеримо action-кнопки якщо є
                        if (message.actions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            message.actions.forEach { action ->
                                ChatActionButton(
                                    action = action,
                                    onClick = { onActionClick(action) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatActionButton(
    action: ChatAction,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Black,
        contentColor = White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = action.label,
            style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.SemiBold),
            color = White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TypingIndicatorDots() {
    val transition = rememberInfiniteTransition(label = "typingDots")
    val dot1 = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2 = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3 = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(dot1, dot2, dot3).forEach { animated ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2937).copy(alpha = animated.value))
            )
        }
    }
}

@Composable
private fun RoleCardWithForm(
    role: RoleForm,
    personTypes: List<PersonType>,
    selectedPersonTypeId: String,
    onSelectPersonType: (String) -> Unit,
    values: SnapshotStateMap<String, String>,
    isSelected: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isSelected) BlackAlpha20 else BlackAlpha10
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = White,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(enabled = !role.isOccupied, onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = role.title,
                        style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                        color = Black
                    )
                    Text(
                        text = role.description,
                        style = DiiaTextStyle.t3TextBody,
                        color = BlackAlpha54
                    )
                }
                if (!role.isOccupied) {
                    val arrowIcon = if (isExpanded) UiBaseR.drawable.ic_role_arrow_up else UiBaseR.drawable.ic_role_arrow_down
                    Icon(
                        painter = painterResource(id = arrowIcon),
                        contentDescription = null,
                        tint = Color(0xFF123264),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(180)),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut(animationSpec = tween(160))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Селектор типу особи (якщо є більше 1 типу)
                    if (personTypes.size > 1) {
                        PersonTypeSelector(
                            personTypes = personTypes,
                            selectedId = selectedPersonTypeId,
                            onSelect = onSelectPersonType,
                            enabled = !role.isOccupied
                        )
                    }
                    
                    val resolvedFields = personTypes.firstOrNull { it.id == selectedPersonTypeId }?.fields
                        ?: personTypes.firstOrNull()?.fields
                        ?: emptyList()
                    PersonFields(
                        fields = resolvedFields,
                        values = values,
                        enabled = !role.isOccupied
                    )
                }
            }
        }
    }
}

data class ContractTemplate(
    val id: String,
    val name: String,
    val file: String
)

@Composable
fun ContractTemplatesScreen(
    categoryId: String,
    templates: List<ContractTemplate>,
    onBackClick: () -> Unit,
    onTemplateSelected: (String) -> Unit,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFD7E7F5),
                        Color(0xFFE7F4ED)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TopBar(title = stringResource(R.string.contracts_templates_title), onBackClick = onBackClick)
            Text(
                text = "Оберіть шаблон договору",
                style = DiiaTextStyle.h2MediumHeading,
                color = Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(templates) { template ->
                    Surface(
                        onClick = { onTemplateSelected(template.id) },
                        shape = RoundedCornerShape(18.dp),
                        color = White,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.name,
                                    style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                                    color = Black
                                )
                            }
                        }
                    }
                }
                if (templates.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.contracts_templates_empty),
                            style = DiiaTextStyle.t3TextBody,
                            color = BlackAlpha54,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun templatesForCategory(categoryId: String): List<ContractTemplate> {
    // Deprecated: дані тепер приходять з бекенду (ContractsMenuViewModel).
    return emptyList()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPromptPicker(
    quickPrompts: List<Pair<String, String>>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPromptShape = RoundedCornerShape(18.dp)
    val quickPromptBorder = Color(0xFF7E5CFF)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(quickPromptShape)
            .background(Color.White)
            .border(1.dp, quickPromptBorder, quickPromptShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFF0F172A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = UiBaseR.drawable.ic_search_black),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Швидкі запити",
                    style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.SemiBold),
                    color = Black
                )
                Text(
                    text = "Обери підказку та одразу відправ — економимо час на наборі тексту.",
                    style = DiiaTextStyle.t4TextSmallDescription,
                    color = BlackAlpha54
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { (title, subtitle) ->
                QuickPromptChip(
                    title = title,
                    subtitle = subtitle,
                    onClick = { onPick(title) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuickPromptChip(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "promptPressScale"
    )
    val chipBackground = Color(0xFFEAF2FF)
    val chipBorder = Color(0xFFC8D9FF)

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        color = chipBackground,
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, chipBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .widthIn(min = 0.dp, max = 999.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )
            Text(
                text = subtitle,
                style = DiiaTextStyle.t4TextSmallDescription,
                color = BlackAlpha54
            )
        }
    }
}

// ===== КОМПОНЕНТИ У СТИЛІ ДІЇ =====

/**
 * Навігаційна панель у стилі Дії (NavigationPanelMlc)
 * Відступи: start=24dp, top=32dp, end=24dp, bottom=16dp
 */
@Composable
fun DiiaNavigationPanel(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showContextMenu: Boolean = false,
    onContextMenuClick: () -> Unit = {},
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка назад - 28dp як у NavigationPanelMlc
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                contentDescription = "Назад",
                tint = Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Заголовок - h4ExtraSmallHeading
        Text(
            text = title,
            style = DiiaTextStyle.h4ExtraSmallHeading,
            color = Black,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Кастомний трейлінг або дефолтне контекстне меню
        if (trailingContent != null) {
            trailingContent()
        } else if (showContextMenu) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onContextMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = UiBaseR.drawable.ic_menu),
                    contentDescription = "Меню",
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Поле пошуку у стилі Дії (SearchInputMlc)
 * Білий фон, заокруглені кути 16dp, іконка пошуку зліва
 */
@Composable
fun DiiaSearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(16.dp)
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Іконка пошуку
            Icon(
                painter = painterResource(id = UiBaseR.drawable.ic_search_black),
                contentDescription = null,
                tint = BlackAlpha30,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Текстове поле
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = DiiaTextStyle.t2TextDescription,
                        color = BlackAlpha30
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = DiiaTextStyle.t2TextDescription.copy(color = Black),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Кнопка очищення
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_input_clear),
                        contentDescription = "Очистити",
                        tint = BlackAlpha54
                    )
                }
            }
        }
    }
}

/**
 * Промо-банер AI у стилі Дії
 * Світло-блакитний фон з бордером, іконка зліва, заокруглені кути 16dp
 */
@Composable
fun DiiaAiPromoBanner(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = Color(0xFFE8F4FD) // Світло-блакитний
    val borderColor = Color(0xFFB8DCF5) // Блакитний бордер
    val iconBackground = Color(0xFF4BB3FE) // Синій фон іконки
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Іконка AI
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = UiBaseR.drawable.ic_doc_info),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Текст
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = DiiaTextStyle.t1BigText,
                    color = Black
                )
                Text(
                    text = subtitle,
                    style = DiiaTextStyle.t3TextBody,
                    color = BlackAlpha54
                )
            }
            
            // Стрілка вправо
            Icon(
                painter = painterResource(id = UiBaseR.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Старий TopBar для зворотної сумісності
 * @deprecated Використовуйте DiiaNavigationPanel
 */
@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
    DiiaNavigationPanel(
        title = title,
        onBackClick = onBackClick
    )
}

@Composable
fun SelectionCard(title: String, description: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Text(
            text = title,
            style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.SemiBold),
            color = Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = DiiaTextStyle.t2TextDescription,
            color = BlackAlpha54
        )
    }
}

/**
 * Чекбокс у стилі Дії
 */
@Composable
fun DiiaCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Індикатор чекбоксу
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Black else White)
                .border(
                    width = 1.5.dp,
                    color = if (checked) Black else BlackAlpha20,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Текст
        Text(
            text = label,
            style = DiiaTextStyle.t2TextDescription,
            color = Black
        )
    }
}

/**
 * Основна кнопка у стилі Дії (BtnPrimaryDefaultAtm)
 */
@Composable
fun DiiaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val backgroundColor = if (enabled) Black else BlackAlpha10
    val textColor = if (enabled) White else BlackAlpha54
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

/**
 * Елемент списку у стилі Дії (іконка + текст + стрілка)
 */
@Composable
fun DiiaListItem(
    title: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Іконка зліва
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0F3F7),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Текст
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = DiiaTextStyle.t1BigText,
                color = Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = DiiaTextStyle.t3TextBody,
                    color = BlackAlpha54,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Стрілка справа
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = BlackAlpha54,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Картка меню у стилі Дії (біла плашка з текстом і стрілкою)
 */
@Composable
fun DiiaMenuCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
            color = Black
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Black
        )
    }
}

/**
 * Картка опції у стилі Дії (для вибору режиму заповнення)
 */
@Composable
fun DiiaOptionCard(
    title: String,
    subtitle: String,
    cta: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Black else BlackAlpha10
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Alabaster,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )
            Text(
                text = subtitle,
                style = DiiaTextStyle.t2TextDescription,
                color = BlackAlpha54
            )
            Text(
                text = cta,
                style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                color = Black
            )
        }
    }
}

/**
 * Картка ролі у стилі Дії (для вибору сторони договору)
 */
@Composable
fun DiiaRoleCard(
    title: String,
    description: String,
    isOccupied: Boolean,
    isOwnedByMe: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        isOwnedByMe -> "Ваша роль"
        isOccupied -> "Зайнята"
        else -> "Вільна"
    }
    val statusColor = when {
        isOwnedByMe -> AzureRadiance
        isOccupied -> Color(0xFFE74C3C)
        else -> Color(0xFF1E9E55)
    }
    val isClickable = !isOccupied || isOwnedByMe
    val borderColor = if (isClickable) BlackAlpha10 else BlackAlpha10
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isClickable, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Alabaster,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isClickable) Black else BlackAlpha54
                )
                // Статус бейдж
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = description,
                style = DiiaTextStyle.t2TextDescription,
                color = BlackAlpha54
            )
        }
    }
}

