package org.learn.board.domain.vote.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.vote.domain.repository.PostVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class VoteFacadeConcurrencyTest {

    @Autowired
    private VoteFacade voteFacade;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostVoteRepository postVoteRepository;

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testPostId;

    @BeforeEach
    void setUp() {
        Gallery testGallery = galleryRepository.save(
                Gallery.builder()
                        .name("test-gallery")
                        .displayName("테스트 갤러리")
                        .build()
        );

        Post testPost = postRepository.save(
                Post.builder()
                        .gallery(testGallery)
                        .title("동시성 테스트 게시글")
                        .content("내용")
                        .writer("테스터")
                        .password(passwordEncoder.encode("1234"))
                        .build()
        );

        this.testPostId = testPost.getId();
    }

    @AfterEach
    void tearDown() {
        postVoteRepository.deleteAll();
        postRepository.deleteAll();
        galleryRepository.deleteAll();
    }

    @Test
    void 게시글_추천_동시성_테스트() throws InterruptedException {
        // given
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // when
        for (int i = 0; i < numberOfThreads; i++) {
            String uniqueVoterIp = "127.0.0." + i;

            executorService.submit(() -> {
                try {
                    voteFacade.likePost(testPostId, uniqueVoterIp);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Post resultPost = postRepository.findById(testPostId)
                .orElseThrow(() -> new RuntimeException("테스트 게시글을 찾을 수 없습니다."));

        System.out.println("최종 likeCount : " + resultPost.getLikeCount());
        System.out.println("기대 likeCount : 100");

        assertThat(resultPost.getLikeCount()).isEqualTo(100);
    }
}
