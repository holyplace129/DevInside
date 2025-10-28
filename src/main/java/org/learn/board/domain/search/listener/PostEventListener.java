package org.learn.board.domain.search.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.learn.board.domain.post.event.PostSavedEvent;
import org.learn.board.domain.search.document.PostDocument;
import org.learn.board.domain.search.document.PostDocumentRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final PostDocumentRepository postDocumentRepository;

    @Async
    @TransactionalEventListener
    public void handlePostSavedEvent(PostSavedEvent event) {
        log.info("게시글 저장/수정 이벤트 수신. Elasticsearch 인덱싱 시작. postId: {}", event.getId());

        try {
            PostDocument postDocument = PostDocument.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .content(event.getContent())
                    .writer(event.getWriter())
                    .galleryName(event.getGalleryName())
                    .createdAt(event.getCreatedAt())
                    .likeCount(event.getLikeCount())
                    .build();

            postDocumentRepository.save(postDocument);

            log.info("Elasticsearch 인덱싱 완료. postId: {}", event.getId());
        } catch (Exception e) {
            log.error("Elasticsearch 인덱싱 중 오류 발생. postId: {}", event.getId());
        }
    }
}
