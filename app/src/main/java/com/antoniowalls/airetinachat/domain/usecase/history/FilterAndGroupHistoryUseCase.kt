package com.antoniowalls.airetinachat.domain.usecase.history

import com.antoniowalls.airetinachat.data.model.ChatSession

/**
 * Caso de Uso para filtrar y agrupar las sesiones de chat.
 * Toda la lógica de negocio (búsqueda por título o preview, agrupación por categoría)
 * se encapsula aquí.
 */
class FilterAndGroupHistoryUseCase {
    operator fun invoke(sessions: List<ChatSession>, query: String): Map<String, List<ChatSession>> {
        val filtered = if (query.isBlank()) {
            sessions
        } else {
            sessions.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.preview.contains(query, ignoreCase = true)
            }
        }
        return filtered.groupBy { it.category }
    }
}