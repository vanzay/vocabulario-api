package vio.jobs

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import vio.services.TrainingService

@Component
class RefreshTrainingPointsJob(
    private val trainingService: TrainingService
) {

    @Scheduled(cron = "\${jobs.refresh.training.points.schedule}")
    fun execute() {
        trainingService.refreshPoints()
    }
}
