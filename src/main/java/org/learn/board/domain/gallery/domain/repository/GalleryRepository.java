package org.learn.board.domain.gallery.domain.repository;

import org.learn.board.domain.gallery.domain.Gallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    Optional<Gallery> findByName(String name);

    @Modifying(clearAutomatically = true)
    @Query("DELETE from Gallery ")
    void deleteAllInBatch();
}