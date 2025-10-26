package org.learn.board.domain.gallery.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.learn.board.global.domain.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Gallery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gallery_id_seq")
    @SequenceGenerator(name = "gallery_id_seq", sequenceName = "GALLERY_ID_SEQ", allocationSize = 1)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(length = 100, nullable = false)
    private String displayName;

    @Column(length = 255)
    private String description;

    @Builder
    public Gallery(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    public void update(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}