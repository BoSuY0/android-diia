package ua.gov.diia.opensource.ui.compose

/**
 * Єдиний sealed class для навігації в флоу створення договору.
 * Використовується як в ContractsFCompose, так і в CreateContractFCompose.
 */
sealed class ContractCreationStep {
    /** Інтро-екран (тільки для standalone флоу) */
    object Intro : ContractCreationStep()
    
    /** Вибір категорії договору */
    object SelectCategory : ContractCreationStep()
    
    /** Вибір шаблону для категорії */
    data class SelectTemplate(val categoryId: String) : ContractCreationStep()
    
    /** Вибір режиму заповнення (за обидві сторони / тільки своя частина) */
    data class SelectMode(val contractType: String) : ContractCreationStep()
    
    /** Меню вибору ролі */
    data class SelectRoleMenu(val contractType: String, val isBothSides: Boolean) : ContractCreationStep()
    
    /** Форма заповнення даних для обраної ролі */
    data class FillRoleData(
        val contractType: String,
        val isBothSides: Boolean,
        val selectedRoleId: String? = null,
        val fromEdit: Boolean = false
    ) : ContractCreationStep()
    
    /** AI чат для кастомного договору */
    object AiChat : ContractCreationStep()
    
    /** Деталі збереженого договору */
    data class ContractDetails(val contract: ContractUiModel) : ContractCreationStep()
    
    /** Прев'ю HTML договору */
    data class ContractPreview(val contract: ContractUiModel) : ContractCreationStep()
    
    /**
     * Отримати індекс для визначення напрямку анімації.
     * Менший індекс = раніший крок у флоу.
     */
    val stepIndex: Int get() = when (this) {
        is Intro -> 0
        is SelectCategory -> 1
        is SelectTemplate -> 2
        is SelectMode -> 3
        is SelectRoleMenu -> 4
        is FillRoleData -> 5
        is AiChat -> 6
        is ContractDetails -> 7
        is ContractPreview -> 8
    }
}
