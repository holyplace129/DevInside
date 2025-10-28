package org.learn.board.global.init;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.gallery.domain.Gallery;
import org.learn.board.domain.gallery.domain.repository.GalleryRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepDataTasklet implements Tasklet {

    private final GalleryRepository galleryRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Gallery testGallery = galleryRepository.findByName("test-gallery")
                .orElseGet(() -> galleryRepository.save(
                        Gallery.builder()
                                .name("test-gallery")
                                .displayName("대용량 테스트 갤러시")
                                .build()
                ));

        chunkContext.getStepContext().getStepExecution().getJobExecution()
                .getExecutionContext().put("galleryId", testGallery.getId());

        return RepeatStatus.FINISHED;
    }
}
