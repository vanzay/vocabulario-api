package vio.jobs

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import vio.services.UserBookService

@Component
class UpdateSummaryJob(
    private val redisTemplate: RedisTemplate<String?, Any?>,
    private val userBookService: UserBookService
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = "\${jobs.update.summary.schedule}")
    fun execute() {
        do {
            val userBookId = redisTemplate.opsForSet().pop("books.update.summary") ?: break
            try {
                userBookService.updateSummary(userBookId.toString().toInt())
            } catch (ex: Exception) {
                log.error("Exception while updating user books summary", ex)
            }
        } while (true)
    }
}
