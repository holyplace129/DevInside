package org.learn.board.domain.search.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.search.application.dto.SearchRequest;
import org.learn.board.global.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchFacade {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PageResponse<PostListResponse> searchPosts(SearchRequest searchRequest, Pageable pageable) {
        if (searchRequest.getKeyword() == null || searchRequest.getKeyword().isBlank()) {
            Page<Post> posts;
        }

        Page<Post> posts = switch (searchRequest.getType()) {
            case TITLE -> postRepository.findByTitleContaining(searchRequest.getKeyword(), pageable);
            case CONTENT -> postRepository.findByContentContaining(searchRequest.getKeyword(), pageable);
            case WRITER -> postRepository.findByWriter(searchRequest.getKeyword(), pageable);
            case TITLE_CONTENT ->
                    postRepository.findByTitleContainingOrContentContaining(searchRequest.getKeyword(), pageable);
            default -> throw new IllegalArgumentException("유효하지 않은 검색 타입입니다: " + searchRequest.getType());
        };

        Page<PostListResponse> dtoPage = posts.map(postMapper::toListResponse);

        return new PageResponse<>(dtoPage);
    }

}
