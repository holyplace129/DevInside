package org.learn.board.domain.gallery.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.gallery.application.dto.GalleryCreateRequest;
import org.learn.board.domain.gallery.application.dto.GalleryResponse;
import org.learn.board.domain.gallery.application.dto.GalleryUpdateRequest;
import org.learn.board.domain.gallery.application.mapper.GalleryMapper;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.learn.board.global.error.ErrorCode;
import org.learn.board.global.error.exception.EntityNotFoundException;
import org.learn.board.global.error.exception.InvalidValueException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class GalleryFacade {

    private final GalleryRepository galleryRepository;
    private final GalleryMapper galleryMapper;

    // 갤러리 생성
    public GalleryResponse createGallery(GalleryCreateRequest request) {
        if (galleryRepository.findByName(request.getName()).isPresent()) {
            throw new InvalidValueException(ErrorCode.GALLERY_NAME_DUPLICATED);
        }

        Gallery gallery = Gallery.builder()
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .build();

        Gallery savedGallery = galleryRepository.save(gallery);

        return galleryMapper.toResponse(savedGallery);
    }



    // 갤러리 수정
    public void updateGallery(String name, GalleryUpdateRequest request) {
        Gallery gallery = galleryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.GALLERY_NOT_FOUND));

        gallery.update(request.getDisplayName(), request.getDescription());
    }


    // 갤러리 삭제
    public void deleteGallery(String name) {
        Gallery gallery = galleryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.GALLERY_NOT_FOUND));

        galleryRepository.delete(gallery);
    }
}