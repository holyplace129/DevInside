package org.learn.board.domain.search.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostDocumentRepository extends ElasticsearchRepository<PostDocument, Long> {

    Page<PostDocument> findByTitleContainsOrContentContains(String title, String content, Pageable pageable);
}
