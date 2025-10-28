package org.learn.board.global.init;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.domain.Post;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DummyDataBatchConfiguration {

    private final GalleryRepository galleryRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PasswordEncoder passwordEncoder;
    private final Tasklet setupDataTasklet;

    @Bean
    public Job createDummyDataJob() {
        return new JobBuilder("createDummyDataJob", jobRepository)
                .start(createDummyPostStep())
                .build();
    }

    @Bean
    public Step setupStep() {
        return new StepBuilder("setupStep", jobRepository)
                .tasklet(setupDataTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step createDummyPostStep() {
        return new StepBuilder("createDummyPostStep", jobRepository)
                .<Post, Post>chunk(1000, transactionManager)
                .reader(dummyPostReader())
                .writer(jpaPostWriter())
                .build();
    }

    @Bean
    public ItemReader<Post> dummyPostReader() {
        return new DummyPostItemReader(passwordEncoder, galleryRepository, 100000);
    }

    @Bean
    public JpaItemWriter<Post> jpaPostWriter() {
        JpaItemWriter<Post> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
