package org.learn.board;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class BatchJobTest {

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job createDummyDataJob;
    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        galleryRepository.save(
                Gallery.builder()
                        .name("test-gallery")
                        .displayName("대용량 테스트 갤러리")
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAllInBatch();
        galleryRepository.deleteAllInBatch();
    }

    @Test
    @Commit
    void runCreateDummyDataJob() throws Exception {
        jobLauncher.run(createDummyDataJob, new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters());
    }
}
