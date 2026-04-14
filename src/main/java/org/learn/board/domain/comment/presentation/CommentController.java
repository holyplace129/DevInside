package org.learn.board.domain.comment.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.learn.board.domain.comment.application.CommentFacade;
import org.learn.board.domain.comment.application.dto.CommentCreateRequest;
import org.learn.board.domain.comment.application.dto.CommentResponse;
import org.learn.board.domain.comment.application.dto.CommentUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/galleries/{galleryName}/posts/{postId}/comments")
public class CommentController {

    private final CommentFacade commentFacade;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        commentFacade.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 게시글 내 댓글 조회
    @GetMapping
    public ResponseEntity<List<CommentResponse>> findCommentByPostId(@PathVariable Long postId) {
        List<CommentResponse> responses = commentFacade.findCommentByPost(postId);
        return ResponseEntity.ok(responses);
    }

    // 게시글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        commentFacade.updateComment(commentId, request);
        return ResponseEntity.ok().build();
    }

    // 게시글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> passwordMap) {
        String password = passwordMap.get("password");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        commentFacade.deleteComment(commentId, password);
        return ResponseEntity.noContent().build();
    }
}
