package org.learn.board.domain.post.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.learn.board.domain.post.application.PostFacade;
import org.learn.board.domain.post.application.PostQueryFacade;
import org.learn.board.domain.post.application.dto.PostCreateRequest;
import org.learn.board.domain.post.application.dto.PostDetailResponse;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.application.dto.PostUpdateRequest;
import org.learn.board.global.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/galleries/{galleryName}/posts")
public class PostController {

    private final PostFacade postFacade;
    private final PostQueryFacade postQueryFacade;

    // --- 쓰기 ---

    // 게시글 작성
    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost (
            @PathVariable String galleryName, @Valid @RequestBody PostCreateRequest request) {
        PostDetailResponse responsePost = postFacade.createPost(galleryName, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responsePost);
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable String galleryName,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        postFacade.updatePost(postId, request);
        return ResponseEntity.ok().build();
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String galleryName,
            @PathVariable Long postId,
            @RequestBody Map<String, String> passwordMap) {
        String password = passwordMap.get("password");
        if (password == null) {
            return ResponseEntity.badRequest().build();
        }
        postFacade.deletePost(postId, password);
        return ResponseEntity.noContent().build();
    }

    // --- 읽기 ---

    // 갤러리 내 게시글 목록
    @GetMapping
    public ResponseEntity<PageResponse<PostListResponse>> findPostByGallery(
            @PathVariable String galleryName,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PostListResponse> responses = postQueryFacade.findPostsByGallery(galleryName, pageable);
        return ResponseEntity.ok(responses);
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> findPostById(
            @PathVariable String galleryName,
            @PathVariable Long postId) {
        PostDetailResponse response = postQueryFacade.findPostById(postId);
        postFacade.increaseViewCount(postId);
        return ResponseEntity.ok(response);
    }

    // 갤러리별 인기 게시글
    @GetMapping("/popular")
    public ResponseEntity<List<PostListResponse>> findGalleryPopularPosts(@PathVariable String galleryName) {
        List<PostListResponse> responses = postQueryFacade.findGalleryPopularPosts(galleryName);
        return ResponseEntity.ok(responses);
    }
}
