package org.learn.board.domain.post.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PopularPostBatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final ItemProcessor<Gallery, GalleryPopularPostProcessor.GalleryWithPopularPosts> galleryPopularPostProcessor;
    private final ItemWriter<GalleryPopularPostProcessor.GalleryWithPopularPosts> redisPopularPostWriter;

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job aggregatePopularPostsJob() {
        return new JobBuilder("aggregatePopularPostsJob", jobRepository)
                .start(aggregateGalleryStep())
                .build();
    }

    @Bean
    public Step aggregateGalleryStep() {
        return new StepBuilder("aggregateGalleryStep", jobRepository)
                .<Gallery, GalleryPopularPostProcessor.GalleryWithPopularPosts>chunk(CHUNK_SIZE, transactionManager)
                .reader(galleryReader())
                .processor(galleryPopularPostProcessor)
                .writer(redisPopularPostWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Gallery> galleryReader() {
        log.info("갤러리 페이징 리더 실행");
        return new JpaPagingItemReaderBuilder<Gallery>()
                .name("galleryReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT g FROM Gallery g ORDER BY g.id")
                .build();
    }
}
