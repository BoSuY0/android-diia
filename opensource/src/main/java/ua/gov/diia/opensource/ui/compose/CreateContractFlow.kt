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
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
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
import ua.gov.diia.ui_base.components.theme.AzureRadiance
import ua.gov.diia.ui_base.components.theme.Black
import ua.gov.diia.ui_base.components.theme.BlackAlpha10
import ua.gov.diia.ui_base.components.theme.BlackAlpha20
import ua.gov.diia.ui_base.components.theme.BlackAlpha80
import ua.gov.diia.ui_base.components.theme.BlackAlpha54
import ua.gov.diia.ui_base.components.theme.DiiaTextStyle
import ua.gov.diia.ui_base.components.theme.TropicalBlue
import ua.gov.diia.ui_base.components.theme.White
import ua.gov.diia.ui_base.components.theme.WhiteAlpha20
import ua.gov.diia.ui_base.R as UiBaseR
import kotlinx.coroutines.delay
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
    onAiChatClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredContracts = contractCategories.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFD2EBF7),
                        Color(0xFFE9F5E5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Black
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Створити юридичний договір",
                    style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.Bold),
                    color = Black
                )
            }

            // Search Bar (аналогічно SearchInputV2: невеликий відступ зверху)
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Пошук",
                        style = DiiaTextStyle.t1BigText,
                        color = BlackAlpha54
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_search_black),
                        contentDescription = "Search",
                        tint = BlackAlpha54
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(White),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    disabledContainerColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Black,
                    focusedTextColor = Black,
                    unfocusedTextColor = Black
                ),
                singleLine = true,
                textStyle = DiiaTextStyle.t1BigText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grouped List using standard ListItemGroupOrg
            if (filteredContracts.isNotEmpty()) {
                val listData = ListItemGroupOrgData(
                    itemsList = filteredContracts.map { contract ->
                        ListItemMlcData(
                            id = contract.id,
                            label = UiText.DynamicString(contract.title),
                            iconRight = UiIcon.DrawableResource(DiiaResourceIcon.ELLIPSE_ARROW_RIGHT.code),
                            action = DataActionWrapper(type = contract.id),
                            componentId = UiText.DynamicString("legal_contract_item_${'$'}{contract.id}")
                        )
                    },
                    componentId = UiText.DynamicString("legal_contracts_list"),
                    paddingTop = TopPaddingMode.MEDIUM,
                    paddingHorizontal = SidePaddingMode.NONE
                )

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

            Spacer(modifier = Modifier.height(16.dp))

            val promoShape = RoundedCornerShape(16.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, promoShape, clip = false)
                    .clip(promoShape)
                    .clickable { onAiChatClick() },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AzureRadiance, TropicalBlue)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(WhiteAlpha20, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = UiBaseR.drawable.ic_faq),
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Не знайшли потрібний?",
                                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                                color = White
                            )
                            Text(
                                text = "Створити кастомний договір за допомогою AI",
                                style = DiiaTextStyle.t3TextBody,
                                color = White.copy(alpha = 0.92f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = UiBaseR.drawable.ic_arrow_right),
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Chat FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            FloatingActionButton(
                onClick = { /* TODO: Open support chat */ },
                containerColor = Black,
                contentColor = White,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = UiBaseR.drawable.ic_faq),
                    contentDescription = "Support",
                    modifier = Modifier.size(24.dp)
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
    onBackClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val hasUserMessages = messages.any { it.isUser }

    fun sendMessage(content: String) {
        val text = content.trim()
        if (text.isEmpty() || isSending) return
        inputText = ""
        coroutineScope.launch { onSendMessage(text) }
    }

    val quickPrompts = listOf(
        "Скласти договір оренди житла" to "Готовий каркас з ключовими пунктами",
        "Підготувати трудовий договір" to "Зберемо вимоги по оплаті та строках",
        "Пояснити пункт про відповідальність сторін" to "Розділимо складні формулювання на прості кроки"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE6F0FF),
                        Color(0xFFE9F6ED)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Black
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "AI-чат для договорів",
                    style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.Bold),
                    color = Black
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(messages, key = { _, item -> item.id }) { _, message ->
                    ContractChatMessageBubble(message = message)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White.copy(alpha = 0.98f),
                shadowElevation = 18.dp,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnimatedVisibility(
                        visible = !hasUserMessages,
                        enter = fadeIn(tween(250, easing = FastOutSlowInEasing)) + expandVertically(),
                        exit = fadeOut(tween(200, easing = FastOutLinearInEasing)) + shrinkVertically()
                    ) {
                        QuickPromptPicker(
                            quickPrompts = quickPrompts,
                            onPick = { selection -> sendMessage(selection) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val inputShape = RoundedCornerShape(12.dp)
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrect = true,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onSend = { sendMessage(inputText) }
                            ),
                            placeholder = {
                                Text(
                                    text = "Опишіть, який договір вам потрібен",
                                    style = DiiaTextStyle.t3TextBody,
                                    color = BlackAlpha54
                                )
                            },
                            singleLine = true,
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                disabledContainerColor = White,
                                focusedBorderColor = Black,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                disabledBorderColor = Color(0xFFCBD5E1),
                                cursorColor = Black,
                                focusedTextColor = Black,
                                unfocusedTextColor = Black
                            ),
                            shape = inputShape,
                            textStyle = DiiaTextStyle.t3TextBody,
                            enabled = true,
                            readOnly = false
                        )

                    Spacer(modifier = Modifier.width(10.dp))

                    FilledIconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isSending,
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Black,
                            contentColor = White,
                            disabledContainerColor = Black.copy(alpha = 0.2f),
                            disabledContentColor = White
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = UiBaseR.drawable.ic_role_arrow_up),
                            contentDescription = "Надіслати",
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD7E7F5),
            Color(0xFFE7F4ED)
        )
    )
    val introText = """
        Тут ви зможете швидко зібрати юридичний договір. Що підготувати:
        • дані сторін: ПІБ/назва, контакти, РНОКПП або ЄДРПОУ;
        • адресу та короткий опис об’єкта (квартира, будинок тощо);
        • фінанси: щомісячну суму, завдаток (якщо є), дедлайн оплати;
        • строки дії договору (початок, кінець або безстроково).

        Порада: якщо заповнюєте за дві сторони — одразу внесіть їхні контакти, щоб відправити посилання на погодження. Якщо тільки за себе — достатньо ваших даних, другу сторону можна запросити пізніше.

        У демо-версії все зберігається локально на вашому пристрої. За потреби зможете змінити будь-яке поле перед відправкою чи підписанням.
    """.trimIndent()

    var skipIntro by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TopBar(title = "Юридичні договори", onBackClick = onBackClick)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Вітаємо! 👋",
                            style = DiiaTextStyle.h2MediumHeading,
                            color = Black
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = White,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, Color(0xFFE1E7F5))
                        ) {
                            Text(
                                text = introText,
                                style = DiiaTextStyle.t3TextBody.copy(lineHeight = DiiaTextStyle.t3TextBody.fontSize * 1.3f),
                                color = Black,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SkipIntroToggle(
                    checked = skipIntro,
                    onCheckedChange = {
                        skipIntro = it
                        onSkipIntroChange(it)
                    },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Black, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onStartClick)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Розпочати",
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                        color = White
                    )
                }
            }
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
                    AnimatedVisibility(visible = checked) {
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
                text = "Більше не показувати це вікно",
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
            title = "Заповнити за обидві сторони",
            subtitle = "Ви введете дані за себе та за іншу сторону договору.",
            cta = "Спільне редагування"
        ),
        ModeOption(
            id = "one_side",
            title = "Заповнити тільки свою частину",
            subtitle = "Ви введете тільки свої дані. Інша сторона отримає посилання для заповнення своїх даних.",
            cta = "Посилання для другої сторони"
        )
    )
    var selectedId by remember { mutableStateOf(options.first().id) }

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
            TopBar(title = "Спосіб заповнення", onBackClick = onBackClick)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Формат заповнення",
                            style = DiiaTextStyle.h2MediumHeading,
                            color = Black
                        )
                        Text(
                            text = "Оберіть, як будете вносити дані до договору.",
                            style = DiiaTextStyle.t2TextDescription,
                            color = BlackAlpha54
                        )
                    }
                }

                items(options) { option ->
                    ModeCard(
                        option = option,
                        isSelected = selectedId == option.id,
                        onSelect = {
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
    mainRoleId: String? = null,
    onBackClick: () -> Unit,
    onRoleSelected: (String) -> Unit
) {
    val uiPersonTypes = remember(personTypes) {
        if (personTypes.isNotEmpty()) {
            personTypes.map { it.toUiPersonType() }
        } else {
            leasePersonTypes()
        }
    }
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
            TopBar(title = "Вибір ролі", onBackClick = onBackClick)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Оберіть сторону договору",
                            style = DiiaTextStyle.h2MediumHeading,
                            color = Black
                        )
                        Text(
                            text = if (isBothSides) "Ви можете вказати дані для обох сторін. Почніть з основної." else "Оберіть свою сторону, далі заповните її дані.",
                            style = DiiaTextStyle.t2TextDescription,
                            color = BlackAlpha54
                        )
                    }
                }

                items(roleForms) { role ->
                    RoleMenuCard(
                        role = role,
                        isMain = role.id == mainRoleId,
                        onSelect = { if (!role.isOccupied) onRoleSelected(role.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleMenuCard(
    role: RoleForm,
    isMain: Boolean = false,
    onSelect: () -> Unit
) {
    val statusText = if (role.isOccupied) "Зайнята" else if (isMain) "Головна" else "Вільна"
    val statusColor = when {
        role.isOccupied -> Color(0xFFE74C3C)
        isMain -> Color(0xFF123264)
        else -> Color(0xFF1E9E55)
    }
    val alphaBg = if (role.isOccupied) 0.06f else 0.12f
    Surface(
        onClick = onSelect,
        enabled = !role.isOccupied,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        color = White,
        border = BorderStroke(1.dp, if (role.isOccupied) BlackAlpha10 else BlackAlpha20)
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

data class ContractDraft(
    val contractType: String,
    val roleId: String,
    val roleTitle: String,
    val personTypeId: String,
    val personTypeLabel: String,
    val fields: Map<String, String>
)

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
    mainRoleId: String? = null,
    initialSelectedRoleId: String? = null,
    isLoading: Boolean = false
) {
    val uiPersonTypes = remember(roles, personTypes) {
        when {
            personTypes.isNotEmpty() -> personTypes.map { it.toUiPersonType() }
            roles.any { it.fields.isNotEmpty() } -> {
                roles.map { role ->
                    PersonType(
                        id = role.personType ?: role.id,
                        label = role.label.ifBlank { role.personType ?: role.id },
                        fields = role.fields.map { it.toUiField() }
                    )
                }
            }
            else -> leasePersonTypes()
        }
    }
    val rolesMap = roles.associateBy { it.id }
    val hasSchemaPersonTypes = personTypes.isNotEmpty()
    val roleForms = remember(roles, uiPersonTypes, clientId) {
        val source = roles.takeIf { it.isNotEmpty() } ?: listOf(
            ContractPartyRole(id = "role_1", label = "Сторона 1", allowedPersonTypes = emptyList()),
            ContractPartyRole(id = "role_2", label = "Сторона 2", allowedPersonTypes = emptyList())
        )
        source.map { role ->
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
        return if (contextMatch != null) {
            val contextFields = contextMatch.fields.map { it.toUiField() }
            base.map { type ->
                if (type.id == contextMatch.personTypeId) type.copy(fields = contextFields) else type
            }
        } else {
            base
        }
    }

    // For new contracts, auto-select first role; for editing, use provided role or let user choose
    val selectedRoleId = remember { 
        mutableStateOf(
            when {
                // If editing with a specific role selected
                !initialSelectedRoleId.isNullOrBlank() -> initialSelectedRoleId
                // For new contracts, auto-select first role
                initialSelectedRoleId == null && roleForms.isNotEmpty() -> roleForms.first().id
                // Otherwise, no selection
                else -> ""
            }
        )
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
    val contractTermsValues = remember { mutableStateMapOf<String, String>() }
    val contractTermsFieldList = remember(contractFields) {
        val backendFields = contractFields.takeIf { it.isNotEmpty() }?.map { it.toUiField() }
        backendFields ?: contractTermsFields()
    }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
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
        if (initialSelectedRoleId == null && selectedRoleId.value.isBlank() && roleForms.isNotEmpty()) {
            selectedRoleId.value = roleForms.first().id
        }
    }

    val currentPersonTypeId = selectedPersonType[selectedRoleId.value]

    LaunchedEffect(selectedRoleId.value, currentPersonTypeId) {
        if (!selectedRoleId.value.isNullOrEmpty() && !currentPersonTypeId.isNullOrEmpty()) {
            onPartyContextChanged(selectedRoleId.value, currentPersonTypeId.orEmpty())
        }
    }

    LaunchedEffect(partyContext) {
        if (partyContext != null &&
            partyContext.roleId == selectedRoleId.value &&
            partyContext.personTypeId == currentPersonTypeId
        ) {
            val fieldValues = roleFieldValues.getOrPut(selectedRoleId.value) { mutableStateMapOf() }
            val contractKeys = contractTermsFieldList.map { it.key }.toSet()
            partyContext.fields.forEach { field ->
                if (!field.value.isNullOrEmpty()) {
                    fieldValues[field.key] = field.value.orEmpty()
                    if (contractKeys.contains(field.key)) {
                        contractTermsValues[field.key] = field.value.orEmpty()
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

    LaunchedEffect(roles) {
        val contractKeys = contractTermsFieldList.map { it.key }.toSet()
        roles.forEach { role ->
            val initialFields = role.fields.filter { !it.value.isNullOrEmpty() }
            if (initialFields.isNotEmpty()) {
                val stateMap = roleFieldValues.getOrPut(role.id) { mutableStateMapOf() }
                initialFields.forEach { field ->
                    val value = field.value.orEmpty()
                    stateMap[field.key] = value
                    if (contractKeys.contains(field.key)) {
                        contractTermsValues[field.key] = value
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar(title = "Ваша роль", onBackClick = onBackClick)

            val contentModifier = Modifier
                .weight(1f)
                .fillMaxWidth()

            val formSections = remember(roleForms, isBothSides, selectedRoleId.value, mainRoleId) {
                val sections = mutableListOf<FormSection>()
                val multiRoleMode = isBothSides
                if (multiRoleMode) {
                    roleForms.forEach {
                        sections.add(FormSection(id = it.id, title = it.title, isContractTerms = false))
                    }
                    if (!mainRoleId.isNullOrEmpty()) {
                        sections.add(
                            FormSection(
                                id = contractTermsSectionId,
                                title = "Умови договору",
                                isContractTerms = true
                            )
                        )
                    }
                } else {
                    val roleId = selectedRoleId.value
                    if (roleId.isNotBlank()) {
                        val roleTitle = roleForms.firstOrNull { it.id == roleId }?.title ?: roleId
                        sections.add(
                            FormSection(
                                id = roleId,
                                title = roleTitle,
                                isContractTerms = false
                            )
                        )
                        if (roleId == mainRoleId) {
                            sections.add(
                                FormSection(
                                    id = contractTermsSectionId,
                                    title = "Умови договору",
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
                    selectedRoleId.value.takeIf { id ->
                        id.isNotBlank() && formSections.any { it.id == id }
                    } ?: formSections.firstOrNull()?.id.orEmpty()
                )
            }
            LaunchedEffect(formSections) {
                val preferredSection = selectedRoleId.value.takeIf { id ->
                    id.isNotBlank() && formSections.any { it.id == id }
                }
                selectedSectionId = preferredSection ?: formSections.firstOrNull()?.id.orEmpty()
            }

            Column(
                modifier = contentModifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IdentityReminderNote()

                val showSectionSelector = formSections.size > 1
                if (showSectionSelector) {
                    SectionSelector(
                        sections = formSections,
                        selectedSectionId = selectedSectionId,
                        onSelect = { sectionId ->
                            selectedSectionId = sectionId
                            if (sectionId != contractTermsSectionId) {
                                selectedRoleId.value = sectionId
                            }
                        }
                    )
                }

                when {
                    selectedSectionId == contractTermsSectionId -> {
                        ContractTermsCard(
                            values = contractTermsValues,
                            fields = contractTermsFieldList
                        )
                    }
                    selectedSectionId.isNotBlank() -> {
                        val role = roleForms.firstOrNull { it.id == selectedSectionId }
                        if (role != null) {
                            val availableTypes = personTypesForRole(role.id)
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    validationError != null -> ErrorBanner(message = validationError!!)
                    saveSuccessMessage != null -> SaveSuccessBanner(message = saveSuccessMessage!!)
                }

                val currentRole = roleForms.firstOrNull { it.id == selectedRoleId.value }
                val currentPersonTypeId = currentRole?.let {
                    selectedPersonType[it.id] ?: personTypesForRole(it.id).firstOrNull()?.id.orEmpty()
                }.orEmpty()
                val isCurrentRoleMain = currentRole?.let {
                    if (!mainRoleId.isNullOrEmpty()) {
                        mainRoleId == it.id
                    } else {
                        // Fallback: assume first role is main if not specified
                        roleForms.firstOrNull()?.id == it.id
                    }
                } ?: false
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
                        val missingTerms = if (contractTermsVisible && isCurrentRoleMain) {
                            contractTermsFieldList.filter { it.required && contractTermsValues[it.key].orEmpty().trim().isEmpty() }
                        } else emptyList()
                        val missingAll = (missingRequired + missingTerms).distinctBy { it.key }
                        if (missingAll.isNotEmpty()) {
                            validationError = "Заповніть обов'язкові поля: " + missingAll.joinToString(", ") { it.label }
                            return@Button
                        }
                        validationError = null
                        val mergedFields = fieldValues.toMutableMap().apply {
                            if (contractTermsVisible && isCurrentRoleMain) {
                                putAll(contractTermsValues)
                            }
                        }
                        coroutineScope.launch {
                            val saved = runCatching {
                                onRemoteSave(role.id, currentPersonTypeId, mergedFields)
                            }.onFailure { error ->
                                // validationError = error.message ?: "Не вдалося зберегти дані. Спробуйте ще раз."
                            }.getOrNull()
                            // if (saved == null) {
                            //    validationError = validationError ?: "Не вдалося зберегти дані. Спробуйте ще раз."
                            //    return@launch
                            // }
                            if (saved != null) {
                                lastSavedContract = saved
                            }
                            saveSuccessMessage = "Дані успішно збережено"
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
                        text = "Зберегти дані",
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
private fun ErrorBanner(message: String) {
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
            textAlign = TextAlign.Start
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDataInputScreen(
    role: String,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val personTypes = leasePersonTypes()
    val contractFields = contractTermsFields()

    var selectedPersonTypeId by remember { mutableStateOf(personTypes.first().id) }
    val personFieldValues = remember { mutableStateMapOf<String, String>() }
    val contractFieldValues = remember { mutableStateMapOf<String, String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar(title = "Дані сторони ($role)", onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Тип сторони",
                            style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                            color = Black
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            personTypes.forEach { type ->
                                val selected = selectedPersonTypeId == type.id
                                Box(modifier = Modifier.weight(1f)) {
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedPersonTypeId = type.id },
                                        label = {
                                            Text(
                                                text = type.label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = White,
                                            selectedContainerColor = BlackAlpha10,
                                            labelColor = Black,
                                            selectedLabelColor = Black
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, BlackAlpha10),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 44.dp)
                                    )
                                }
                            }
                        }
                        PersonFields(
                            fields = personTypes.first { it.id == selectedPersonTypeId }.fields,
                            values = personFieldValues
                        )
                    }
                }

                Surface(
                    color = White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Дані договору",
                            style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                            color = Black
                        )
                        PersonFields(
                            fields = contractFields,
                            values = contractFieldValues
                        )
                    }
                }
            }

            // Next Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Black, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onNextClick)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Далі",
                        style = DiiaTextStyle.t1BigText.copy(fontWeight = FontWeight.SemiBold),
                        color = White
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
    val isTyping: Boolean = false
)

data class ContractCategory(
    val id: String,
    val title: String
)

private data class RoleForm(
    val id: String,
    val title: String,
    val description: String,
    val isOccupied: Boolean
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
                text = "Умови договору",
                style = DiiaTextStyle.t2TextDescription.copy(fontWeight = FontWeight.SemiBold),
                color = Black
            )
            Text(
                text = "Заповнює головна сторона. Дані додаються до договору для обох сторін.",
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
private fun ContractChatMessageBubble(message: ContractChatMessage) {
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
                    .widthIn(min = 120.dp, max = 220.dp)
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
                    Text(
                        text = message.text,
                        style = DiiaTextStyle.t3TextBody,
                        color = textColor
                    )
                }
            }
        }
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
    val statusText = if (role.isOccupied) "Зайнята" else "Вільна"
    val statusColor = if (role.isOccupied) Color(0xFFE74C3C) else Color(0xFF1E9E55)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = DiiaTextStyle.t3TextBody.copy(fontWeight = FontWeight.Medium),
                            color = statusColor
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

private fun leasePersonTypes(): List<PersonType> = listOf(
    PersonType(
        id = "individual",
        label = "Фізична особа",
        fields = listOf(
            PersonField("name", "ПІБ", true, KeyboardType.Text),
            PersonField("address", "Адреса проживання/реєстрації", true, KeyboardType.Text),
            PersonField("id_doc", "Паспорт (серія/номер)", false, KeyboardType.Text),
            PersonField("id_code", "РНОКПП", false, KeyboardType.Text),
            PersonField("iban", "IBAN рахунку (за наявності)", false, KeyboardType.Text),
            PersonField("phone", "Телефон", false, KeyboardType.Phone),
            PersonField("email", "Email", false, KeyboardType.Email)
        )
    ),
    PersonType(
        id = "fop",
        label = "ФОП",
        fields = listOf(
            PersonField("name", "ПІБ ФОП", true, KeyboardType.Text),
            PersonField("address", "Адреса реєстрації ФОП", true, KeyboardType.Text),
            PersonField("id_code", "РНОКПП", true, KeyboardType.Text),
            PersonField("id_doc", "Паспорт (серія/номер)", false, KeyboardType.Text),
            PersonField("iban", "IBAN рахунку", false, KeyboardType.Text),
            PersonField("phone", "Телефон", false, KeyboardType.Phone),
            PersonField("email", "Email", false, KeyboardType.Email)
        )
    ),
    PersonType(
        id = "company",
        label = "Юр. особа",
        fields = listOf(
            PersonField("name", "Повна назва юр. особи", true, KeyboardType.Text),
            PersonField("address", "Юридична адреса", true, KeyboardType.Text),
            PersonField("id_code", "Код ЄДРПОУ", true, KeyboardType.Text),
            PersonField("representative", "ПІБ директора / представника", false, KeyboardType.Text),
            PersonField("id_doc", "Документ представника", false, KeyboardType.Text),
            PersonField("iban", "IBAN рахунку", false, KeyboardType.Text),
            PersonField("phone", "Телефон", false, KeyboardType.Phone),
            PersonField("email", "Email", false, KeyboardType.Email)
        )
    )
)

private fun contractTermsFields(): List<PersonField> = leaseContractFields()

private fun leaseContractFields(): List<PersonField> = listOf(
    PersonField("object_address", "Адреса житла (об'єкт оренди)", true, KeyboardType.Text),
    PersonField("object_description", "Опис об'єкта", false, KeyboardType.Text),
    PersonField("area_sqm", "Площа (м²)", false, KeyboardType.Number),
    PersonField("rent_price_month", "Сума оренди за місяць (грн/міс)", true, KeyboardType.Number),
    PersonField("deposit_amount", "Сума завдатку (грн)", false, KeyboardType.Number),
    PersonField("payment_due_day", "День місяця для оплати", false, KeyboardType.Number),
    PersonField("start_date", "Дата початку оренди", true, KeyboardType.Text),
    PersonField("end_date", "Дата закінчення оренди", false, KeyboardType.Text)
)

fun ContractDraft.toContractUiModel(): ContractUiModel {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val formattedDate = formatter.format(Date())
    val name = fields["name"]?.takeIf { it.isNotBlank() }
    val subtitleParts = listOfNotNull(personTypeLabel.takeIf { it.isNotBlank() }, name)
    val subtitle = subtitleParts.joinToString(" • ").ifBlank { "Дані успішно збережено" }

    return ContractUiModel(
        id = "${contractType}_${roleId}_${System.currentTimeMillis()}",
        title = "Чернетка договору",
        subtitle = subtitle,
        status = ContractStatus.DRAFT,
        lastUpdated = formattedDate,
        iconRes = UiBaseR.drawable.ic_doc_cert,
        isFilled = fields.isNotEmpty(),
        isSigned = false,
        history = listOf(
            HistoryUiModel(
                date = formattedDate,
                description = "Дані сторони збережено: $roleTitle"
            )
        )
    )
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
    onTemplateSelected: (String) -> Unit
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
            TopBar(title = "Шаблони договорів", onBackClick = onBackClick)

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
                            text = "Немає шаблонів для цієї категорії",
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
    onPick: (String) -> Unit
) {
    val quickPromptShape = RoundedCornerShape(18.dp)
    val quickPromptBorder = Color(0xFF7E5CFF)

    Column(
        modifier = Modifier
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

@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = UiBaseR.drawable.ic_arrow_back),
                contentDescription = "Back",
                tint = Black
            )
        }
        Text(
            text = title,
            style = DiiaTextStyle.h3SmallHeading.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
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
