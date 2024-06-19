package io.taesu.apiclientspring

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.ZoneId
import java.util.*

@SpringBootApplication
class ApiclientSpringApplication {
    @PostConstruct
    fun onConstruct() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")))
    }
}

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")))
    runApplication<ApiclientSpringApplication>(*args)
}
