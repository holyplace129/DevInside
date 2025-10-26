package org.learn.board.domain.vote.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.learn.board.domain.comment.domain.Comment;
import org.learn.board.global.domain.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_vote", columnNames = {"comment_id", "voterIp"})
})
@Entity
public class CommentVote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_vote_id_seq")
    @SequenceGenerator(name = "comment_vote_id_seq", sequenceName = "COMMENT_VOTE_ID_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(length = 45, nullable = false)
    private String voterIp;

    @Builder
    public CommentVote(Comment comment, String voterIp) {
        this.comment = comment;
        this.voterIp = voterIp;
    }
}