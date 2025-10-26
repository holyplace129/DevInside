package org.learn.board.domain.post.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PopularPostScheduler {

    private final PostRepository postRepository;
    private final GalleryRepository galleryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;

    private static final String TOTAL_POPULAR_POSTS_KEY = "popular:posts:total";

    @Scheduled(fixedRate = 60000)
    @Transactional(readOnly = true)
    public void aggregatePopularPosts() {
        log.info("인기 게시글 집계 스케줄러 시작");

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        List<Post> totalPopularPosts = postRepository
                .findTop10ByCreatedAtAfterAndLikeCountGreaterThanOrderByLikeCountDesc(twentyFourHoursAgo, 0);

        List<PostListResponse> totalDtos = totalPopularPosts.stream()
                .map(postMapper::toListResponse)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(TOTAL_POPULAR_POSTS_KEY, totalDtos, 15, TimeUnit.MINUTES);
        log.info("전체 인기 게시글 {}건 Redis 저장 완료", totalDtos.size());

        List<Gallery> allGalleries = galleryRepository.findAll();
        for (Gallery gallery : allGalleries) {
            List<Post> galleryPopularPosts = postRepository
                    .findTop10ByGalleryAndCreatedAtAfterAndLikeCountGreaterThanOrderByLikeCountDesc(gallery, twentyFourHoursAgo, 0);

            List<PostListResponse> galleryDtos = galleryPopularPosts.stream()
                    .map(postMapper::toListResponse)
                    .collect(Collectors.toList());

            String galleryKey = "popular:posts:gallery" + gallery.getName();
            redisTemplate.opsForValue().set(galleryKey, galleryDtos, 15, TimeUnit.MINUTES);
            log.info("{} 갤러리 인기 게시글 {}건 Redis 저장 완료", gallery.getDisplayName(), galleryDtos.size());
        }

        log.info("인기 게시글 집계 스케줄러 종료");
    }
}
