package org.learn.board.domain.post.batch;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GalleryPopularPostProcessor implements ItemProcessor<Gallery, GalleryPopularPostProcessor.GalleryWithPopularPosts> {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    public GalleryWithPopularPosts process(Gallery gallery) throws Exception {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        List<Post> popularPosts = postRepository
                .findTop10ByGalleryAndCreatedAtAfterAndLikeCountGreaterThanOrderByLikeCountDesc(
                        gallery, twentyFourHoursAgo, 0
                );

        List<PostListResponse> dtoList = popularPosts.stream()
                .map(postMapper::toListResponse)
                .collect(Collectors.toList());

        return new GalleryWithPopularPosts(gallery, dtoList);
    }

    public record GalleryWithPopularPosts(Gallery gallery, List<PostListResponse> popularPosts) {}
}