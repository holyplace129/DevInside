package org.learn.board.domain.report.domain;

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
        @UniqueConstraint(name = "uk_comment_report", columnNames = {"comment_id", "reporterIp"})
})
@Entity
public class CommentReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_report_id_seq")
    @SequenceGenerator(name = "comment_report_id_seq", sequenceName = "COMMENT_REPORT_ID_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(length = 45, nullable = false)
    private String reporterIp;

    @Column(length = 30, nullable = false)
    private String reasonCode;

    @Lob
    private String reasonDetail;

    @Builder
    public CommentReport(Comment comment, String reporterIp, String reasonCode, String reasonDetail) {
        this.comment = comment;
        this.reporterIp = reporterIp;
        this.reasonCode = reasonCode;
        this.reasonDetail = reasonDetail;
    }
}