package org.learn.board.domain.post.application.mapper;

import org.learn.board.domain.post.application.dto.PostDetailResponse;
import org.learn.board.domain.post.application.dto.PostListResponse;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.search.document.PostDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface PostMapper {

    // Post -> PostDetailResponse 동일 필드명 매핑
    PostDetailResponse toDetailResponse(Post post);

    // Post -> PostListResponse 매핑
    @Mapping(target = "createdAt", expression = "java(formatCreatedAt(post.getCreatedAt()))")
    PostListResponse toListResponse(Post post);

    // PostDocument 변환
    @Mapping(target = "createdAt", expression = "java(formatCreatedAt(postDocument.getCreatedAt()))")
    @Mapping(target = "commentCount", ignore = true)
    PostListResponse toListResponse(PostDocument postDocument);

    // 매핑 중 호출 가능하게 default 메소드로 날짜 포맷팅
    default String formatCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDate createdDate = createdAt.toLocalDate();

        if (createdDate.isEqual(today)) {
            return createdAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (createdDate.getYear() == today.getYear()) {
            return createdAt.format(DateTimeFormatter.ofPattern("MM:dd"));
        } else {
            return createdAt.format(DateTimeFormatter.ofPattern("yy.MM.dd"));
        }
    }
}
