package org.learn.board.domain.comment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentCreateRequest {

    // 대댓글인 경우 부모 댓글의 ID
    private Long parentId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
    private String content;

    @NotBlank(message = "작성자는 필수입니다.")
    @Size(max = 30, message = "작성자는 30자를 초과할 수 없습니다.")
    private String writer;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하여야 합니다.")
    private String password;
}