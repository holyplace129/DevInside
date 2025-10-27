package org.learn.board.domain.post.event;

import lombok.Getter;
import org.learn.board.domain.post.domain.Post;

import java.time.LocalDateTime;

@Getter
public class PostSavedEvent {

    private final Long id;
    private final String title;
    private final String content;
    private final String writer;
    private final String galleryName;
    private final LocalDateTime createdAt;
    private final int likeCount;

    public static PostSavedEvent from(Post post) {
        return new PostSavedEvent(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getGallery().getName(), // Gallery 객체에서 이름만 추출
                post.getCreatedAt(),
                post.getLikeCount()
        );
    }

    public PostSavedEvent(Long id, String title, String content, String writer, String galleryName, LocalDateTime createdAt, int likeCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.galleryName = galleryName;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
    }
}
