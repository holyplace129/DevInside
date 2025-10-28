package org.learn.board.domain.search.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.mapper.PostMapper;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.search.application.dto.SearchRequest;
import org.learn.board.domain.search.document.PostDocument;
import org.learn.board.domain.search.document.PostDocumentRepository;
import org.learn.board.global.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchFacade {

    private final PostDocumentRepository postDocumentRepository;
    private final PostMapper postMapper;

    public PageResponse<PostListResponse> searchPosts(SearchRequest searchRequest, Pageable pageable) {
        if (searchRequest.getKeyword() == null || searchRequest.getKeyword().isBlank()) {
            return new PageResponse<>(Page.empty());
        }

        Page<PostDocument> documents;
        String keyword = searchRequest.getKeyword();

        documents = postDocumentRepository.findByTitleContainsOrContentContains(keyword, keyword, pageable);

        Page<PostListResponse> dtoPage = documents.map(postMapper::toListResponse);
        return new PageResponse<>(dtoPage);
    }

}
