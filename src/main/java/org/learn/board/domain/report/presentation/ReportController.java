package org.learn.board.domain.report.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.learn.board.domain.report.application.ReportFacade;
import org.learn.board.domain.report.application.dto.ReportCreateRequest;
import org.learn.board.global.util.ClientIpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportFacade reportFacade;

    // 게시글 신고
    @PostMapping("/galleries/{galleryName}/posts/{postId}/reports")
    public ResponseEntity<Void> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportCreateRequest requestDto,
            HttpServletRequest request) {
        String clientIp = ClientIpUtil.getClientIp(request);
        reportFacade.reportPost(postId, clientIp, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 댓글 신고
    @PostMapping("/galleries/{galleryName}/posts/{postId}/comments/{commentId}/reports")
    public ResponseEntity<Void> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportCreateRequest requestDto,
            HttpServletRequest request) {
        String clientIp = ClientIpUtil.getClientIp(request);
        reportFacade.reportComment(commentId, clientIp, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
