package net.mayope.bibliothekar.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.mayope.bibliothekar.model.Bibliothek
import net.mayope.bibliothekar.model.Index
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import java.util.UUID
import javax.persistence.EntityNotFoundException

internal data class SearchParam(val indexedField: String, val value: String)

@Service
internal class IndexRepository(@Value("\${bibliothekar.rootDir}") private val rootFolder: String,
    private val objectMapper: ObjectMapper) {

    private val indexCache = mutableMapOf<Pair<UUID, String>, Index>()

    init {
        File(rootFolder).mkdirs()
    }

    fun byId(id: UUID) =
        bibliothekFile(id).let {
            if (!it.exists()) {
                throw  EntityNotFoundException("Bibliothek with id: $id not found")
            }
            objectMapper.readValue<Bibliothek>(it)
        }

    fun getDocument(bibliothekId: UUID, documentId: UUID) = documentFile(bibliothekId, documentId).let {
        if (!it.exists()) {
            throw  EntityNotFoundException("Document with id: $documentId not found in bibliothek: $bibliothekId")
        }
        objectMapper.readTree(it)
    }

    fun findDocuments(bibliothekId: UUID, searchParams: List<SearchParam>): Set<UUID> {
        val bibliothek = byId(bibliothekId)
        if (!bibliothek.indexedFields.containsAll(searchParams.map { it.indexedField })) {
            error("Not all requested indexedFields are present in the index")
        }
        return searchParams.mapNotNull {
            loadIndex(bibliothekId, it.indexedField).documents[it.value]?.toSet()
        }.reduceOrNull { acc, set -> acc.intersect(set) } ?: emptySet()
    }

    fun findWholeDocuments(bibliothekId: UUID, searchParams: List<SearchParam>): List<JsonNode> {
        return findDocuments(bibliothekId, searchParams).map {
            getDocument(bibliothekId, it)
        }
    }

    fun loadIndex(bibliothekId: UUID, indexedField: String) =
        indexCache[bibliothekId to indexedField] ?: indexFile(bibliothekId, indexedField).let {
            if (!it.exists()) {
                throw  EntityNotFoundException("Index with name: $indexedField not found in bibliothek: $bibliothekId")
            }
            objectMapper.readValue<Index>(it)
        }

    fun new(name: String, indexedFields: List<String>) =
        Bibliothek(name, indexedFields).also {
            val destFile = bibliothekFile(it.id)
            objectMapper.writeValue(destFile, it)
            bibliothekDir(it.id).resolve("data").mkdirs()
            bibliothekDir(it.id).resolve("index").mkdirs()
            indexedFields.forEach { indexedField ->
                objectMapper.writeValue(indexFile(it.id, indexedField), Index(indexedField, mutableMapOf()))
            }
        }

    fun addDocument(bibliothekId: UUID, document: JsonNode) {
        try {
            byId(bibliothekId).let {
                it.indexedFields.forEach {
                    indexField(bibliothekId, it, listOf(document))
                }
                objectMapper.writeValue(documentFile(bibliothekId, document.id()), document)
            }
        } catch (e: Throwable) {
            println(e)
            if (documentFile(bibliothekId, document.id()).exists()) {
                documentFile(bibliothekId, document.id()).delete()
            }
        }
    }

    fun addDocuments(bibliothekId: UUID, documents: List<JsonNode>) {
        try {
            byId(bibliothekId).let {
                it.indexedFields.forEach {
                    indexField(bibliothekId, it, documents)
                }
                documents.forEach {
                    objectMapper.writeValue(documentFile(bibliothekId, it.id()), it)
                }
            }
        } catch (e: Throwable) {
            println(e)
        }
    }

    private fun indexField(bibliothekId: UUID,
        indexField: String,
        documents: List<JsonNode>) {
        loadIndex(bibliothekId, indexField).let { index ->
            documents.forEach { document ->
                if (document.has(indexField)) {
                    document.get(indexField).isArray
                    if (!index.documents.containsKey(fieldValue(document, indexField))) {
                        index.documents[fieldValue(document, indexField)] = mutableSetOf()
                    }
                    index.documents[fieldValue(document, indexField)]?.add(document.id()) ?: error(
                        "index entry could not get created"
                    )
                }
            }
            objectMapper.writeValue(indexFile(bibliothekId, indexField), index)
        }
    }

    private fun fieldValue(document: JsonNode, indexField: String) =
        if (document.get(indexField).isTextual) {
            document.get(indexField).textValue()
        } else {
            document.get(indexField).toString()
        }


    private fun JsonNode.id() =
        get("guid")?.textValue().let { UUID.fromString(it) } ?: error("Not a valid document: $this")

    private fun bibliothekFile(id: UUID) = Path.of(rootFolder).resolve("$id.bibliothek").toFile()
    private fun bibliothekDir(id: UUID) = Path.of(rootFolder).resolve(id.toString()).toFile()
    private fun indexFile(bibliothekId: UUID, indexedField: String) =
        bibliothekDir(bibliothekId).resolve("index").resolve("$indexedField.index")

    private fun documentFile(bibliothekId: UUID, documentId: UUID) =
        bibliothekDir(bibliothekId).resolve("data").resolve(documentId.toString())

}
