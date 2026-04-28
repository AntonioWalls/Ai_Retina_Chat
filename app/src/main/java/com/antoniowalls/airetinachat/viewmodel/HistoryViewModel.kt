package com.antoniowalls.airetinachat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antoniowalls.airetinachat.data.model.ChatSession
import com.antoniowalls.airetinachat.domain.usecase.GetChatHistoryUseCase
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
    private val getChatHistoryUseCase: GetChatHistoryUseCase
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
                        val newGrouped = filterAndGroupSessions(sessions, currentState.searchQuery)
                        currentState.copy(
                            allSessions = sessions,
                            groupedHistory = newGrouped,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de carga: ${e.localizedMessage}", isLoading = false) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val newGrouped = filterAndGroupSessions(currentState.allSessions, query)
            currentState.copy(
                searchQuery = query,
                groupedHistory = newGrouped
            )
        }
    }

    private fun filterAndGroupSessions(sessions: List<ChatSession>, query: String): Map<String, List<ChatSession>> {
        val filtered = if (query.isBlank()) {
            sessions
        } else {
            sessions.filter {
                it.title.contains(query, ignoreCase = true) || it.preview.contains(query, ignoreCase = true)
            }
        }
        return filtered.groupBy { it.category }
    }
}