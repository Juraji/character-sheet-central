package nl.juraji.charactersheetscentral.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Configuration
@EnableAsync
class AsyncExecutionConfiguration : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor = asyncThreadPoolExecutor()

    @Bean
    fun asyncThreadPoolExecutor(): ThreadPoolExecutor = ThreadPoolExecutor(
        CORE_POOL_SIZE, MAX_POOL_SIZE,
        KEEP_ALIVE_SEC, TimeUnit.SECONDS,
        SynchronousQueue()
    )

    @Bean
    fun applicationEventMulticaster(executor: ThreadPoolExecutor): ApplicationEventMulticaster =
        SimpleApplicationEventMulticaster().apply {
            setTaskExecutor(executor)
        }

    companion object {
        const val CORE_POOL_SIZE = 8
        const val MAX_POOL_SIZE = 16
        const val KEEP_ALIVE_SEC = 60L
    }
}
