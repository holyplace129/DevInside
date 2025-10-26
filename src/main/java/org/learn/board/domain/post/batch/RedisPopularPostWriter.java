package org.learn.board.domain.post.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPopularPostWriter implements ItemWriter<GalleryPopularPostProcessor.GalleryWithPopularPosts> {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void write(Chunk<? extends GalleryPopularPostProcessor.GalleryWithPopularPosts> chunk) throws Exception {
        for (GalleryPopularPostProcessor.GalleryWithPopularPosts data : chunk.getItems()) {
            if (!data.popularPosts().isEmpty()) {
                String galleryKey = "popular:posts:gallery:" + data.gallery().getName();
                redisTemplate.opsForValue().set(galleryKey, data.popularPosts(), 15, TimeUnit.MINUTES);
                log.debug("{} 갤러리 인기 게시글 {}건 Redis 저장", data.gallery().getDisplayName(), data.popularPosts().size());
            }
        }
    }
}
