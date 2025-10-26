package org.learn.board.global.init;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("init-data") // test 프로파일에서는 실행되지 않도록 설정
@RequiredArgsConstructor
public class DataInitializer {

    private final GalleryRepository galleryRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        // 1. 갤러리 생성 (기존 data.sql 내용과 유사)
        Gallery baseballGallery = galleryRepository.save(Gallery.builder().name("baseball").displayName("국내야구 갤러리").build());

        // 2. 대용량 게시글 데이터 생성 (예: 2만 건)
        List<Post> posts = new ArrayList<>();
        String encryptedPassword = passwordEncoder.encode("1234");

        for (int i = 1; i <= 20000; i++) {
            Post post = Post.builder()
                    .gallery(baseballGallery)
                    .title("테스트 게시글 " + i)
                    .content("성능 테스트를 위한 내용입니다. " + i)
                    .writer("테스터")
                    .password(encryptedPassword)
                    .build();
            posts.add(post);
        }

        // 3. JpaRepository의 saveAll을 사용하여 배치 INSERT로 성능 최적화
        postRepository.saveAll(posts);
    }
}
