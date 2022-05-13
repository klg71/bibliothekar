package net.mayope.bibliothekar.model

import java.util.UUID

/**
 * Represents the on disk data store
 */
internal data class Bibliothek(
    val name: String,
    val indexedFields: List<String>,
    val id: UUID = UUID.randomUUID(),
)

internal data class Index(
    val field: String,
    var documents: MutableMap<String, MutableSet<UUID>>
)
