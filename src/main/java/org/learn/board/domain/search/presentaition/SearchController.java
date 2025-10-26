package org.learn.board.domain.search.presentaition;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.search.application.SearchFacade;
import org.learn.board.domain.search.application.dto.SearchRequest;
import org.learn.board.global.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchFacade searchFacade;

    @GetMapping("/posts")
    public ResponseEntity<PageResponse<PostListResponse>> searchPost(
            @ModelAttribute SearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PostListResponse> responses = searchFacade.searchPosts(searchRequest, pageable);

        return ResponseEntity.ok(responses);
    }
}
