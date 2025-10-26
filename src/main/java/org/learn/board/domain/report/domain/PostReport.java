package org.learn.board.domain.report.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.global.domain.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_report", columnNames = {"post_id", "reporterIp"})
})
@Entity
public class PostReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_report_id_seq")
    @SequenceGenerator(name = "post_REPORT_id_seq", sequenceName = "POST_REPORT_ID_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(length = 45, nullable = false)
    private String reporterIp;

    @Column(length = 30, nullable = false)
    private String reasonCode;

    @Lob
    private String reasonDetail;

    @Builder
    public PostReport(Post post, String reporterIp, String reasonCode, String reasonDetail) {
        this.post = post;
        this.reporterIp = reporterIp;
        this.reasonCode = reasonCode;
        this.reasonDetail = reasonDetail;
    }
}