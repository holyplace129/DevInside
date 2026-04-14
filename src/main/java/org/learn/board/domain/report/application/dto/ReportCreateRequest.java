package org.learn.board.domain.report.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportCreateRequest {

    @NotBlank(message = "신고 사유는 필수입니다.")
    @Size(max = 30, message = "신고 사유 코드는 30자 이하여야 합니다.")
    private String reasonCode;

    private String reasonDetail;
}
