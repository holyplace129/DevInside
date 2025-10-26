package org.learn.board.domain.post.presentation;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.post.application.PostFacade;
import org.learn.board.domain.post.application.PostQueryFacade;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.global.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class TotalPostController {

    private final PostQueryFacade postQueryFacade;

    // 전체 게시글 목록 조회
    @GetMapping
    public ResponseEntity<PageResponse<PostListResponse>> findAllPosts(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PostListResponse> responses = postQueryFacade.findAllPosts(pageable);
        return ResponseEntity.ok(responses);
    }

    // 전체 인기 게시글
    @GetMapping("/popular")
    public ResponseEntity<List<PostListResponse>> findTotalPopularPosts() {
        List<PostListResponse> responses = postQueryFacade.findTotalPopularPosts();
        return ResponseEntity.ok(responses);
    }
}