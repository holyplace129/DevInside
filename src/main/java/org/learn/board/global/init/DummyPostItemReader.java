package org.learn.board.global.init;

import net.datafaker.Faker;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.domain.Post;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

public class DummyPostItemReader implements ItemReader<Post> {

    private final PasswordEncoder passwordEncoder;
    private final GalleryRepository galleryRepository;
    private final int totalSize;
    private int currentIndex = 0;
    private Faker faker;
    private String encryptedPassword;
    private Gallery gallery;

    public DummyPostItemReader(PasswordEncoder passwordEncoder, GalleryRepository galleryRepository, int totalSize) {
        this.passwordEncoder = passwordEncoder;
        this.galleryRepository = galleryRepository;
        this.totalSize = totalSize;
    }

    private void initialize() {
        if (faker == null) {
            this.gallery = galleryRepository.findByName("test-gallery")
                    .orElseThrow(() -> new IllegalStateException("테스트 갤러리를 찾을 수 없습니다."));
            this.faker = new Faker(new Locale("ko"));
            this.encryptedPassword = passwordEncoder.encode("1234");
        }
    }


    @Override
    public Post read() {
        initialize();

        if (currentIndex >= totalSize) {
            return null;
        }

        Post post = Post.builder()
                .gallery(this.gallery)
                .title(faker.lorem().sentence(5, 10))
                .content(String.join("\n", faker.lorem().paragraphs(3)))
                .writer(faker.name().fullName())
                .password(encryptedPassword)
                .build();

        currentIndex++;
        return post;
    }
}
