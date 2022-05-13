package net.mayope.bibliothekar

import com.fasterxml.jackson.databind.ObjectMapper
import net.mayope.bibliothekar.repository.IndexRepository
import net.mayope.bibliothekar.repository.SearchParam
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.io.File

@SpringBootTest
internal class BibliothekarTest {
    @Autowired
    private lateinit var indexRepository: IndexRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun test() {
        val bibo = indexRepository.new("test", listOf("email", "company", "age"))

        var time = System.currentTimeMillis()
        File("E:\\repositories\\bibliothekar\\src\\test\\resources\\generated.json").let {
            objectMapper.readTree(it).toList()
        }.let {
            indexRepository.addDocuments(bibo.id, it)
        }
        println("Indexing took: ${System.currentTimeMillis() - time} ms")

        time = System.currentTimeMillis()
        val results = indexRepository.findWholeDocuments(bibo.id, listOf(SearchParam("company", "FIREWAX")))
        println("Query took: ${System.currentTimeMillis() - time} ms")
        println(results.size)
        results.forEach {
            println(it)
        }
    }
}
