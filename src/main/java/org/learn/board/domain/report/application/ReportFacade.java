package org.learn.board.domain.report.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.comment.domain.Comment;
import org.learn.board.domain.comment.domain.repository.CommentRepository;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.report.application.dto.ReportCreateRequest;
import org.learn.board.domain.report.domain.CommentReport;
import org.learn.board.domain.report.domain.PostReport;
import org.learn.board.domain.report.domain.repository.CommentReportRepository;
import org.learn.board.domain.report.domain.repository.PostReportRepository;
import org.learn.board.global.error.ErrorCode;
import org.learn.board.global.error.exception.EntityNotFoundException;
import org.learn.board.global.error.exception.InvalidValueException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportFacade {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;

    // 게시글 신고
    public void reportPost(Long postId, String reporterIp, ReportCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));

        postReportRepository.findByPostAndReporterIp(post, reporterIp).ifPresent(r -> {
            throw new InvalidValueException(ErrorCode.ALREADY_REPORTED);
        });

        PostReport postReport = PostReport.builder()
                .post(post)
                .reporterIp(reporterIp)
                .reasonCode(request.getReasonCode())
                .reasonDetail(request.getReasonDetail())
                .build();
        postReportRepository.save(postReport);

        postRepository.incrementReportCount(postId);
    }

    // 댓글 신고
    public void reportComment(Long commentId, String reporterIp, ReportCreateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        commentReportRepository.findByCommentAndReporterIp(comment, reporterIp).ifPresent(r -> {
            throw new InvalidValueException(ErrorCode.ALREADY_REPORTED);
        });

        CommentReport commentReport = CommentReport.builder()
                .comment(comment)
                .reporterIp(reporterIp)
                .reasonCode(request.getReasonCode())
                .reasonDetail(request.getReasonDetail())
                .build();

        commentReportRepository.save(commentReport);

        commentRepository.incrementReportCount(commentId);
    }
}
