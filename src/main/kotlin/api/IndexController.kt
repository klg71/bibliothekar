package net.mayope.bibliothekar.api

import net.mayope.bibliothekar.API_BASE_PATH
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class IndexCreateRequest(val name: String, val fieldsToIndex: List<String>)

@RestController
@RequestMapping("$API_BASE_PATH/index")
internal class IndexController {
    @PostMapping("/")
    fun create(@RequestBody request: IndexCreateRequest): UUID {
        return UUID.randomUUID()
    }
}
