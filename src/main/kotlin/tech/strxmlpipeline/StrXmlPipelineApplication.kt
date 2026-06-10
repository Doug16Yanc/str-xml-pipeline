package tech.strxmlpipeline

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableRetry
class StrXmlPipelineApplication

fun main(args: Array<String>) {
    runApplication<StrXmlPipelineApplication>(*args)
}
