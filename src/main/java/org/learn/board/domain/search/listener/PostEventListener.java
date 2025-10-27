package org.learn.board.domain.search.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.learn.board.domain.post.event.PostSavedEvent;
import org.learn.board.domain.search.document.PostDocument;
import org.learn.board.domain.search.document.PostDocumentRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final PostDocumentRepository postDocumentRepository;

    @Async
    @EventListener
    public void handlePostSavedEvent(PostSavedEvent event) {
        log.info("게시글 저장/수정 이벤트 수신. Elasticsearch 인덱싱 시작. postId: {}", event.getId());

        // 1. 이벤트 DTO를 PostDocument로 변환
        PostDocument postDocument = PostDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .writer(event.getWriter())
                .galleryName(event.getGalleryName())
                .createdAt(event.getCreatedAt())
                .likeCount(event.getLikeCount())
                .build();

        // 2. Elasticsearch에 저장 (ID 같으면 덮어쓰기)
        postDocumentRepository.save(postDocument);

        log.info("Elasticsearch 인덱싱 완료. postId: {}", event.getId());
    }
}
