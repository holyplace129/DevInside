package org.learn.board.domain.comment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_id_seq")
    @SequenceGenerator(name = "comment_id_seq", sequenceName = "COMMENT_ID_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(length = 1000, nullable = false)
    private String content;

    @Column(length = 30, nullable = false)
    @ColumnDefault("'ㅇㅇ'")
    private String writer;

    @Column(nullable = false)
    private String password;

    @ColumnDefault("0")
    private int likeCount;

    @ColumnDefault("0")
    private int reportCount;

    @Builder
    public Comment(Post post, Comment parent, String content, String writer, String password, int likeCount, int reportCount) {
        this.post = post;
        this.parent = parent;
        this.content = content;
        this.writer = (writer != null) ? writer : "ㅇㅇ";
        this.password = password;
        this.likeCount = 0;
        this.reportCount = 0;
    }

    public void update(String content) {
        this.content = content;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void increaseReportCount() {
        this.reportCount++;
    }
}