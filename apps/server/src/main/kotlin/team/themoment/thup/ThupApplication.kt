package team.themoment.thup

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ThupApplication

fun main(args: Array<String>) {
    runApplication<ThupApplication>(*args)
}
