package todoapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.web.reactive.config.WebFluxConfigurer
import todoapp.application.TodoManageProcessor
import todoapp.domain.UUIDTodoIdGenerator
import todoapp.serializer.Serializers
import todoapp.web.IndexRouter
import todoapp.web.TodoRouter

@SpringBootApplication
class TodoServerApplication

/**
 * 서버 애플리케이션 진입점(entry point)
 *
 * @author springrunner.kr@gmail.com
 */
fun main(args: Array<String>) {
    val applicationBeans = beans {
        bean {
            TodoManageProcessor(
                todoIdGenerator = UUIDTodoIdGenerator(),
                todoRepository = ref()
            )
        }
    }
    val webBeans = beans {
        bean {
            object : WebFluxConfigurer {
                override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
                    configurer.defaultCodecs().kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(Serializers.JSON))
                    configurer.defaultCodecs().kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(Serializers.JSON))
                }
            }
        }
        bean { IndexRouter() }
        bean { TodoRouter(ref(), ref(), ref(), ref()) }
    }

    runApplication<TodoServerApplication>(*args) {
        addInitializers(applicationBeans, webBeans)
    }
}
