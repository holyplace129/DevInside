# DevInside — 대용량 트래픽을 고려한 익명 커뮤니티 백엔드

**DCInside 스타일의 갤러리 기반 익명 게시판**  
로그인 없이 비밀번호만으로 게시글·댓글을 관리하며, Redis 캐싱·분산 락·Elasticsearch를 통한 성능 최적화와 동시성 제어에 집중한 백엔드 프로젝트입니다.

- **기간**: 2025.07 — 2025.07
- **인원**: 개인 프로젝트
- **GitHub**: https://github.com/holyplace129/DevInside

---

## 목차

- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Key Technical Challenges](#key-technical-challenges)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Local Setup](#local-setup)
- [Technical Decision](#technical-decision)

---

## Tech Stack

| 분류 | 기술 | 버전 |
| :--- | :--- | :--- |
| Language | Java | 17 |
| Framework | Spring Boot | 3.5.4 |
| Framework | Spring Security | 6.5.x |
| ORM | Spring Data JPA / Hibernate | 6.6.x |
| Database | MySQL | 8.x |
| Cache | Spring Cache + Redis (Lettuce) | - |
| Distributed Lock | Redisson | 3.27.2 |
| Search | Spring Data Elasticsearch | 5.x |
| Batch | Spring Batch | 5.x |
| Mapping | MapStruct | 1.5.5.Final |
| Build | Gradle | 8.x |

---

## System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         Client (HTTP)                            │
└──────────────────────────┬───────────────────────────────────────┘
                           │ REST API
┌──────────────────────────▼───────────────────────────────────────┐
│                    Presentation Layer                             │
│   GalleryController · PostController · CommentController         │
│   VoteController · ReportController · SearchController           │
│   ImageController                                                │
└───────────────┬──────────────────────────────┬───────────────────┘
                │ Command                      │ Query
┌───────────────▼───────────┐   ┌──────────────▼────────────────┐
│      Command Facade        │   │       Query Facade             │
│  PostFacade                │   │  PostQueryFacade               │
│  CommentFacade             │   │  GalleryQueryFacade            │
│  VoteFacade                │   │  SearchFacade                  │
│  ReportFacade              │   │                                │
│  GalleryFacade             │   │  @Cacheable (Redis)            │
│  ImageFacade               │   │  TTL: 10분                     │
└───────┬───────────┬────────┘   └───────┬───────────────────────┘
        │           │                    │
        │     ┌─────▼──────┐       ┌─────▼──────┐
        │     │  Redisson   │       │   Redis    │
        │     │ Distributed │       │   Cache    │
        │     │    Lock     │       │            │
        │     └─────────────┘       └────────────┘
┌───────▼────────────────────────────────────────────────────────┐
│                       Domain Layer                              │
│  Post · Comment · Vote · Report · Gallery · PostImage          │
│  원자적 JPQL @Modifying 쿼리로 카운트 동시성 보장              │
└─────────────────────────────┬──────────────────────────────────┘
                              │
              ┌───────────────▼───────────────┐
              │           MySQL               │
              └───────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                     비동기 / 배치 레이어                        │
│                                                                │
│  게시글 저장/수정                                              │
│  → ApplicationEventPublisher                                   │
│  → PostEventListener (@Async, @TransactionalEventListener)     │
│  → Elasticsearch 인덱싱 (posts 인덱스)                         │
│                                                                │
│  BatchScheduler (10분 Cron)                                    │
│  → Spring Batch: JpaPagingItemReader → Processor → Writer      │
│  → 인기 게시글 집계 결과 → Redis 저장 (TTL: 15분)              │
└────────────────────────────────────────────────────────────────┘
```

**핵심 아키텍처 특징**

- **Layered Architecture (DDD)**: `Controller → Facade → Domain` 계층형 구조로 관심사 분리
- **Command/Query 분리**: `PostFacade`(쓰기) + `PostQueryFacade`(읽기)로 역할 분리
- **Event-Driven 인덱싱**: Spring Event + `@TransactionalEventListener`로 ES 인덱싱을 트랜잭션과 분리
- **Distributed Lock**: Redisson `@DistributedLock` AOP로 비즈니스 로직과 락 로직 분리

---

## Key Technical Challenges

### 1. 추천 카운트 Lost Update — 분산 락 + 원자적 쿼리

**문제**  
`post.increaseLikeCount()` (JPA Dirty Checking 방식)는 여러 스레드가 동시에 같은 값을 읽은 뒤 각자 +1해 덮어쓰는 Lost Update가 발생한다. 분산 락만으로는 중복 추천을 막을 수 있지만 락 해제 직후 이전 값을 읽어 둔 스레드가 업데이트하면 카운트가 소실된다.

**해결**  
`PostRepository`에 `@Modifying @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")` 원자적 쿼리를 추가하고 모든 카운트 증가를 DB 레벨 연산으로 교체했다. 분산 락은 중복 방지, 원자적 쿼리는 카운트 정확성으로 역할을 분리했다.

**결과**  
추천·비추천·신고·댓글 수 총 7개 카운트 필드에서 동시성 오류 없이 정확한 카운트가 보장된다.

---

### 2. 게시글 조회수 캐시 정합성 — @CacheEvict 순서 제어

**문제**  
상세 조회 API에서 `@Cacheable`로 캐시를 먼저 읽고 그 다음 `viewCount`를 DB에 증가시켰다. 반환된 응답에는 증가 전 값이 담기고 이후 TTL(10분) 동안 모든 사용자에게 stale한 조회수가 표시됐다.

**해결**  
`increaseViewCount()`에 `@CacheEvict(value = "postDetail", key = "#postId")`를 추가하고 컨트롤러 호출 순서를 *증가 → 캐시 무효화 → 조회* 순으로 변경했다. `viewCount` 증가도 `UPDATE viewCount = viewCount + 1` 원자적 쿼리로 전환했다.

**결과**  
사용자에게 항상 최신 조회수가 반환되며 캐시는 조회 즉시 재갱신되어 DB 부하가 낮게 유지된다.

---

### 3. Elasticsearch 장애 시 검색 전체 중단 — DB 폴백

**문제**  
검색이 Elasticsearch에만 의존해, ES 연결 실패 시 `DataAccessResourceFailureException`이 전파되어 503이 반환됐다. 게시글 인덱싱도 `@Async` 실패 시 로그만 출력하고 재시도 없이 무시되었다.

**해결**  
`SearchFacade`에서 `DataAccessResourceFailureException`을 catch해 MySQL `LIKE` 검색으로 자동 폴백한다. ES 인덱싱은 `@TransactionalEventListener`(트랜잭션 커밋 후 수신) + `@Async`(별도 스레드)로 HTTP 응답과 완전히 분리했다.

**결과**  
ES 정상 시 전문 검색, 장애 시 DB 검색으로 자동 전환되어 서비스 연속성이 보장된다. 인덱싱 실패가 API 응답에 영향을 주지 않는다.

---

## API Endpoints

### Gallery
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/galleries` | 갤러리 목록 |
| `GET` | `/galleries/{name}` | 갤러리 상세 |
| `POST` | `/galleries` | 갤러리 생성 |
| `PUT` | `/galleries/{name}` | 갤러리 수정 |
| `DELETE` | `/galleries/{name}` | 갤러리 삭제 |

### Post
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/galleries/{galleryName}/posts` | 게시글 목록 (페이지네이션) |
| `GET` | `/galleries/{galleryName}/posts/{postId}` | 게시글 상세 |
| `GET` | `/galleries/{galleryName}/posts/popular` | 인기 게시글 (Redis) |
| `POST` | `/galleries/{galleryName}/posts` | 게시글 작성 |
| `PUT` | `/galleries/{galleryName}/posts/{postId}` | 게시글 수정 (비밀번호 필요) |
| `DELETE` | `/galleries/{galleryName}/posts/{postId}` | 게시글 삭제 (비밀번호 필요) |

### Comment
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/galleries/{galleryName}/posts/{postId}/comments` | 댓글 목록 (계층형) |
| `POST` | `/galleries/{galleryName}/posts/{postId}/comments` | 댓글/대댓글 작성 |
| `PUT` | `/galleries/{galleryName}/posts/{postId}/comments/{commentId}` | 댓글 수정 (비밀번호 필요) |
| `DELETE` | `/galleries/{galleryName}/posts/{postId}/comments/{commentId}` | 댓글 삭제 (비밀번호 필요) |

### Vote & Report
| Method | URL | 설명 |
|--------|-----|------|
| `POST` | `/galleries/{galleryName}/posts/{postId}/like` | 게시글 추천 (IP 기반 중복 방지) |
| `POST` | `/galleries/{galleryName}/posts/{postId}/dislike` | 게시글 비추천 |
| `POST` | `/galleries/{galleryName}/posts/{postId}/comments/{commentId}/like` | 댓글 추천 |
| `POST` | `/galleries/{galleryName}/posts/{postId}/reports` | 게시글 신고 |
| `POST` | `/galleries/{galleryName}/posts/{postId}/comments/{commentId}/reports` | 댓글 신고 |

### Search & Image
| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/search/posts?keyword={keyword}` | 게시글 검색 (ES → DB 폴백) |
| `POST` | `/images` | 이미지 업로드 |

---

## Project Structure

```
src/main/java/org/learn/board/
├── domain/
│   ├── gallery/          # 갤러리 도메인
│   ├── post/             # 게시글 도메인
│   │   ├── application/  # Facade, DTO, Mapper
│   │   ├── batch/        # Spring Batch (인기 게시글 집계)
│   │   ├── domain/       # Entity, Repository
│   │   ├── event/        # PostSavedEvent
│   │   └── presentation/ # Controller
│   ├── comment/          # 댓글 도메인
│   ├── vote/             # 추천/비추천 도메인
│   ├── report/           # 신고 도메인
│   ├── search/           # 검색 도메인 (Elasticsearch)
│   └── image/            # 이미지 업로드
├── global/
│   ├── aop/              # @DistributedLock AOP
│   ├── config/           # Redis, Security 설정
│   ├── domain/           # BaseTimeEntity
│   ├── error/            # GlobalExceptionHandler, ErrorCode
│   ├── init/             # DummyData Batch
│   └── util/             # ClientIpUtil
└── BoardApplication.java
```

---

## Local Setup

### 사전 요구사항

- Java 17
- MySQL 8.x
- Redis 7.x
- Elasticsearch 8.x (선택 — 없으면 DB 검색으로 폴백)

### 실행 방법

**1. MySQL 데이터베이스 생성**
```sql
CREATE DATABASE board_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'board_user'@'localhost' IDENTIFIED BY '0000';
GRANT ALL PRIVILEGES ON board_db.* TO 'board_user'@'localhost';
```

**2. application.yml 확인 (기본값)**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/board_db
    username: board_user
    password: '0000'
  data:
    redis:
      host: localhost
      port: 6379
  elasticsearch:
    uris: http://localhost:9200

custom:
  upload:
    path: C:/codes/board/uploads/   # 이미지 저장 경로 (환경에 맞게 수정)
```

**3. 빌드 및 실행**
```bash
./gradlew bootRun
```

**4. 더미 데이터 삽입 (선택)**  
`application.yml`에서 `spring.batch.job.enabled: true`로 변경 후 실행하면 `DummyDataBatchConfiguration`이 동작합니다.

---

## Technical Decision

| 기술 | 선택 이유 |
|------|-----------|
| **Redisson (분산 락)** | Lettuce 대비 tryLock 타임아웃 제어 용이, Pub/Sub 방식으로 스핀 락 부하 최소화, SpEL로 동적 락 키 생성 |
| **Spring Cache + Redis** | 갤러리 목록·게시글 상세 반복 조회 비용 감소, TTL 10분으로 DB 부하 분산 |
| **Spring Batch** | 인기 게시글 집계를 Chunk(100) 단위로 처리해 메모리 일정하게 유지, 실패 재시작 포인트 관리 |
| **@TransactionalEventListener** | 트랜잭션 롤백 시 ES 인덱싱 방지, DB 커밋 확정 후에만 인덱싱 수행 |
| **원자적 JPQL @Modifying** | 애플리케이션 레벨 락 없이 DB 레벨 연산으로 카운트 정확성 보장 |
| **익명 게시판 (비밀번호 방식)** | 로그인 없이 BCrypt 해시 비밀번호로 게시글·댓글 수정/삭제 권한 관리 |
