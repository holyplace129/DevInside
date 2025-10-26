package org.learn.board.domain.post.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.learn.board.global.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_image_id_seq")
    @SequenceGenerator(name = "post_image_id_seq", sequenceName = "POST_IMAGE_ID_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(length = 512, nullable = false)
    private String fileUrl;

    @Column(length = 255)
    private String originalFileName;

    private Integer fileSizeKb;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public PostImage(Post post, String fileUrl, String originalFileName, Integer fileSizeKb, int sortOrder) {
        this.post = post;
        this.fileUrl = fileUrl;
        this.originalFileName = originalFileName;
        this.fileSizeKb = fileSizeKb;
        this.sortOrder = sortOrder;
    }
}