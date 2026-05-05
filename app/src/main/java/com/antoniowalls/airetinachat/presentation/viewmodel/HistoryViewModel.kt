package com.antoniowalls.airetinachat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.model.ChatSession
import com.antoniowalls.airetinachat.domain.usecase.history.GetChatHistoryUseCase
import com.antoniowalls.airetinachat.domain.usecase.history.FilterAndGroupHistoryUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val searchQuery: String = "",
    val allSessions: List<ChatSession> = emptyList(),
    val groupedHistory: Map<String, List<ChatSession>> = emptyMap(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val filterAndGroupHistoryUseCase: FilterAndGroupHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                getChatHistoryUseCase().collect { sessions ->
                    _uiState.update { currentState ->
                        // Usamos el Caso de Uso para filtrar y agrupar
                        val newGrouped = filterAndGroupHistoryUseCase(sessions, currentState.searchQuery)
                        currentState.copy(
                            allSessions = sessions,
                            groupedHistory = newGrouped,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Error de carga: ${e.localizedMessage}", isLoading = false)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            // Usamos el Caso de Uso para filtrar y agrupar con la nueva query
            val newGrouped = filterAndGroupHistoryUseCase(currentState.allSessions, query)
            currentState.copy(
                searchQuery = query,
                groupedHistory = newGrouped
            )
        }
    }
}