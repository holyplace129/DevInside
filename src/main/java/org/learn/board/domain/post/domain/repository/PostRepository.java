package org.learn.board.domain.post.domain.repository;

import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByGallery(Gallery gallery, Pageable pageable);

    // 제목으로 검색
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    // 내용으로 검색
    Page<Post> findByContentContaining(String keyword, Pageable pageable);

    // 작성자로 검색
    Page<Post> findByWriter(String keyword, Pageable pageable);

    // 제목+내용으로 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> findByTitleContainingOrContentContaining(@Param("keyword")String keyword, Pageable pageable);

    // 24이내 작성, 추천 수 1이상 상위 10개
    List<Post> findTop10ByCreatedAtAfterAndLikeCountGreaterThanOrderByLikeCountDesc(LocalDateTime localDateTime, int likeCount);

    // 갤러리 내에서 24이내 작성
    List<Post> findTop10ByGalleryAndCreatedAtAfterAndLikeCountGreaterThanOrderByLikeCountDesc(Gallery gallery, LocalDateTime localDateTime, int likeCount);

    // 조회수 원자적 증가 (동시성 문제 방지)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 추천수 원자적 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    // 비추천수 원자적 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.dislikeCount = p.dislikeCount + 1 WHERE p.id = :postId")
    void incrementDislikeCount(@Param("postId") Long postId);

    // 신고수 원자적 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.reportCount = p.reportCount + 1 WHERE p.id = :postId")
    void incrementReportCount(@Param("postId") Long postId);

    // 댓글수 원자적 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    // 댓글수 원자적 감소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Post p")
    void deleteAllInBatch();
}
