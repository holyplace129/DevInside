# 대용량 트래픽 처리를 위한 고성능 커뮤니티 백엔드, DevInside
**(부제: Redis 캐싱, 분산 락, 검색 엔진 도입을 통한 성능 최적화 및 확장성 확보)**

## Project Overview
대규모 트래픽이 발생하는 커뮤니티 서비스를 가정하여, **성능 최적화, 데이터 정합성, 시스템 확장성**을 고려한 백엔드 시스템을 구축했습니다. 단순한 기능 구현을 넘어, 발생 가능한 기술적 문제를 정의하고 이를 해결하는 과정에 집중했습니다.

- **기간**: 202X.XX - 202X.XX
- **인원**: 개인 프로젝트
- **Tech Blog**: (블로그 링크가 있다면 여기에 추가)
- **API Docs**: (Notion 또는 Swagger 링크가 있다면 여기에 추가)

---

## 🛠 Tech Stack
| 분류 | 기술 |
| :--- | :--- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2, Spring Batch, Spring Security, Spring Data JPA |
| **Database** | MySQL 8.0, Redis, H2 (Test) |
| **Search Engine** | Elasticsearch 8.11 |
| **Testing** | JUnit5, JMeter |
| **Build Tool** | Gradle (Kotlin DSL) |
| **DevOps** | Docker, Docker Compose |

---

## System Architecture
(여기에 Mermaid 다이어그램 또는 아키텍처 이미지를 삽입하세요)
![System Architecture](시스템 아키텍처 이미지 URL)

**핵심 아키텍처 특징:**
*   **Layered Architecture (DDD)**: `Controller -> Facade -> Domain`의 계층형 구조로 관심사 분리
*   **CQRS Pattern**: Command(MySQL), Query(Redis), Search(Elasticsearch)의 역할 분리를 통한 성능 최적화
*   **Event-Driven**: Spring Event를 활용한 비동기 데이터 동기화 (Decoupling)
*   **Distributed Lock**: Redis(Redisson)를 활용한 분산 환경 동시성 제어

---

## Key Achievements & Problem Solving

### [Challenge 1] 조회 API 성능 병목 해결과 캐싱 전략의 진화
> **Goal**: 대용량 트래픽 상황에서 게시글 목록 조회 API의 응답 속도 및 처리량 개선

*   **Problem**: JMeter 부하 테스트 결과, MySQL 직접 조회 시 **705.7 TPS**의 낮은 성능 기록. DB Disk I/O가 병목임을 확인.
*   **Process**:
    *   **1차 (Local Cache)**: Caffeine 도입으로 **6759.8 TPS** 달성. 단, Scale-out 시 데이터 불일치(Inconsistency) 한계 확인.
    *   **2차 (Global Cache)**: Redis 도입. `Serializable` 직렬화 문제를 JSON 직렬화(`GenericJackson2JsonRedisSerializer`)와 Jackson 커스텀 설정(`@JsonCreator`)으로 해결.
*   **Solve**:
    *   Redis Global Cache 적용 및 `@CacheEvict`를 통한 데이터 정합성 보장.
    *   안정적인 API 계약을 위해 `Page` 객체 대신 `PageResponse` DTO 도입.
*   **Result**: 최종 **7216.0 TPS** 달성 (초기 대비 **10.2배** 성능 향상).

### [Challenge 2] 좋아요 누락: 분산 환경에서의 동시성 제어
> **Goal**: 다중 사용자 동시 추천 시 발생하는 데이터 유실(Race Condition) 방지

*   **Problem**: 100명의 동시 추천 요청 테스트 시, DB에 **50개**만 카운트되는 데이터 정합성 문제 발생.
*   **Process**:
    *   `synchronized`는 단일 서버(JVM)에서만 유효하므로 분산 환경에서 부적합.
    *   DB Lock(Pessimistic)은 성능 저하 및 데드락 위험 존재.
    *   이미 사용 중인 **Redis**를 활용한 분산 락 도입 결정.
*   **Solve**:
    *   **Redisson 도입**: Pub/Sub 방식을 통해 스핀 락(Spin Lock)으로 인한 Redis 부하 최소화.
    *   **AOP 구현**: `@DistributedLock` 어노테이션을 직접 구현하여 비즈니스 로직과 락킹 로직 분리.
    *   **트랜잭션 문제 해결**: AOP 우선순위(`@Order`)를 조정하여 **'트랜잭션 커밋 후 락 해제'**를 강제함으로써 정합성 보장.
*   **Result**: 통합 테스트 결과 100개의 동시 요청에도 **누락 없이 100% 카운트**됨을 검증.

### [Challenge 3] 대용량 데이터 처리를 위한 배치 최적화
> **Goal**: 인기 게시글 집계를 위한 10만 건 이상의 데이터 효율적 처리

*   **Problem**: `DataInitializer`로 대용량 더미 데이터 생성 시 `OutOfMemory` 발생 및 DB 부하 급증. 단순 스케줄러(`@Scheduled`)로는 10만 건 처리가 불가능.
*   **Process**: 대용량 데이터는 메모리에 한 번에 올리는 것이 아니라, 나누어 처리해야 함을 인지.
*   **Solve**:
    *   **Spring Batch 도입**: 데이터를 1,000개 단위의 **Chunk**로 나누어 처리하여 메모리 사용량을 일정하게 유지.
    *   **JDBC Batch Insert**: `rewriteBatchedStatements=true` 옵션 활성화 및 ID 채번 전략을 `IDENTITY`에서 `SEQUENCE`로 변경하여, 1만 번의 `INSERT` 쿼리를 **단 10번의 네트워크 통신**으로 최적화.
*   **Result**: 메모리 사용량 **90% 이상 절감** 및 대용량 데이터 처리 속도 획기적 단축.

### [Challenge 4] RDBMS 검색 한계 극복과 검색 엔진 도입
> **Goal**: `LIKE` 검색의 성능 저하 문제 해결 및 검색 품질 향상

*   **Problem**: 데이터 증가 시 `LIKE` 검색은 **Full Table Scan**으로 인해 성능이 급격히 저하됨. 또한 'JPA' 검색 시 'JPA는'을 찾지 못하는 등 형태소 분석 부재.
*   **Solve**:
    *   **Elasticsearch 도입**: 역인덱스(Inverted Index) 구조를 활용하여 검색 성능을 **O(1)**에 가깝게 최적화.
    *   **비동기 동기화**: `Spring Event`(`@TransactionalEventListener`)와 `@Async`를 활용하여, 트랜잭션과 검색 인덱싱을 분리(Decoupling)함으로써 시스템 응답 속도와 안정성 확보.

---

## Technical Decision (Why?)

*   **Redis & SPOF**: 단순 캐싱뿐만 아니라 분산 락, 랭킹 시스템 등 활용도가 높아 선택했습니다. SPOF(Single Point of Failure)를 대비해 향후 Redis Sentinel 또는 Cluster 도입을 고려하고 있습니다.
*   **JWT vs Session**: Scale-out 환경을 전제로 하기에, 별도의 세션 저장소 없이 인증이 가능한 Stateless한 JWT를 선택했습니다.
*   **JPA vs MyBatis**: 도메인 주도 설계(DDD)와 객체지향적 모델링의 이점을 살리기 위해 JPA를 선택했습니다. 복잡한 쿼리는 QueryDSL(향후 도입 예정)이나 Elasticsearch로 보완합니다.

---
