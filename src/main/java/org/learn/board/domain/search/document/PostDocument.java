package org.learn.board.domain.search.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Getter
@Document(indexName = "posts")
public class PostDocument {

    @Id
    private final Long id;

    @Field(type = FieldType.Text)
    private final String title;

    @Field(type = FieldType.Text)
    private final String content;

    @Field(type = FieldType.Keyword)
    private final String writer;

    @Field(type = FieldType.Keyword)
    private final String galleryName;

    @Field(type = FieldType.Date)
    private final LocalDateTime createdAt;

    @Field(type = FieldType.Integer)
    private final int likeCount;

    @Builder
    public PostDocument(String galleryName, Long id, String title, String content, String writer, LocalDateTime createdAt, int likeCount) {
        this.galleryName = galleryName;
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
    }
}
