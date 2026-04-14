package org.learn.board.domain.comment.domain.repository;

import org.learn.board.domain.comment.domain.Comment;
import org.learn.board.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPost(Post post);

    // 추천수 원자적 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    // 신고수 원자적 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.reportCount = c.reportCount + 1 WHERE c.id = :commentId")
    void incrementReportCount(@Param("commentId") Long commentId);
}
