package org.learn.board.domain.post.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private int viewCount;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 참고: password 필드는 절대 응답에 포함하지 않습니다.
}