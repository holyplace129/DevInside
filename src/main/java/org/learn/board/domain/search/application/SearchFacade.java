package org.learn.board.domain.search.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.search.application.dto.SearchRequest;
import org.learn.board.domain.search.document.PostDocument;
import org.learn.board.domain.search.document.PostDocumentRepository;
import org.learn.board.global.common.PageResponse;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchFacade {

    private final PostDocumentRepository postDocumentRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PageResponse<PostListResponse> searchPosts(SearchRequest searchRequest, Pageable pageable) {
        if (searchRequest.getKeyword() == null || searchRequest.getKeyword().isBlank()) {
            return new PageResponse<>(Page.empty());
        }

        String keyword = searchRequest.getKeyword();

        try {
            Page<PostDocument> documents = postDocumentRepository
                    .findByTitleContainsOrContentContains(keyword, keyword, pageable);
            Page<PostListResponse> dtoPage = documents.map(postMapper::toListResponse);
            return new PageResponse<>(dtoPage);
        } catch (DataAccessResourceFailureException e) {
            log.warn("Elasticsearch 연결 실패. DB 폴백 검색 수행. keyword: {}", keyword);
            Page<Post> posts = postRepository.findByTitleContainingOrContentContaining(keyword, pageable);
            Page<PostListResponse> dtoPage = posts.map(postMapper::toListResponse);
            return new PageResponse<>(dtoPage);
        }
    }
}
