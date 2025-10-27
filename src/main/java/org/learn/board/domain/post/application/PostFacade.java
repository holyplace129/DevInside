package org.learn.board.domain.post.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.domain.post.application.dto.PostCreateRequest;
import org.learn.board.domain.post.application.dto.PostDetailResponse;
import org.learn.board.domain.post.application.dto.PostUpdateRequest;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.PostImage;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.post.event.PostSavedEvent;
import org.learn.board.global.error.ErrorCode;
import org.learn.board.global.error.exception.EntityNotFoundException;
import org.learn.board.global.error.exception.InvalidValueException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostFacade {

    private final PostRepository postRepository;
    private final GalleryRepository galleryRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostMapper postMapper;
    private final ApplicationEventPublisher eventPublisher;

    // 게시글 생성
    public PostDetailResponse createPost(String galleryName, PostCreateRequest request) {
        Gallery gallery = galleryRepository.findByName(galleryName)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.GALLERY_NOT_FOUND));

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        Post post = Post.builder()
                .gallery(gallery)
                .title(request.getTitle())
                .content(request.getContent())
                .writer(request.getWriter())
                .password(encryptedPassword)
                .build();

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            request.getImageUrl().forEach(imageUrl -> {
                PostImage postImage = PostImage.builder()
                        .post(post)
                        .fileUrl(imageUrl)
                        .build();
                post.addImage(postImage);
            });
        }

        Post savePost = postRepository.save(post);

        eventPublisher.publishEvent(PostSavedEvent.from(savePost));

        return postMapper.toDetailResponse(savePost);
    }



    // 게시글 수정
    @Caching(evict = {
            @CacheEvict(value = "postDetail", key = "#postId"),
            @CacheEvict(value = "galleryPosts", allEntries = true) // 'galleryPosts' 캐시 전체를 비움
    })
    public void updatePost(Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));

        if (!post.isPasswordMatches(request.getPassword(), passwordEncoder)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }

        post.update(request.getTitle(), request.getContent());

        eventPublisher.publishEvent(PostSavedEvent.from(post));
    }

    // 게시글 삭제
    @Caching(evict = {
            @CacheEvict(value = "postDetail", key = "#postId"),
            @CacheEvict(value = "galleryPosts", allEntries = true)
    })
    public void deletePost(Long postId, String password) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));

        if (!post.isPasswordMatches(password, passwordEncoder)) {
            throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
        }

        postRepository.delete(post);
    }

    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));
        post.increaseViewCount();
    }
}
