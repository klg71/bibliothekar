package net.mayope.bibliothekar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

const val API_BASE_PATH = "/v1"

@SpringBootApplication
class App

internal fun main(args: Array<String>) {
    @Suppress("SpreadOperator") // no performance issue here
    runApplication<App>(*args)
}
