package org.learn.board.global.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PopularPostBatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;

    private static final int CHUNK_SIZE = 100;

}
