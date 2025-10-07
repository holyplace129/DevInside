package org.learn.board.domain.post.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job aggregatePopularPostsJob;

    @Scheduled(cron = "0 */10 * * * *")
    public void runAggregatePopularPostsJob() {
        try {
            log.info("인기 게시글 집계 배치 Job 실행");
            jobLauncher.run(aggregatePopularPostsJob, new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters());
        } catch (Exception e) {
            log.error("인기 게시글 집계 배치 Job 실행 중 오류 발생", e);
        }
    }
}
