# 코드 리뷰 버그 수정 보고서

> 작성일: 2026-04-14  
> 대상 프로젝트: Spring Boot 익명 게시판 (DCInside 스타일)  
> 기술 스택: Spring Boot 3.5.4, JPA/MySQL, Redis/Redisson, Elasticsearch, Spring Batch

---

## 목차

1. [수정 이력 요약](#1-수정-이력-요약)
2. [1차 수정 (이전 대화)](#2-1차-수정-이전-대화)
3. [2차 수정 (이번 검토)](#3-2차-수정-이번-검토)
4. [아키텍처 개요](#4-아키텍처-개요)
5. [미수정 사항 및 권장 개선](#5-미수정-사항-및-권장-개선)

---

## 1. 수정 이력 요약

| 회차 | 수정 건수 | 주요 내용 |
|------|-----------|-----------|
| 1차  | 18건      | 컴파일 에러, 런타임 에러, 예외 타입 오류 등 |
| 2차  | 11건      | 동시성, 데이터 정합성, 보안, 성능, API 규격 |

---

## 2. 1차 수정 (이전 대화)

### 2-1. [CRITICAL] BaseTimeEntity — 잘못된 EntityListeners
- **파일**: `global/domain/BaseTimeEntity.java`
- **문제**: `@EntityListeners(AutoCloseable.class)` → `createdAt`, `updatedAt` 필드가 전혀 채워지지 않음
- **수정**: `@EntityListeners(AuditingEntityListener.class)`

### 2-2. [CRITICAL] 애플리케이션 기동 실패 — Elasticsearch 연결 오류
- **파일**: `domain/search/document/PostDocument.java`
- **문제**: `@Document(indexName = "posts")` 기본값 `createIndex = true`로 인해 시작 시 ES 연결 시도 → Connection refused
- **수정**: `@Document(indexName = "posts", createIndex = false)`

### 2-3. [CRITICAL] 배치 기동 오류 — Job 이름 미지정
- **파일**: `src/main/resources/application.yml`
- **문제**: `spring.batch.job.execution.enabled: false`는 Spring Boot 3.x에서 무효한 설정 (묵시적으로 무시됨) → 두 개의 Job이 동시 실행 시도
- **수정**: `spring.batch.job.enabled: false`

### 2-4. [HIGH] Redis 직렬화 오류 — LocalDateTime 미지원
- **파일**: `global/config/RedisCacheConfig.java`
- **문제**: 기본 `GenericJackson2JsonRedisSerializer`에 `JavaTimeModule` 미등록 → `LocalDateTime` 직렬화 실패
- **수정**: 커스텀 `ObjectMapper`에 `JavaTimeModule` 등록, `WRITE_DATES_AS_TIMESTAMPS` 비활성화

### 2-5. [HIGH] 댓글 삭제 시 FK 제약 위반
- **파일**: `domain/comment/domain/Comment.java`
- **문제**: `children` 컬렉션 없이 부모 댓글 삭제 시도 → FK constraint violation
- **수정**: `@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true) List<Comment> children` 추가

### 2-6. [HIGH] VoteFacade/ReportFacade — 잘못된 예외 타입
- **파일**: `domain/vote/application/VoteFacade.java`, `domain/report/application/ReportFacade.java`
- **문제**: 중복 추천/신고 시 `EntityNotFoundException(404)` 반환
- **수정**: `InvalidValueException(409 Conflict)`로 교체

### 2-7. [HIGH] PostQueryFacade — 범용 예외 사용
- **파일**: `domain/post/application/PostQueryFacade.java`
- **문제**: `IllegalArgumentException("존재하지 않는 갤러리/게시글")` → GlobalExceptionHandler 처리 안 됨
- **수정**: `EntityNotFoundException(ErrorCode.GALLERY_NOT_FOUND)`, `EntityNotFoundException(ErrorCode.POST_NOT_FOUND)`로 교체

### 2-8. [HIGH] CommentFacade — 범용 예외 사용
- **파일**: `domain/comment/application/CommentFacade.java`
- **문제**: 모든 예외를 `IllegalArgumentException`으로 처리
- **수정**: `EntityNotFoundException`, `InvalidValueException`으로 타입 분리

### 2-9. [MEDIUM] PostFacade — 게시글 작성 시 galleryPosts 캐시 미삭제
- **파일**: `domain/post/application/PostFacade.java`
- **문제**: 게시글 작성 후 갤러리 목록 캐시가 갱신되지 않아 새 게시글이 목록에 미표시
- **수정**: `@CacheEvict(value = "galleryPosts", allEntries = true)` 추가

### 2-10. [MEDIUM] 조회수 증가 — 동시성 문제
- **파일**: `domain/post/application/PostFacade.java`, `domain/post/domain/repository/PostRepository.java`
- **문제**: `post.increaseViewCount()` (인메모리 증가) → 동시 요청 시 Lost Update 발생
- **수정**: `@Modifying @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")` 원자적 쿼리 사용

### 2-11. [MEDIUM] PostMapper — 날짜 포맷 오타
- **파일**: `domain/post/application/mapper/PostMapper.java:41`
- **문제**: `"MM:dd"` → 날짜가 `04:13` 형태로 표시됨
- **수정**: `"MM.dd"`

### 2-12. [MEDIUM] GalleryFacade — 중복 갤러리명 예외 타입 오류
- **파일**: `domain/gallery/application/GalleryFacade.java:29`
- **문제**: `EntityNotFoundException(GALLERY_NAME_DUPLICATED)` → HTTP 404 반환 (실제로는 409 Conflict)
- **수정**: `InvalidValueException(GALLERY_NAME_DUPLICATED)`

### 2-13. [MEDIUM] PostController — 조회수 캐시 stale 문제
- **파일**: `domain/post/presentation/PostController.java`, `domain/post/application/PostFacade.java`
- **문제**: 캐시에서 조회 후 조회수 증가 → 반환된 응답에 이전 조회수 표시
- **수정**: `increaseViewCount()`에 `@CacheEvict(value = "postDetail", key = "#postId")` 추가 + 컨트롤러 호출 순서 변경 (증가 먼저)

### 2-14. [MEDIUM] ClientIpUtil — X-Forwarded-For 파싱 오류
- **파일**: `global/util/ClientIpUtil.java`
- **문제**: 프록시 체인 환경에서 `"1.2.3.4, proxy1, proxy2"` 전체가 IP로 저장됨
- **수정**: `ip.split(",")[0].trim()`으로 첫 번째 IP만 추출

### 2-15. [MEDIUM] ImageFacade — NPE + 잘못된 예외 타입
- **파일**: `domain/image/controller/ImageFacade.java`
- **문제**: `originFilename`이 null이거나 확장자 없을 시 `NullPointerException`; 빈 파일에 404 반환
- **수정**: null/확장자 체크 추가, `EntityNotFoundException` → `InvalidValueException`

### 2-16. [LOW] DistributedLockAspect — 로그 포맷 문자열 버그
- **파일**: `global/aop/DistributedLockAspect.java`
- **문제**: `"락 획득 실패! lockKey: {}" + lockKey` → 로그에 `"{} lockKeyValue"` 형태로 출력
- **수정**: `log.warn("락 획득 실패! lockKey: {}", lockKey)`

### 2-17. [LOW] application.yml — 불필요한 SQL 로그
- **파일**: `src/main/resources/application.yml`
- **문제**: `format_sql: true`, `show_sql: true` → 운영 환경에서 성능 저하
- **수정**: `false`로 변경

### 2-18. [LOW] ReportCreateRequest — 유효성 검증 누락
- **파일**: `domain/report/application/dto/ReportCreateRequest.java`
- **문제**: `reasonCode` 필드에 길이 제한 없음
- **수정**: `@Size(max = 30)` 추가

---

## 3. 2차 수정 (이번 검토)

### 3-1. [CRITICAL] VoteFacade/ReportFacade — Lost Update 동시성 버그

**대상 파일**
- `domain/vote/application/VoteFacade.java`
- `domain/report/application/ReportFacade.java`
- `domain/post/domain/repository/PostRepository.java`
- `domain/comment/domain/repository/CommentRepository.java`

**문제**
```java
// 기존 (인메모리 증가 — Lost Update 발생 가능)
post.increaseLikeCount();      // SELECT 시점의 값에 +1
post.increaseDislikeCount();
post.increaseReportCount();
comment.increaseLikeCount();
comment.increaseReportCount();
```
분산 락으로 중복 투표는 방지되지만, 락 해제 후 다른 스레드가 이미 조회한 Post 객체로 업데이트하면 이전 값으로 덮어써짐.

**수정**
```java
// PostRepository에 원자적 쿼리 추가
@Modifying(clearAutomatically = true)
@Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
void incrementLikeCount(@Param("postId") Long postId);

@Modifying(clearAutomatically = true)
@Query("UPDATE Post p SET p.dislikeCount = p.dislikeCount + 1 WHERE p.id = :postId")
void incrementDislikeCount(@Param("postId") Long postId);

@Modifying(clearAutomatically = true)
@Query("UPDATE Post p SET p.reportCount = p.reportCount + 1 WHERE p.id = :postId")
void incrementReportCount(@Param("postId") Long postId);

@Modifying(clearAutomatically = true)
@Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
void incrementCommentCount(@Param("postId") Long postId);

@Modifying(clearAutomatically = true)
@Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
void decrementCommentCount(@Param("postId") Long postId);

// CommentRepository에 원자적 쿼리 추가
@Modifying(clearAutomatically = true)
@Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
void incrementLikeCount(@Param("commentId") Long commentId);

@Modifying(clearAutomatically = true)
@Query("UPDATE Comment c SET c.reportCount = c.reportCount + 1 WHERE c.id = :commentId")
void incrementReportCount(@Param("commentId") Long commentId);
```

VoteFacade, ReportFacade, CommentFacade의 인메모리 증가 메서드 호출을 모두 위 원자적 쿼리로 교체.

---

### 3-2. [HIGH] CommentFacade — 부모 댓글 검증 누락

**파일**: `domain/comment/application/CommentFacade.java`

**문제**
```java
// 기존: 부모 댓글이 존재하는지만 확인
if (request.getParentId() != null) {
    parentComment = commentRepository.findById(request.getParentId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
}
```
- 다른 게시글의 댓글을 부모로 지정 가능 → 데이터 정합성 훼손
- 대댓글의 대댓글 무한 중첩 가능

**수정**
```java
if (request.getParentId() != null) {
    parentComment = commentRepository.findById(request.getParentId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PARENT_COMMENT_NOT_FOUND));

    // 부모 댓글이 같은 게시글에 속하는지 검증
    if (!parentComment.getPost().getId().equals(postId)) {
        throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
    }

    // 1단계 대댓글만 허용 (대댓글의 대댓글 방지)
    if (parentComment.getParent() != null) {
        throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
```

댓글 카운트도 `postRepository.incrementCommentCount(postId)` 원자적 쿼리로 교체.
삭제도 `postRepository.decrementCommentCount(postId)` 원자적 쿼리로 교체.

---

### 3-3. [HIGH] SearchFacade — Elasticsearch 장애 시 서비스 전체 중단

**파일**: `domain/search/application/SearchFacade.java`

**문제**
```java
// ES가 죽으면 바로 503 에러
documents = postDocumentRepository.findByTitleContainsOrContentContains(keyword, keyword, pageable);
```

**수정**
```java
try {
    Page<PostDocument> documents = postDocumentRepository
            .findByTitleContainsOrContentContains(keyword, keyword, pageable);
    return new PageResponse<>(documents.map(postMapper::toListResponse));
} catch (DataAccessResourceFailureException e) {
    log.warn("Elasticsearch 연결 실패. DB 폴백 검색 수행. keyword: {}", keyword);
    // DB에서 직접 검색 (이미 PostRepository에 해당 쿼리 존재)
    Page<Post> posts = postRepository.findByTitleContainingOrContentContaining(keyword, pageable);
    return new PageResponse<>(posts.map(postMapper::toListResponse));
}
```

ES 장애 시 자동으로 DB 검색으로 폴백. `postRepository` 의존성 추가.

---

### 3-4. [MEDIUM] ImageFacade — 보안 취약점 (파일 업로드)

**파일**: `domain/image/controller/ImageFacade.java`

**문제**
- 확장자 검증 없음 (`.php`, `.jsp`, `.exe` 등 업로드 가능)
- MIME 타입 검증 없음 (Content-Type 조작 가능)
- 디렉토리 트래버설 공격 가능 (`../../../etc/passwd` 형태의 경로)
- 업로드 디렉토리 미생성 시 IOException 발생

**수정**
```java
private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
private static final List<String> ALLOWED_MIME_TYPES = List.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
);

// 확장자 화이트리스트 검증
String fileExtension = originFilename.substring(originFilename.lastIndexOf(".") + 1).toLowerCase();
if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
    throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
}

// MIME 타입 검증
String mimeType = file.getContentType();
if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
    throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
}

// 디렉토리 트래버설 방지
File uploadDir = new File(uploadPath);
if (!uploadDir.exists()) uploadDir.mkdirs();
File destFile = new File(uploadDir, storedFilename);
if (!destFile.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
    throw new InvalidValueException(ErrorCode.INVALID_INPUT_VALUE);
}
```

---

### 3-5. [LOW] ReportController — HTTP 상태코드 불일치

**파일**: `domain/report/presentation/ReportController.java`

**문제**: 신고 생성(POST) 성공 시 `200 OK` 반환 (REST 관례 위반)

**수정**: `ResponseEntity.status(HttpStatus.CREATED).build()` (201 Created)

---

### 3-6. [LOW] Post/Comment — allocationSize 성능 개선

**파일**: `domain/post/domain/Post.java`, `domain/comment/domain/Comment.java`

**문제**
```java
// allocationSize = 1 → INSERT마다 DB 시퀀스 조회 발생
// DummyDataBatchConfiguration에서 10만 건 삽입 시 10만 번의 시퀀스 쿼리
@SequenceGenerator(name = "post_id_seq", sequenceName = "POST_ID_SEQ", allocationSize = 1)
```

**수정**
```java
// JPA가 시퀀스를 50개씩 미리 가져와 캐싱 → 시퀀스 쿼리 50배 감소
@SequenceGenerator(name = "post_id_seq", sequenceName = "POST_ID_SEQ", allocationSize = 50)
@SequenceGenerator(name = "comment_id_seq", sequenceName = "COMMENT_ID_SEQ", allocationSize = 50)
```

---

### 3-7. [LOW] ApiResponse — 미사용 빈 클래스 삭제

**파일**: `global/common/ApiResponse.java` (삭제)

**문제**: 내용이 없는 빈 클래스가 코드베이스에 존재 (혼란 유발)

---

## 4. 아키텍처 개요

```
presentation (Controller)
    ↓
application (Facade = CommandFacade + QueryFacade)
    ↓
domain (Entity + Repository)
    ↓
infrastructure (MySQL / Redis / Elasticsearch)
```

### 주요 흐름

| 기능 | 흐름 |
|------|------|
| 게시글 작성 | PostController → PostFacade → PostRepository (MySQL) + EventPublisher → PostEventListener (비동기, ES 인덱싱) |
| 게시글 조회 | PostController → PostFacade(viewCount 원자적 증가+캐시 무효화) → PostQueryFacade(@Cacheable, Redis) |
| 인기 게시글 | BatchScheduler(10분마다) → Spring Batch Job → DB 집계 → Redis 저장 |
| 추천/비추천 | VoteFacade → @DistributedLock(Redisson) → 원자적 DB 카운트 증가 |
| 검색 | SearchFacade → Elasticsearch(폴백: MySQL) |

---

## 5. 미수정 사항 및 권장 개선

### 권장 사항 (즉시 적용 불필요)

| 항목 | 설명 | 우선순위 |
|------|------|----------|
| 게시글 삭제 시 이미지 파일 정리 | 디스크에 고아 파일 누적 | Medium |
| 갤러리 삭제 시 ES 인덱스 정리 | ES와 DB 불일치 | Medium |
| BatchScheduler 결과 검증 | `JobExecution.getStatus()` 확인 후 실패 알림 | Medium |
| PostEventListener 재시도 로직 | ES 인덱싱 실패 시 재처리 메커니즘 없음 | Medium |
| 댓글 삭제 응답 코드 | `200 OK` → `204 No Content` | Low |
| PasswordRequest DTO | 삭제 요청의 비밀번호를 Map 대신 DTO + `@Valid`로 처리 | Low |
| 검색 타입 활용 | `SearchRequest.SearchType`이 정의되어 있으나 미사용 | Low |

---

*이 보고서는 프로젝트 전체 코드 리뷰를 통해 발견된 모든 버그와 수정 내역을 기록합니다.*
