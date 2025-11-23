package ua.gov.diia.opensource.ui.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.gov.diia.opensource.data.contracts.repo.ContractsRepository
import javax.inject.Inject

@HiltViewModel
class ContractsMenuViewModel @Inject constructor(
    private val repository: ContractsRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<ContractCategory>>(emptyList())
    val categories: StateFlow<List<ContractCategory>> = _categories.asStateFlow()

    private val _templates = MutableStateFlow<Map<String, List<ContractTemplate>>>(emptyMap())
    val templates: StateFlow<Map<String, List<ContractTemplate>>> = _templates.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCategories(force: Boolean = false) {
        if (_categories.value.isNotEmpty() && !force) return
        viewModelScope.launch {
            runCatching { repository.fetchCategories() }
                .onSuccess {
                    _categories.value = it
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun loadTemplates(categoryId: String, force: Boolean = false) {
        if (_templates.value[categoryId]?.isNotEmpty() == true && !force) return
        viewModelScope.launch {
            runCatching { repository.fetchTemplates(categoryId) }
                .onSuccess { list ->
                    _templates.value = _templates.value.toMutableMap().apply { put(categoryId, list) }
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
        }
    }
}
