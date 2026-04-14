package org.learn.board.domain.comment.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.comment.application.dto.CommentCreateRequest;
import org.learn.board.domain.comment.application.dto.CommentResponse;
import org.learn.board.domain.comment.application.dto.CommentUpdateRequest;
import org.learn.board.domain.comment.domain.Comment;
import org.learn.board.domain.comment.domain.repository.CommentRepository;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.global.error.ErrorCode;
import org.learn.board.global.error.exception.EntityNotFoundException;
import org.learn.board.global.error.exception.InvalidValueException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentFacade {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    // 댓글 생성
    @Transactional
    public void createComment(Long postId, CommentCreateRequest request) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException(ErrorCode.POST_NOT_FOUND);
        }

        // 대댓글인 경우 부모 댓글 검증
        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

            // 부모 댓글이 같은 게시글에 속하는지 검증
            if (!parentComment.getPost().getId().equals(postId)) {
                throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
            }

            // 대댓글의 대댓글 방지 (1단계 대댓글만 허용)
            if (parentComment.getParent() != null) {
                throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        Post postRef = postRepository.getReferenceById(postId);
        Comment comment = Comment.builder()
                .post(postRef)
                .parent(parentComment)
                .content(request.getContent())
                .writer(request.getWriter())
                .password(encryptedPassword)
                .build();

        commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);
    }

    // 게시글 내 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));

        List<Comment> comments = commentRepository.findAllByPost(post);

        return buildCommentTree(comments);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), comment.getPassword())) {
            throw new InvalidValueException(ErrorCode.INVALID_PASSWORD);
        }

        comment.update(request.getContent());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String password) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (!passwordEncoder.matches(password, comment.getPassword())) {
            throw new InvalidValueException(ErrorCode.INVALID_PASSWORD);
        }

        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);
        postRepository.decrementCommentCount(postId);
    }

    // 댓글 계층 구조
    private List<CommentResponse> buildCommentTree(List<Comment> comments) {
        Map<Long, CommentResponse> commentResponseMap = comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toMap(CommentResponse::getId, comment -> comment));

        commentResponseMap.values().forEach(commentResponse -> {
            Long parentId = commentResponse.getParentId();
            if (parentId != null) {
                CommentResponse parentResponse = commentResponseMap.get(parentId);
                if (parentResponse != null) {
                    parentResponse.getChildren().add(commentResponse);
                }
            }
        });

        return commentResponseMap.values().stream()
                .filter(commentResponse -> commentResponse.getParentId() == null)
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());

        if (comment.getParent() != null) {
            response.setParentId(comment.getParent().getId());
        }

        response.setContent(comment.getContent());
        response.setWriter(comment.getWriter());
        response.setLikeCount(comment.getLikeCount());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
